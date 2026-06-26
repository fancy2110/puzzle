package com.puzzle.game.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import kotlin.random.Random

object PuzzlePictureGenerator {

    fun generate(theme: ThemeData, width: Int = 800, height: Int = 600): ImageBitmap {
        val bitmap = ImageBitmap(width, height)
        val canvas = Canvas(bitmap)
        val rng = Random(theme.id.hashCode())

        drawBackground(canvas, width, height, theme)

        when (theme.id) {
            "demo" -> drawDemoPicture(canvas, width, height, theme, rng)
            "cat" -> drawFriendlyCat(canvas, width, height, theme, rng)
            "balloon" -> drawBalloons(canvas, width, height, theme, rng)
            "ocean" -> drawOcean(canvas, width, height, theme, rng)
            "forest" -> drawForest(canvas, width, height, theme, rng)
            "space" -> drawSpace(canvas, width, height, theme, rng)
            "flower" -> drawFlowers(canvas, width, height, theme, rng)
            else -> drawStorybookScene(canvas, width, height, theme, rng)
        }

        return bitmap
    }

    private fun drawDemoPicture(canvas: Canvas, w: Int, h: Int, theme: ThemeData, rng: Random) {
        val sky = Paint().apply { color = Color(0xFFBFE3F2) }
        val hill = Paint().apply { color = Color(0xFF8BC39A) }
        val roof = Paint().apply { color = theme.accent }
        val house = Paint().apply { color = Color(0xFFFFF2D6) }
        val door = Paint().apply { color = theme.primary }

        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), sky)
        canvas.drawOval(-80f, h * 0.55f, w * 0.7f, h * 1.1f, hill)
        canvas.drawOval(w * 0.35f, h * 0.5f, w + 80f, h * 1.05f, Paint().apply { color = Color(0xFFA8D5A3) })
        drawSunAt(canvas, w - 105f, 88f, 42f)

        val cx = w * 0.52f
        val cy = h * 0.56f
        canvas.drawRect(cx - 95f, cy - 20f, cx + 95f, cy + 115f, house)
        val roofPath = Path().apply {
            moveTo(cx - 120f, cy - 20f)
            lineTo(cx, cy - 115f)
            lineTo(cx + 120f, cy - 20f)
            close()
        }
        canvas.drawPath(roofPath, roof)
        canvas.drawRect(cx - 22f, cy + 42f, cx + 22f, cy + 115f, door)
        canvas.drawRect(cx - 72f, cy + 18f, cx - 35f, cy + 54f, Paint().apply { color = Color.White })
        canvas.drawRect(cx + 38f, cy + 18f, cx + 75f, cy + 54f, Paint().apply { color = Color.White })

        repeat(14) {
            val fx = 40f + rng.nextFloat() * (w - 80f)
            val fy = h * 0.70f + rng.nextFloat() * h * 0.22f
            canvas.drawCircle(
                Offset(fx, fy),
                7f + rng.nextFloat() * 5f,
                Paint().apply { color = listOf(theme.accent, Color(0xFFFFD166), Color.White).random(rng) }
            )
        }
    }

    private fun drawBackground(canvas: Canvas, w: Int, h: Int, theme: ThemeData) {
        val paint = Paint().apply { color = theme.secondary }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
    }

    private fun drawStorybookScene(canvas: Canvas, w: Int, h: Int, theme: ThemeData, rng: Random) {
        val sky = Paint().apply { color = theme.secondary }
        val hill = Paint().apply { color = theme.primary.copy(alpha = 0.34f) }
        val hill2 = Paint().apply { color = theme.primary.copy(alpha = 0.22f) }
        val ink = Paint().apply { color = theme.primary }
        val accent = Paint().apply { color = theme.accent }
        val warm = Paint().apply { color = Color(0xFFFFF6DF) }

        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), sky)
        canvas.drawOval(-w * 0.20f, h * 0.50f, w * 0.74f, h * 1.08f, hill2)
        canvas.drawOval(w * 0.28f, h * 0.46f, w * 1.18f, h * 1.08f, hill)

        val isNight = theme.id.contains("red") ||
                theme.id.contains("sleeping") ||
                theme.id.contains("mermaid") ||
                theme.id.contains("snow")
        if (isNight) {
            repeat(24) {
                val sx = rng.nextFloat() * w
                val sy = 24f + rng.nextFloat() * h * 0.42f
                canvas.drawCircle(Offset(sx, sy), 2f + rng.nextFloat() * 3f, Paint().apply { color = Color.White.copy(alpha = 0.72f) })
            }
            canvas.drawCircle(Offset(w * 0.82f, h * 0.16f), 34f, Paint().apply { color = Color(0xFFFFE7A6) })
        } else {
            drawSunAt(canvas, w * 0.82f, h * 0.16f, 34f)
        }

        val path = Path().apply {
            moveTo(w * 0.42f, h.toFloat())
            cubicTo(w * 0.36f, h * 0.78f, w * 0.48f, h * 0.62f, w * 0.55f, h * 0.46f)
            cubicTo(w * 0.62f, h * 0.62f, w * 0.72f, h * 0.78f, w * 0.70f, h.toFloat())
            close()
        }
        canvas.drawPath(path, Paint().apply { color = Color(0xFFFFF0C9).copy(alpha = 0.78f) })

        drawStoryLandmark(canvas, w, h, theme, ink, accent, warm)
        drawStoryCharacters(canvas, w, h, theme, rng)
        drawStoryPlants(canvas, w, h, theme, rng)
    }

    private fun drawStoryLandmark(
        canvas: Canvas,
        w: Int,
        h: Int,
        theme: ThemeData,
        ink: Paint,
        accent: Paint,
        warm: Paint
    ) {
        val baseX = w * 0.56f
        val baseY = h * 0.48f
        if (theme.id.contains("cinderella") || theme.id.contains("snow") || theme.id.contains("sleeping") || theme.id.contains("beanstalk")) {
            canvas.drawRect(baseX - 84f, baseY - 30f, baseX + 84f, baseY + 118f, warm)
            repeat(3) { i ->
                val tx = baseX - 70f + i * 70f
                canvas.drawRect(tx - 18f, baseY - 92f, tx + 18f, baseY + 118f, warm)
                val roof = Path().apply {
                    moveTo(tx - 26f, baseY - 92f)
                    lineTo(tx, baseY - 132f)
                    lineTo(tx + 26f, baseY - 92f)
                    close()
                }
                canvas.drawPath(roof, accent)
            }
            canvas.drawRect(baseX - 16f, baseY + 54f, baseX + 16f, baseY + 118f, ink)
        } else if (theme.id.contains("pigs") || theme.id.contains("hansel") || theme.id.contains("red")) {
            canvas.drawRect(baseX - 86f, baseY + 8f, baseX + 86f, baseY + 118f, warm)
            val roof = Path().apply {
                moveTo(baseX - 108f, baseY + 8f)
                lineTo(baseX, baseY - 78f)
                lineTo(baseX + 108f, baseY + 8f)
                close()
            }
            canvas.drawPath(roof, accent)
            canvas.drawRect(baseX - 18f, baseY + 58f, baseX + 18f, baseY + 118f, ink)
        } else {
            canvas.drawOval(baseX - 86f, baseY - 36f, baseX + 86f, baseY + 132f, warm)
            canvas.drawCircle(Offset(baseX, baseY + 18f), 36f, accent)
            canvas.drawRect(baseX - 14f, baseY + 50f, baseX + 14f, baseY + 132f, ink)
        }
    }

    private fun drawStoryCharacters(canvas: Canvas, w: Int, h: Int, theme: ThemeData, rng: Random) {
        val body = Paint().apply { color = theme.accent }
        val head = Paint().apply { color = Color(0xFFFFD9B0) }
        val dark = Paint().apply { color = theme.primary }
        val cx = w * 0.34f
        val cy = h * 0.67f
        canvas.drawCircle(Offset(cx, cy - 54f), 24f, head)
        canvas.drawOval(cx - 30f, cy - 30f, cx + 30f, cy + 58f, body)
        canvas.drawCircle(Offset(cx - 8f, cy - 58f), 3f, dark)
        canvas.drawCircle(Offset(cx + 8f, cy - 58f), 3f, dark)

        val companionCount = if (theme.id.contains("pigs")) 3 else if (theme.id.contains("snow")) 4 else 1
        repeat(companionCount) { index ->
            val px = w * (0.18f + index * 0.08f)
            val py = h * (0.78f + (index % 2) * 0.04f)
            canvas.drawOval(px - 18f, py - 14f, px + 18f, py + 16f, Paint().apply { color = theme.primary.copy(alpha = 0.82f) })
            canvas.drawCircle(Offset(px + 10f, py - 12f), 11f, Paint().apply { color = theme.accent.copy(alpha = 0.90f) })
        }
    }

    private fun drawStoryPlants(canvas: Canvas, w: Int, h: Int, theme: ThemeData, rng: Random) {
        repeat(18) {
            val x = 24f + rng.nextFloat() * (w - 48f)
            val y = h * 0.70f + rng.nextFloat() * h * 0.24f
            val r = 5f + rng.nextFloat() * 9f
            canvas.drawCircle(Offset(x, y), r, Paint().apply { color = listOf(theme.primary, theme.accent, Color.White).random(rng).copy(alpha = 0.76f) })
        }
    }

    private fun drawFriendlyCat(canvas: Canvas, w: Int, h: Int, theme: ThemeData, rng: Random) {
        val cx = w / 2f
        val cy = h / 2f
        val catPaint = Paint().apply { color = theme.accent }
        val darkPaint = Paint().apply { color = theme.primary }
        val greenPaint = Paint().apply { color = Color(0xFF81C784) }

        canvas.drawCircle(Offset(cx, cy - 20), 50f, catPaint)
        canvas.drawCircle(Offset(cx - 30, cy - 60), 20f, catPaint)
        canvas.drawCircle(Offset(cx + 30, cy - 60), 20f, catPaint)
        canvas.drawCircle(Offset(cx - 30, cy - 60), 10f, darkPaint)
        canvas.drawCircle(Offset(cx + 30, cy - 60), 10f, darkPaint)
        canvas.drawCircle(Offset(cx - 12, cy - 10), 6f, darkPaint)
        canvas.drawCircle(Offset(cx + 12, cy - 10), 6f, darkPaint)
        canvas.drawOval(cx - 5, cy, cx + 5, cy + 8, darkPaint)

        drawSunAt(canvas, w - 60f, 60f, 30f)
        repeat(4) {
            canvas.drawCircle(
                    Offset(rng.nextFloat() * w, rng.nextFloat() * h * 0.6f),
                    4f,
                    greenPaint
            )
        }
    }

    private fun drawBalloons(canvas: Canvas, w: Int, h: Int, theme: ThemeData, rng: Random) {
        val colors = listOf(Color.Red, theme.accent, Color.Yellow, Color.Blue)
        val xPositions = listOf(w * 0.2f, w * 0.5f, w * 0.75f, w * 0.4f)
        val yPositions = listOf(h * 0.4f, h * 0.25f, h * 0.35f, h * 0.55f)
        val grayLine =
                Paint().apply {
                    color = Color.Gray
                    strokeWidth = 2f
                }

        for (i in xPositions.indices) {
            val bx = xPositions[i]
            val by = yPositions[i]
            val color = colors[i]
            val paint = Paint().apply { this.color = color }
            canvas.drawOval(bx - 25, by - 35, bx + 25, by + 20, paint)
            canvas.drawLine(Offset(bx, by + 20f), Offset(bx, by + 60f), grayLine)
            val hlPaint =
                    Paint().apply {
                        this.color = color
                        alpha = 0.6f
                    }
            canvas.drawOval(bx - 8, by - 5, bx + 8, by + 8, hlPaint)
        }
    }

    private fun drawOcean(canvas: Canvas, w: Int, h: Int, theme: ThemeData, rng: Random) {
        val fishPositions =
                listOf(
                        Offset(w * 0.3f, h * 0.5f),
                        Offset(w * 0.6f, h * 0.35f),
                        Offset(w * 0.5f, h * 0.7f),
                        Offset(w * 0.75f, h * 0.55f)
                )
        val fishColors = listOf(theme.accent, Color(0xFFFF9800), Color(0xFFE91E63), theme.primary)

        for ((i, pos) in fishPositions.withIndex()) {
            val fp = Paint().apply { color = fishColors[i] }
            canvas.drawOval(pos.x - 20, pos.y - 10, pos.x + 20, pos.y + 10, fp)
            canvas.drawOval(pos.x - 30, pos.y - 12, pos.x - 10, pos.y + 5, fp)
            canvas.drawCircle(
                    Offset(pos.x + 12, pos.y - 2),
                    2.5f,
                    Paint().apply { color = Color.White }
            )
        }

        repeat(8) {
            val bx = rng.nextFloat() * w
            val by = h * 0.6f + rng.nextFloat() * h * 0.4f
            val alpha = (0.1f + rng.nextFloat() * 0.3f)
            canvas.drawCircle(
                    Offset(bx, by),
                    6f,
                    Paint().apply {
                        color = Color.White
                        this.alpha = alpha
                    }
            )
        }
    }

    private fun drawForest(canvas: Canvas, w: Int, h: Int, theme: ThemeData, rng: Random) {
        val treePaint = Paint().apply { color = theme.primary }
        val trunkPaint =
                Paint().apply {
                    color = Color(0xFF8D6E63)
                    strokeWidth = 4f
                }
        val brownPaint = Paint().apply { color = Color(0xFFA1887F) }

        val trees =
                listOf(
                        Offset(w * 0.25f, h * 0.6f),
                        Offset(w * 0.5f, h * 0.5f),
                        Offset(w * 0.75f, h * 0.55f)
                )
        for (t in trees) {
            canvas.drawCircle(Offset(t.x, t.y - 30), 28f, treePaint)
            canvas.drawCircle(Offset(t.x - 12, t.y - 15), 22f, treePaint)
            canvas.drawCircle(Offset(t.x + 12, t.y - 15), 22f, treePaint)
            canvas.drawLine(t, Offset(t.x, t.y + 40), trunkPaint)
        }

        val animalPaint = Paint().apply { color = theme.accent }
        canvas.drawOval(w * 0.4f, h * 0.75f, w * 0.4f + 30, h * 0.75f + 15, animalPaint)
        canvas.drawCircle(Offset(w * 0.42f, h * 0.73f), 3f, Paint().apply { color = Color.White })
        canvas.drawCircle(Offset(w * 0.47f, h * 0.73f), 3f, Paint().apply { color = Color.White })

        drawSunAt(canvas, w - 50f, 50f, 25f)
    }

    private fun drawSpace(canvas: Canvas, w: Int, h: Int, theme: ThemeData, rng: Random) {
        repeat(30) {
            val sx = rng.nextFloat() * w
            val sy = rng.nextFloat() * h * 0.7f
            val size = 1.5f + rng.nextFloat() * 3f
            val color = if (rng.nextBoolean()) Color.White else theme.accent
            canvas.drawCircle(Offset(sx, sy), size, Paint().apply { this.color = color })
        }

        val rocketX = w / 2f
        val rocketY = h / 2f
        val rocketP = Paint().apply { color = Color.White }
        val tipP = Paint().apply { color = theme.accent }

        val path =
                Path().apply {
                    moveTo(rocketX, rocketY - 40)
                    lineTo(rocketX + 15, rocketY + 10)
                    lineTo(rocketX + 15, rocketY + 25)
                    lineTo(rocketX - 15, rocketY + 25)
                    lineTo(rocketX - 15, rocketY + 10)
                    close()
                }
        canvas.drawPath(path, rocketP)
        canvas.drawCircle(
                Offset(rocketX - 6, rocketY),
                4f,
                Paint().apply { color = Color(0xFF42A5F5) }
        )
    }

    private fun drawFlowers(canvas: Canvas, w: Int, h: Int, theme: ThemeData, rng: Random) {
        val flowerPositions =
                listOf(
                        Offset(w * 0.2f, h * 0.5f),
                        Offset(w * 0.45f, h * 0.35f),
                        Offset(w * 0.7f, h * 0.55f),
                        Offset(w * 0.35f, h * 0.75f),
                        Offset(w * 0.6f, h * 0.7f)
                )
        val stemPaint =
                Paint().apply {
                    color = Color(0xFF4CAF50)
                    strokeWidth = 3f
                }

        for (pos in flowerPositions) {
            canvas.drawLine(Offset(pos.x, pos.y - 10), Offset(pos.x, pos.y + 40), stemPaint)
            val petalColor =
                    listOf(theme.primary, theme.accent, Color(0xFFFFAB91), Color(0xFFCE93D8))
                            .random(rng)
            val petalPaint = Paint().apply { color = petalColor }
            for (angle in listOf(0, 72, 144, 216, 288)) {
                val rad = angle * kotlin.math.PI / 180.0
                val cx = (pos.x + 15 * kotlin.math.cos(rad)).toFloat()
                val cy = (pos.y - 20 + 15 * kotlin.math.sin(rad)).toFloat()
                canvas.drawCircle(Offset(cx, cy), 8f, petalPaint)
            }
            canvas.drawCircle(Offset(pos.x, pos.y - 20), 6f, Paint().apply { color = Color.Yellow })
        }

        val bfPaint = Paint().apply { color = theme.accent }
        val bx = w * 0.65f
        val by = h * 0.3f
        canvas.drawOval(bx - 10, by - 8, bx, by + 4, bfPaint)
        canvas.drawOval(bx, by - 8, bx + 10, by + 4, bfPaint)
    }

    private fun drawSunAt(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        canvas.drawCircle(Offset(cx, cy), radius, Paint().apply { color = Color(0xFFFFF176) })
    }
}
