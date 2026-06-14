package com.puzzle.game.game

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzle.game.ai.AIImageGenerator
import com.puzzle.logger.PuzzleLog
import com.puzzle.game.data.PuzzlePictureGenerator
import com.puzzle.game.data.ThemeData
import com.puzzle.game.data.ThemePresets
import com.puzzle.game.decodeToImageBitmap
import com.puzzle.game.platformCacheDir
import com.puzzle.game.engine.PuzzleEngine
import com.puzzle.game.engine.PieceBitmapGenerator
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

class GameViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    private val engine = PuzzleEngine()
    private val nativeAdapter = NativeSplitAdapter()
    private val aiGenerator = AIImageGenerator()
    val dragDropState = DragDropState()

    val themes: List<ThemeData> = ThemePresets.themes

    private var timerJob: Job? = null

    fun selectTheme(themeId: String) {
        val theme = ThemePresets.getById(themeId)
        _state.update { it.copy(selectedTheme = theme) }
    }

    fun selectDifficulty(difficulty: GameDifficulty) {
        _state.update { it.copy(difficulty = difficulty) }
    }

    /**
     * Start a game. If the selected theme has an assetFile (native image),
     * route to the Rust native splitter. Otherwise use the Kotlin procedural path.
     * All I/O and computation runs off the main thread to avoid ANR.
     */
    fun startGame() {
        val currentState = _state.value
        val theme = currentState.selectedTheme ?: ThemePresets.themes.first()
        val pieceCount = currentState.difficulty.pieceCount
        val assetFile = theme.assetFile

        PuzzleLog.i("GameVM", "Starting game: theme=${theme.id} difficulty=${currentState.difficulty.name} pieces=$pieceCount asset=${assetFile ?: "procedural"}")

        // Set loading state immediately, then do blocking work in coroutine
        resetForNewGame()

        viewModelScope.launch {
            try {
                // If theme has a built-in asset image, try native Rust splitter first
                if (assetFile != null) {
                    val bytes = withContext(Dispatchers.Default) {
                        com.puzzle.game.data.AssetLoader.readBytes(assetFile)
                    }
                    if (bytes != null) {
                        PuzzleLog.d("GameVM", "Loaded asset: $assetFile (${bytes.size} bytes)")
                        startGameWithImageInternal(bytes, pieceCount)
                        return@launch
                    }
                    PuzzleLog.w("GameVM", "Asset $assetFile not found, falling back to procedural")
                }

                // Kotlin procedural path
                val gameData = withContext(Dispatchers.Default) {
                    val puzzleBitmap = PuzzlePictureGenerator.generate(theme, 800, 600)
                    engine.loadImage(puzzleBitmap.width, puzzleBitmap.height)
                    engine.splitImage(pieceCount = pieceCount, blockSize = 64)
                    val pieces = engine.shufflePieces()
                    GeneratedGameData(
                        pieces = pieces,
                        bitmap = puzzleBitmap,
                        pieceBitmaps = PieceBitmapGenerator.generate(puzzleBitmap, pieces, 64),
                        imageWidth = puzzleBitmap.width,
                        imageHeight = puzzleBitmap.height
                    )
                }

                applyNewGame(
                    pieces = gameData.pieces,
                    bitmap = gameData.bitmap,
                    pieceBitmaps = gameData.pieceBitmaps,
                    imageWidth = gameData.imageWidth,
                    imageHeight = gameData.imageHeight,
                    blockSize = 64
                )
                startTimer()
            } catch (e: Exception) {
                setError("生成拼图失败: ${e.message ?: "未知错误"}")
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
        val pieceCount = _state.value.difficulty.pieceCount
        resetForNewGame()
        viewModelScope.launch {
            startGameWithImageInternal(imageBytes, pieceCount)
        }
    }

    /**
     * Start a game using AI image generation.
     * Attempts to generate via the configured AI provider,
     * falling back to procedural generation if AI is unavailable.
     */
    fun startAIGame() {
        val currentState = _state.value
        val theme = currentState.selectedTheme ?: ThemePresets.themes.first()
        val pieceCount = currentState.difficulty.pieceCount
        val prompt = theme.description

        PuzzleLog.i("GameVM", "Starting AI game: theme=${theme.id} prompt='$prompt' pieces=$pieceCount")

        resetForNewGame()

        viewModelScope.launch {
            try {
                val generated = withContext(Dispatchers.Default) {
                    aiGenerator.generate(prompt)
                }

                // If AI returned an image URL, download and split
                if (generated.imageUrl != null) {
                    val imageBytes = withContext(Dispatchers.Default) {
                        downloadImage(generated.imageUrl)
                    }
                    if (imageBytes != null) {
                        startGameWithImageInternal(imageBytes, pieceCount)
                        return@launch
                    }
                }

                // If AI returned a local file path, load and split
                if (generated.localPath != null) {
                    val bytes = withContext(Dispatchers.Default) {
                        com.puzzle.game.data.AssetLoader.readBytes(generated.localPath)
                    }
                    if (bytes != null) {
                        startGameWithImageInternal(bytes, pieceCount)
                        return@launch
                    }
                }

                // AI returned no usable image — fall back to procedural
                startGuardedProcedural(theme, pieceCount)
            } catch (e: Exception) {
                startGuardedProcedural(theme, pieceCount, e.message)
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
            }
            startProceduralGame(theme, pieceCount)
        } catch (e: Exception) {
            setError("拼图生成失败: ${e.message ?: "未知错误"}")
        }
    }

    /**
     * Internal: assumes already running in a coroutine.
     * Called from startGame() and startGameWithImage().
     */
    private suspend fun startGameWithImageInternal(imageBytes: ByteArray, pieceCount: Int) {
        val imgSize = "${imageBytes.size / 1024}KB"
        val success = withContext(Dispatchers.Default) {
            nativeAdapter.loadAndSplit(imageBytes, pieceCount, 64, platformCacheDir())
        }
        if (!success) {
            PuzzleLog.w("GameVM", "Native split failed ($imgSize), falling back to Kotlin engine")
            // Fallback to Kotlin path — native library unavailable or failed
            val theme = _state.value.selectedTheme ?: ThemePresets.themes.first()
            val gameData = withContext(Dispatchers.Default) {
                val puzzleBitmap = PuzzlePictureGenerator.generate(theme, 800, 600)
                engine.loadImage(puzzleBitmap.width, puzzleBitmap.height)
                engine.splitImage(pieceCount = pieceCount, blockSize = 64)
                val pieces = engine.shufflePieces()
                GeneratedGameData(
                    pieces = pieces,
                    bitmap = puzzleBitmap,
                    pieceBitmaps = PieceBitmapGenerator.generate(puzzleBitmap, pieces, 64),
                    imageWidth = puzzleBitmap.width,
                    imageHeight = puzzleBitmap.height
                )
            }
            applyNewGame(
                pieces = gameData.pieces,
                bitmap = gameData.bitmap,
                pieceBitmaps = gameData.pieceBitmaps,
                imageWidth = gameData.imageWidth,
                imageHeight = gameData.imageHeight,
                blockSize = 64
            )
            startTimer()
            return
        }

        val bitmap = decodeToImageBitmap(imageBytes)
            ?: PuzzlePictureGenerator.generate(
                _state.value.selectedTheme ?: ThemePresets.themes.first(),
                800, 600
            )

        val (imgW, imgH) = nativeAdapter.imageSize
        val nativePieceBitmaps = nativeAdapter.pieceBitmaps.ifEmpty {
            PieceBitmapGenerator.generate(bitmap, nativeAdapter.pieces, 64)
        }
        applyNewGame(
            pieces = nativeAdapter.pieces,
            bitmap = bitmap,
            pieceBitmaps = nativePieceBitmaps,
            imageWidth = imgW,
            imageHeight = imgH,
            blockSize = 64,
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
        val gameData = withContext(Dispatchers.Default) {
            val puzzleBitmap = PuzzlePictureGenerator.generate(theme, 800, 600)
            engine.loadImage(puzzleBitmap.width, puzzleBitmap.height)
            engine.splitImage(pieceCount = pieceCount, blockSize = 64)
            val pieces = engine.shufflePieces()
            GeneratedGameData(
                pieces = pieces,
                bitmap = puzzleBitmap,
                pieceBitmaps = PieceBitmapGenerator.generate(puzzleBitmap, pieces, 64),
                imageWidth = puzzleBitmap.width,
                imageHeight = puzzleBitmap.height
            )
        }

        applyNewGame(
            pieces = gameData.pieces,
            bitmap = gameData.bitmap,
            pieceBitmaps = gameData.pieceBitmaps,
            imageWidth = gameData.imageWidth,
            imageHeight = gameData.imageHeight,
            blockSize = 64
        )
        startTimer()
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
        val gridCols = (imageWidth / blockSize) + 1
        val gridRows = (imageHeight / blockSize) + 1

        val correctPositions = customPositions ?: run {
            val map = mutableMapOf<String, Pair<Int, Int>>()
            for (piece in pieces) {
                if (piece.items.isNotEmpty()) {
                    val center = piece.items[piece.items.size / 2]
                    map[piece.id] = Pair(center.y, center.x)
                }
            }
            map
        }

        // Log piece dimensions summary
        if (pieces.isNotEmpty()) {
            val dims = pieces.joinToString(", ") { p ->
                "${p.id}: ${p.pixels.width}×${p.pixels.height}px@(${p.pixels.left},${p.pixels.top}) [${p.items.size}b]"
            }
            PuzzleLog.i("GameVM", "New game ready: ${imageWidth}×${imageHeight}px grid=${gridCols}×${gridRows} bs=$blockSize pieces=${pieces.size}")
            PuzzleLog.d("GameVM", "Piece dimensions: $dims")
        }

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

    // ── Gameplay ─────────────────────────────────────────

    fun tryPlacePiece(pieceId: String, targetPieceId: String) {
        val isCorrectTarget = pieceId == targetPieceId

        if (isCorrectTarget) {
            _state.update { current ->
                val newCellFilled = current.cellFilledBy.toMutableMap()
                newCellFilled[pieceId] = pieceId

                val allPlaced = current.pieces.all { piece ->
                    newCellFilled[piece.id] == piece.id
                }

                if (allPlaced) {
                    stopTimer()
                    PuzzleLog.i("GameVM", "Puzzle completed! pieces=${current.pieces.size} time=${current.elapsedSeconds}s")
                    current.copy(
                        cellFilledBy = newCellFilled,
                        phase = GamePhase.COMPLETED,
                        showCelebration = true
                    )
                } else {
                    current.copy(cellFilledBy = newCellFilled)
                }
            }
        } else {
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
            tryPlacePiece(result.pieceId, result.targetPieceId)
        } else {
            dragDropState.cancelDrag()
        }
    }

    fun dismissCelebration() {
        _state.update { it.copy(showCelebration = false) }
    }

    fun resetGame() {
        stopTimer()
        nativeAdapter.close()
        _state.update { GameState() }
    }

    fun goToMenu() {
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

    private fun setError(message: String) {
        PuzzleLog.e("GameVM", "Error: $message")
        stopTimer()
        _state.update { it.copy(phase = GamePhase.ERROR, errorMessage = message, isImageLoading = false) }
    }

    fun retryGame() {
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
        stopTimer()
        _state.update { it.copy(isPaused = true) }
    }

    fun resume() {
        _state.update { it.copy(isPaused = false) }
        startTimer()
    }

    private data class GeneratedGameData(
        val pieces: List<com.puzzle.game.engine.model.PuzzlePiece>,
        val bitmap: ImageBitmap,
        val pieceBitmaps: Map<String, ImageBitmap>,
        val imageWidth: Int,
        val imageHeight: Int
    )
}
