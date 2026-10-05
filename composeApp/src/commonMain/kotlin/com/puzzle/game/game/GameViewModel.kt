package com.puzzle.game.game

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzle.game.ai.AIImageGenerator
import com.puzzle.game.analytics.Analytics
import com.puzzle.game.analytics.AnalyticsEvent
import com.puzzle.game.analytics.AnalyticsScreen
import com.puzzle.logger.PuzzleLog
import com.puzzle.game.data.BuiltinStoryImageSet
import com.puzzle.game.data.PuzzlePictureGenerator
import com.puzzle.game.data.StoryPresets
import com.puzzle.game.data.ThemeData
import com.puzzle.game.data.ThemePresets
import com.puzzle.game.decodeToImageBitmap
import com.puzzle.game.platformCacheDir
import com.puzzle.game.readFileBytes
import com.puzzle.game.engine.PuzzleEngine
import com.puzzle.game.engine.PieceBitmapGenerator
import com.puzzle.game.engine.PuzzleConfig
import com.puzzle.game.native.NativeSplitAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class GameViewModel : ViewModel() {
    private val blockSize = PuzzleConfig.PIXEL_BLOCK_SIZE
    private val maxEagerPieceBitmaps = 120

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    private val engine = PuzzleEngine()
    private val nativeAdapter = NativeSplitAdapter()
    private val aiGenerator = AIImageGenerator()
    val dragDropState = DragDropState()

    val storySets: List<BuiltinStoryImageSet> = StoryPresets.stories
    val storyPages: List<com.puzzle.game.data.StoryPageData>
        get() = StoryPresets.pagesForStory(_state.value.selectedStoryId)

    private var timerJob: Job? = null
    private var gameRunId: String? = null
    private var gameSource: String = "unknown"
    private var prepareStartedAt: TimeMark? = null
    private var wrongPlacementCount: Int = 0
    private var retryCount: Int = 0
    private var pendingParentGameRunId: String? = null
    private var parentGameRunId: String? = null
    private val reportedProgressMilestones = mutableSetOf<Int>()

    fun selectTheme(themeId: String) {
        val theme = StoryPresets.pagesForStory(_state.value.selectedStoryId)
            .firstOrNull { it.theme.id == themeId }
            ?.theme
            ?: ThemePresets.getById(themeId)
        Analytics.track(
            AnalyticsEvent.ThemeSelect,
            mapOf(
                "story_id" to _state.value.selectedStoryId,
                "theme_id" to theme.id,
                "story_page_index" to StoryPresets.indexOfTheme(theme.id)
            )
        )
        _state.update {
            it.copy(
                selectedTheme = theme,
                selectedStoryPageIndex = StoryPresets.indexOfTheme(theme.id)
            )
        }
    }

    fun selectStory(storyId: String) {
        val pages = StoryPresets.pagesForStory(storyId)
        val firstPage = pages.firstOrNull() ?: return
        Analytics.track(
            AnalyticsEvent.ThemeSelect,
            mapOf(
                "story_id" to firstPage.storyId,
                "theme_id" to firstPage.theme.id,
                "story_page_index" to 0,
                "selection_type" to "story"
            )
        )
        _state.update {
            it.copy(
                selectedStoryId = firstPage.storyId,
                selectedStoryPageIndex = 0,
                selectedTheme = firstPage.theme
            )
        }
    }

    fun selectDifficulty(difficulty: GameDifficulty) {
        _state.update { it.copy(difficulty = difficulty, pieceCount = difficulty.pieceCount) }
    }

    fun selectPieceCount(pieceCount: Int) {
        val normalized = pieceCount.coerceIn(10, 300)
        if (_state.value.pieceCount != normalized && normalized % 10 == 0) {
            Analytics.track(
                AnalyticsEvent.PieceCountChange,
                mapOf("piece_count" to normalized)
            )
        }
        _state.update { it.copy(pieceCount = normalized) }
    }

    fun selectStoryPage(index: Int) {
        val pages = storyPages
        val pageIndex = ((index % pages.size) + pages.size) % pages.size
        val page = pages[pageIndex]
        Analytics.track(
            AnalyticsEvent.StoryPageSelect,
            mapOf(
                "story_id" to page.storyId,
                "theme_id" to page.theme.id,
                "story_page_index" to pageIndex
            )
        )
        _state.update {
            it.copy(
                selectedStoryPageIndex = pageIndex,
                selectedTheme = page.theme
            )
        }
    }

    fun setPositionHintEnabled(enabled: Boolean) {
        if (_state.value.showPositionHint == enabled) return
        Analytics.click(
            target = "position_hint_toggle",
            screen = AnalyticsScreen.Game,
            properties = mapOf("enabled" to enabled)
        )
        _state.update { it.copy(showPositionHint = enabled) }
    }

    fun moveStoryPage(delta: Int) {
        selectStoryPage(_state.value.selectedStoryPageIndex + delta)
    }

    fun hasNextStoryPage(): Boolean {
        return _state.value.selectedStoryPageIndex < storyPages.lastIndex
    }

    fun startNextStoryPage(): Boolean {
        val nextIndex = _state.value.selectedStoryPageIndex + 1
        if (nextIndex !in storyPages.indices) return false
        selectStoryPage(nextIndex)
        startGame()
        return true
    }

    /**
     * Start a game. If the selected theme has an assetFile (native image),
     * route to the Rust native splitter. Otherwise use the Kotlin procedural path.
     * All I/O and computation runs off the main thread to avoid ANR.
     */
    fun startGame() {
        val currentState = _state.value
        val theme = currentState.selectedTheme ?: ThemePresets.themes.first()
        val pieceCount = currentState.pieceCount
        val assetFile = theme.assetFile
        beginGameRun(if (assetFile != null) "builtin_asset" else "procedural")

        PuzzleLog.i("GameVM", "Starting game: theme=${theme.id} pieces=$pieceCount asset=${assetFile ?: "procedural"}")
        Analytics.track(
            AnalyticsEvent.GameStart,
            baseGameProperties(theme.id, pieceCount)
        )

        // Set loading state immediately, then do blocking work in coroutine
        resetForNewGame()

        viewModelScope.launch {
            try {
                // If theme has a built-in asset image, try native Rust splitter first
                if (assetFile != null) {
                    val loadStartedAt = TimeSource.Monotonic.markNow()
                    val bytes = withContext(Dispatchers.Default) {
                        com.puzzle.game.data.AssetLoader.readBytes(assetFile)
                    }
                    trackGameTiming("asset_load", loadStartedAt.elapsedNow().inWholeMilliseconds)
                    if (bytes != null) {
                        PuzzleLog.d("GameVM", "Loaded asset: $assetFile (${bytes.size} bytes)")
                        startGameWithImageInternal(bytes, pieceCount)
                        return@launch
                    }
                    setError(
                        message = "故事图片读取失败，请重新选择故事",
                        operation = "asset_load",
                        errorCode = "asset_not_found"
                    )
                    return@launch
                }

                // Kotlin procedural path
                val generationStartedAt = TimeSource.Monotonic.markNow()
                val gameData = withContext(Dispatchers.Default) {
                    val puzzleBitmap = PuzzlePictureGenerator.generate(theme, 800, 600)
                    engine.loadImage(puzzleBitmap.width, puzzleBitmap.height)
                    engine.splitImage(pieceCount = pieceCount, blockSize = blockSize)
                    val pieces = engine.shufflePieces()
                    GeneratedGameData(
                        pieces = pieces,
                        bitmap = puzzleBitmap,
                        pieceBitmaps = generatePieceBitmapsIfAffordable(puzzleBitmap, pieces),
                        imageWidth = puzzleBitmap.width,
                        imageHeight = puzzleBitmap.height
                    )
                }
                trackGameTiming("procedural_generate_split", generationStartedAt.elapsedNow().inWholeMilliseconds)

                applyNewGame(
                    pieces = gameData.pieces,
                    bitmap = gameData.bitmap,
                    pieceBitmaps = gameData.pieceBitmaps,
                    imageWidth = gameData.imageWidth,
                    imageHeight = gameData.imageHeight,
                    blockSize = blockSize
                )
                startTimer()
            } catch (e: Exception) {
                setError(
                    message = "生成拼图失败: ${e.message ?: "未知错误"}",
                    operation = "prepare_game",
                    errorCode = "generation_failed"
                )
            }
        }
    }

    /**
     * Start a game with raw image bytes (Native/Rust path).
     * Use this for AI-generated images, photos, or any PNG/JPEG data.
     * Safe to call from main thread — runs I/O in coroutine.
     *
     * @param imageBytes Raw PNG or JPEG bytes
     */
    fun startGameWithImage(imageBytes: ByteArray) {
        val pieceCount = _state.value.pieceCount
        beginGameRun("external_image")
        Analytics.track(
            AnalyticsEvent.GameStart,
            baseGameProperties(_state.value.selectedTheme?.id ?: "external", pieceCount)
        )
        resetForNewGame()
        viewModelScope.launch {
            try {
                startGameWithImageInternal(imageBytes, pieceCount)
            } catch (error: Exception) {
                setError(
                    message = "图片处理失败: ${error.message ?: "未知错误"}",
                    operation = "image_decode",
                    errorCode = "image_processing_failed"
                )
            }
        }
    }

    /**
     * Start a game using AI image generation.
     * Attempts to generate via the configured AI provider,
     * falling back to procedural generation if AI is unavailable.
     */
    fun startAIGame(promptOverride: String? = null) {
        val currentState = _state.value
        val theme = currentState.selectedTheme ?: ThemePresets.themes.first()
        val pieceCount = currentState.pieceCount
        val prompt = promptOverride?.trim()?.takeIf { it.isNotBlank() } ?: theme.description
        beginGameRun("ai_generated")

        Analytics.track(
            AnalyticsEvent.GameStart,
            baseGameProperties(theme.id, pieceCount)
        )

        PuzzleLog.i("GameVM", "Starting AI game: theme=${theme.id} prompt='$prompt' pieces=$pieceCount")
        Analytics.track(
            AnalyticsEvent.AiGameStart,
            baseGameProperties(theme.id, pieceCount) + mapOf("prompt_length" to prompt.length)
        )

        resetForNewGame()

        viewModelScope.launch {
            try {
                val aiStartedAt = TimeSource.Monotonic.markNow()
                val generated = withContext(Dispatchers.Default) {
                    aiGenerator.generate(prompt)
                }
                trackGameTiming(
                    operation = "ai_generate",
                    durationMs = aiStartedAt.elapsedNow().inWholeMilliseconds,
                    properties = mapOf(
                        "result" to when {
                            generated.imageUrl != null -> "remote_url"
                            generated.localPath != null -> "local_file"
                            else -> "empty"
                        }
                    )
                )

                // If AI returned an image URL, download and split
                if (generated.imageUrl != null) {
                    val downloadStartedAt = TimeSource.Monotonic.markNow()
                    val imageBytes = withContext(Dispatchers.Default) {
                        downloadImage(generated.imageUrl)
                    }
                    trackGameTiming(
                        operation = "ai_image_download",
                        durationMs = downloadStartedAt.elapsedNow().inWholeMilliseconds,
                        properties = mapOf("success" to (imageBytes != null))
                    )
                    if (imageBytes != null) {
                        startGameWithImageInternal(imageBytes, pieceCount)
                        return@launch
                    }
                }

                // If AI returned a local file path, load and split
                if (generated.localPath != null) {
                    val bytes = withContext(Dispatchers.Default) {
                        readFileBytes(generated.localPath)
                    }
                    if (bytes != null) {
                        startGameWithImageInternal(bytes, pieceCount)
                        return@launch
                    }
                }

                // AI returned no usable image — fall back to procedural
                startGuardedProcedural(theme, pieceCount, "no_usable_image")
            } catch (e: Exception) {
                startGuardedProcedural(theme, pieceCount, "provider_exception")
            }
        }
    }

    /**
     * Start procedural game with error guard.
     * If even procedural generation fails, sets ERROR phase so user sees a retry button.
     */
    private suspend fun startGuardedProcedural(theme: ThemeData, pieceCount: Int, fallbackReason: String? = null) {
        try {
            fallbackReason?.let { reason ->
                PuzzleLog.w("GameVM", "AI generation failed, falling back to procedural: $reason")
                Analytics.track(
                    AnalyticsEvent.AiFallback,
                    baseGameProperties(theme.id, pieceCount) + mapOf("reason" to reason)
                )
            }
            startProceduralGame(theme, pieceCount)
        } catch (e: Exception) {
            setError(
                message = "拼图生成失败: ${e.message ?: "未知错误"}",
                operation = "ai_fallback",
                errorCode = "procedural_fallback_failed"
            )
        }
    }

    /**
     * Internal: assumes already running in a coroutine.
     * Called from startGame() and startGameWithImage().
     */
    private suspend fun startGameWithImageInternal(imageBytes: ByteArray, pieceCount: Int) {
        val imgSize = "${imageBytes.size / 1024}KB"
        val decodeStartedAt = TimeSource.Monotonic.markNow()
        val bitmap = withContext(Dispatchers.Default) {
            decodeToImageBitmap(imageBytes)
        } ?: throw IllegalArgumentException("无法解码图片")
        trackGameTiming("image_decode", decodeStartedAt.elapsedNow().inWholeMilliseconds)

        if (blockSize == PuzzleConfig.PIXEL_BLOCK_SIZE) {
            startGameWithBitmapInternal(bitmap, pieceCount)
            return
        }

        val nativeStartedAt = TimeSource.Monotonic.markNow()
        val success = withContext(Dispatchers.Default) {
            nativeAdapter.loadAndSplit(imageBytes, pieceCount, blockSize, platformCacheDir())
        }
        trackGameTiming(
            operation = "native_split",
            durationMs = nativeStartedAt.elapsedNow().inWholeMilliseconds,
            properties = mapOf("success" to success)
        )
        if (!success) {
            PuzzleLog.w("GameVM", "Native split failed ($imgSize), falling back to Kotlin engine")
            startGameWithBitmapInternal(bitmap, pieceCount)
            return
        }

        val (imgW, imgH) = nativeAdapter.imageSize
        val nativePieceBitmaps = nativeAdapter.pieceBitmaps.ifEmpty {
            generatePieceBitmapsIfAffordable(bitmap, nativeAdapter.pieces)
        }
        applyNewGame(
            pieces = nativeAdapter.pieces,
            bitmap = bitmap,
            pieceBitmaps = nativePieceBitmaps,
            imageWidth = imgW,
            imageHeight = imgH,
            blockSize = blockSize,
            customPositions = nativeAdapter.correctPositions
        )
        startTimer()
    }

    // ── Shared game setup ────────────────────────────────

    /**
     * Procedural fallback: generates an image with PuzzlePictureGenerator
     * and splits it with the Kotlin puzzle engine.
     */
    private suspend fun startProceduralGame(theme: ThemeData, pieceCount: Int) {
        PuzzleLog.d("GameVM", "Procedural generation: theme=${theme.id} pieces=$pieceCount")
        val generationStartedAt = TimeSource.Monotonic.markNow()
        val gameData = withContext(Dispatchers.Default) {
            val puzzleBitmap = PuzzlePictureGenerator.generate(theme, 800, 600)
            engine.loadImage(puzzleBitmap.width, puzzleBitmap.height)
            engine.splitImage(pieceCount = pieceCount, blockSize = blockSize)
            val pieces = engine.shufflePieces()
            GeneratedGameData(
                pieces = pieces,
                bitmap = puzzleBitmap,
                pieceBitmaps = generatePieceBitmapsIfAffordable(puzzleBitmap, pieces),
                imageWidth = puzzleBitmap.width,
                imageHeight = puzzleBitmap.height
            )
        }
        trackGameTiming("procedural_generate_split", generationStartedAt.elapsedNow().inWholeMilliseconds)

        applyNewGame(
            pieces = gameData.pieces,
            bitmap = gameData.bitmap,
            pieceBitmaps = gameData.pieceBitmaps,
            imageWidth = gameData.imageWidth,
            imageHeight = gameData.imageHeight,
            blockSize = blockSize
        )
        startTimer()
    }

    private suspend fun startGameWithBitmapInternal(bitmap: ImageBitmap, pieceCount: Int) {
        val splitStartedAt = TimeSource.Monotonic.markNow()
        val gameData = withContext(Dispatchers.Default) {
            engine.loadImage(bitmap.width, bitmap.height)
            engine.splitImage(pieceCount = pieceCount, blockSize = blockSize)
            val pieces = engine.shufflePieces()
            GeneratedGameData(
                pieces = pieces,
                bitmap = bitmap,
                pieceBitmaps = generatePieceBitmapsIfAffordable(bitmap, pieces),
                imageWidth = bitmap.width,
                imageHeight = bitmap.height
            )
        }
        trackGameTiming("kotlin_split", splitStartedAt.elapsedNow().inWholeMilliseconds)

        applyNewGame(
            pieces = gameData.pieces,
            bitmap = gameData.bitmap,
            pieceBitmaps = gameData.pieceBitmaps,
            imageWidth = gameData.imageWidth,
            imageHeight = gameData.imageHeight,
            blockSize = blockSize
        )
        startTimer()
    }

    private fun generatePieceBitmapsIfAffordable(
        bitmap: ImageBitmap,
        pieces: List<com.puzzle.game.engine.model.PuzzlePiece>
    ): Map<String, ImageBitmap> {
        if (pieces.size > maxEagerPieceBitmaps) {
            PuzzleLog.i("GameVM", "Skip eager piece bitmap generation for ${pieces.size} pieces")
            return emptyMap()
        }
        if (pieces.any { it.items.isEmpty() }) {
            PuzzleLog.i("GameVM", "Skip piece bitmap generation because runtime pieces store bounds only")
            return emptyMap()
        }
        return PieceBitmapGenerator.generate(bitmap, pieces, blockSize)
    }

    /**
     * Download image bytes from a URL.
     * Returns null on failure — caller should fall back to procedural generation.
     */
    private suspend fun downloadImage(url: String): ByteArray? {
        return try {
            HttpClient().use { client ->
                client.get(url).body<ByteArray>()
            }
        } catch (e: Exception) {
            PuzzleLog.w("GameVM", "Image download failed, falling back to procedural", e)
            Analytics.monitorError(
                component = "network",
                operation = "ai_image_download",
                errorCode = "download_failed",
                properties = baseGameProperties(
                    _state.value.selectedTheme?.id ?: "unknown",
                    _state.value.pieceCount
                )
            )
            null
        }
    }

    private fun resetForNewGame() {
        nativeAdapter.close()
        dragDropState.cancelDrag()
        dragDropState.clearSelection()
        stopTimer()
        _state.update {
            it.copy(
                phase = GamePhase.GENERATING,
                isImageLoading = true,
                elapsedSeconds = 0,
                isPaused = false,
                wrongDropHint = false,
                showCelebration = false,
                cellFilledBy = emptyMap()
            )
        }
    }

    private fun applyNewGame(
        pieces: List<com.puzzle.game.engine.model.PuzzlePiece>,
        bitmap: ImageBitmap,
        pieceBitmaps: Map<String, ImageBitmap> = emptyMap(),
        imageWidth: Int,
        imageHeight: Int,
        blockSize: Int,
        customPositions: Map<String, Pair<Int, Int>>? = null
    ) {
        val gridCols = ceilDiv(imageWidth, blockSize)
        val gridRows = ceilDiv(imageHeight, blockSize)

        val correctPositions = customPositions ?: run {
            val map = mutableMapOf<String, Pair<Int, Int>>()
            for (piece in pieces) {
                if (piece.items.isNotEmpty()) {
                    val center = piece.items[piece.items.size / 2]
                    map[piece.id] = Pair(center.y, center.x)
                } else {
                    map[piece.id] = Pair(
                        piece.pixels.top + piece.pixels.height / 2,
                        piece.pixels.left + piece.pixels.width / 2
                    )
                }
            }
            map
        }

        // Log piece dimensions summary
        if (pieces.isNotEmpty()) {
            val dims = pieces.joinToString(", ") { p ->
                "${p.id}: ${p.pixels.width}×${p.pixels.height}px@(${p.pixels.left},${p.pixels.top}) [${p.items.size} stored cells]"
            }
            PuzzleLog.i("GameVM", "New game ready: ${imageWidth}×${imageHeight}px grid=${gridCols}×${gridRows} bs=$blockSize pieces=${pieces.size}")
            PuzzleLog.d("GameVM", "Piece dimensions: $dims")
        }
        val prepareDurationMs = prepareStartedAt?.elapsedNow()?.inWholeMilliseconds ?: 0L
        val currentThemeId = _state.value.selectedTheme?.id ?: "unknown"
        Analytics.track(
            AnalyticsEvent.GameReady,
            baseGameProperties(currentThemeId, pieces.size) + mapOf(
                "piece_count" to pieces.size,
                "image_width" to imageWidth,
                "image_height" to imageHeight,
                "grid_cols" to gridCols,
                "grid_rows" to gridRows,
                "block_size" to blockSize,
                "has_piece_bitmaps" to pieceBitmaps.isNotEmpty(),
                "prepare_duration_ms" to prepareDurationMs
            )
        )
        trackGameTiming("prepare_total", prepareDurationMs)
        prepareStartedAt = null

        _state.update {
            it.copy(
                phase = GamePhase.PLAYING,
                pieces = pieces,
                puzzleBitmap = bitmap,
                pieceBitmaps = pieceBitmaps,
                gridCols = gridCols,
                gridRows = gridRows,
                correctPositions = correctPositions,
                cellFilledBy = mutableMapOf(),
                isImageLoading = false,
                showCelebration = false
            )
        }
    }

    private fun ceilDiv(value: Int, divisor: Int): Int {
        return ((value + divisor - 1) / divisor).coerceAtLeast(1)
    }

    // ── Gameplay ─────────────────────────────────────────

    fun tryPlacePiece(pieceId: String, targetPieceId: String, inputMethod: String = "tap") {
        val isCorrectTarget = pieceId == targetPieceId

        if (isCorrectTarget) {
            val before = _state.value
            val placedCount = before.cellFilledBy.size + 1
            val totalCount = before.pieces.size.coerceAtLeast(1)
            val allPlaced = placedCount >= totalCount
            val eventProperties = baseGameProperties(
                themeId = before.selectedTheme?.id ?: "unknown",
                pieceCount = totalCount
            ) + mapOf(
                "result" to "success",
                "input_method" to inputMethod,
                "placed_count" to placedCount,
                "wrong_attempt_count" to wrongPlacementCount,
                "elapsed_seconds" to before.elapsedSeconds
            )

            if (placedCount == 1 || allPlaced) {
                Analytics.track(AnalyticsEvent.PiecePlace, eventProperties)
            }
            reportProgressMilestones(placedCount, totalCount, inputMethod)

            val newCellFilled = before.cellFilledBy.toMutableMap().apply { put(pieceId, pieceId) }
            _state.update { current ->
                current.copy(
                    cellFilledBy = newCellFilled,
                    phase = if (allPlaced) GamePhase.COMPLETED else current.phase,
                    showCelebration = allPlaced || current.showCelebration
                )
            }

            if (allPlaced) {
                stopTimer()
                PuzzleLog.i("GameVM", "Puzzle completed! pieces=$totalCount time=${before.elapsedSeconds}s")
                val completionProperties = eventProperties + mapOf(
                    "total_attempt_count" to (placedCount + wrongPlacementCount),
                    "story_page_count" to storyPages.size
                )
                Analytics.track(AnalyticsEvent.GameComplete, completionProperties)
                Analytics.track(AnalyticsEvent.StorySceneComplete, completionProperties)
                if (before.selectedStoryPageIndex == storyPages.lastIndex) {
                    Analytics.track(AnalyticsEvent.StoryComplete, completionProperties)
                }
            }
        } else {
            val current = _state.value
            wrongPlacementCount++
            Analytics.track(
                AnalyticsEvent.PiecePlace,
                baseGameProperties(
                    themeId = current.selectedTheme?.id ?: "unknown",
                    pieceCount = current.pieces.size
                ) + mapOf(
                    "result" to "wrong_target",
                    "input_method" to inputMethod,
                    "placed_count" to current.cellFilledBy.size,
                    "wrong_attempt_count" to wrongPlacementCount,
                    "elapsed_seconds" to current.elapsedSeconds
                )
            )
            _state.update { it.copy(wrongDropHint = true) }
            viewModelScope.launch {
                delay(600)
                _state.update { it.copy(wrongDropHint = false) }
            }
        }
    }

    fun handleDragEnd() {
        val result = dragDropState.endDrag()
        if (result != null) {
            tryPlacePiece(result.pieceId, result.targetPieceId, inputMethod = "drag")
        } else {
            dragDropState.cancelDrag()
        }
    }

    fun dismissCelebration() {
        val current = _state.value
        Analytics.track(
            AnalyticsEvent.CelebrationDismiss,
            baseGameProperties(
                current.selectedTheme?.id ?: "unknown",
                current.pieces.size
            )
        )
        _state.update { it.copy(showCelebration = false) }
    }

    fun resetGame() {
        stopTimer()
        nativeAdapter.close()
        _state.update { GameState() }
    }

    fun goToMenu() {
        val current = _state.value
        if (current.phase == GamePhase.GENERATING || current.phase == GamePhase.PLAYING || current.phase == GamePhase.ERROR) {
            Analytics.track(
                AnalyticsEvent.GameQuit,
                baseGameProperties(
                    current.selectedTheme?.id ?: "unknown",
                    current.pieces.size.takeIf { it > 0 } ?: current.pieceCount
                ) + mapOf(
                    "phase" to current.phase.name.lowercase(),
                    "placed_count" to current.cellFilledBy.size,
                    "wrong_attempt_count" to wrongPlacementCount,
                    "elapsed_seconds" to current.elapsedSeconds
                )
            )
        }
        stopTimer()
        nativeAdapter.close()
        dragDropState.cancelDrag()
        dragDropState.clearSelection()
        _state.update {
            it.copy(
                phase = GamePhase.MENU,
                pieces = emptyList(),
                cellFilledBy = mutableMapOf(),
                correctPositions = emptyMap(),
                puzzleBitmap = null
            )
        }
    }

    // ── Error ─────────────────────────────────────────────

    private fun setError(message: String, operation: String, errorCode: String) {
        PuzzleLog.e("GameVM", "Error: $message")
        val current = _state.value
        val properties = baseGameProperties(
            current.selectedTheme?.id ?: "unknown",
            current.pieces.size.takeIf { it > 0 } ?: current.pieceCount
        ) + mapOf("phase" to current.phase.name.lowercase())
        Analytics.track(
            AnalyticsEvent.GameError,
            properties + mapOf("operation" to operation, "error_code" to errorCode)
        )
        Analytics.monitorError(
            component = "game",
            operation = operation,
            errorCode = errorCode,
            properties = properties
        )
        stopTimer()
        _state.update { it.copy(phase = GamePhase.ERROR, errorMessage = message, isImageLoading = false) }
    }

    fun retryGame() {
        val current = _state.value
        Analytics.track(
            AnalyticsEvent.GameRetry,
            baseGameProperties(
                current.selectedTheme?.id ?: "unknown",
                current.pieceCount
            ) + mapOf("retry_count" to retryCount + 1)
        )
        pendingParentGameRunId = gameRunId
        retryCount++
        _state.update { it.copy(phase = GamePhase.GENERATING, errorMessage = null, isImageLoading = true) }
        startGame()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // ── Pause / Resume ───────────────────────────────────

    fun pause() {
        if (_state.value.isPaused) return
        val current = _state.value
        Analytics.track(
            AnalyticsEvent.GamePause,
            baseGameProperties(
                current.selectedTheme?.id ?: "unknown",
                current.pieces.size
            ) + mapOf(
                "elapsed_seconds" to current.elapsedSeconds,
                "placed_count" to current.cellFilledBy.size
            )
        )
        stopTimer()
        _state.update { it.copy(isPaused = true) }
    }

    fun resume() {
        if (!_state.value.isPaused) return
        val current = _state.value
        Analytics.track(
            AnalyticsEvent.GameResume,
            baseGameProperties(
                current.selectedTheme?.id ?: "unknown",
                current.pieces.size
            ) + mapOf(
                "elapsed_seconds" to current.elapsedSeconds,
                "placed_count" to current.cellFilledBy.size
            )
        )
        _state.update { it.copy(isPaused = false) }
        startTimer()
    }

    private fun baseGameProperties(themeId: String, pieceCount: Int): Map<String, Any?> {
        val current = _state.value
        return mapOf(
            "game_run_id" to gameRunId,
            "parent_game_run_id" to parentGameRunId,
            "story_id" to current.selectedStoryId,
            "story_page_index" to current.selectedStoryPageIndex,
            "story_page_count" to storyPages.size,
            "theme_id" to themeId,
            "piece_count" to pieceCount,
            "source" to gameSource,
            "retry_count" to retryCount
        )
    }

    private fun beginGameRun(source: String) {
        parentGameRunId = pendingParentGameRunId
        if (parentGameRunId == null) retryCount = 0
        pendingParentGameRunId = null
        gameRunId = Analytics.newTraceId("game")
        gameSource = source
        prepareStartedAt = TimeSource.Monotonic.markNow()
        wrongPlacementCount = 0
        reportedProgressMilestones.clear()
    }

    private fun reportProgressMilestones(placedCount: Int, totalCount: Int, inputMethod: String) {
        val progressPercent = (placedCount * 100 / totalCount.coerceAtLeast(1)).coerceIn(0, 100)
        listOf(25, 50, 75, 100).forEach { milestone ->
            if (progressPercent >= milestone && reportedProgressMilestones.add(milestone)) {
                val current = _state.value
                Analytics.track(
                    AnalyticsEvent.GameProgress,
                    baseGameProperties(
                        current.selectedTheme?.id ?: "unknown",
                        totalCount
                    ) + mapOf(
                        "progress_percent" to milestone,
                        "placed_count" to placedCount,
                        "wrong_attempt_count" to wrongPlacementCount,
                        "input_method" to inputMethod,
                        "elapsed_seconds" to current.elapsedSeconds
                    )
                )
            }
        }
    }

    private fun trackGameTiming(
        operation: String,
        durationMs: Long,
        properties: Map<String, Any?> = emptyMap()
    ) {
        val current = _state.value
        Analytics.monitorTiming(
            component = "game",
            operation = operation,
            durationMs = durationMs,
            properties = baseGameProperties(
                current.selectedTheme?.id ?: "unknown",
                current.pieceCount
            ) + properties
        )
    }

    private data class GeneratedGameData(
        val pieces: List<com.puzzle.game.engine.model.PuzzlePiece>,
        val bitmap: ImageBitmap,
        val pieceBitmaps: Map<String, ImageBitmap>,
        val imageWidth: Int,
        val imageHeight: Int
    )
}
