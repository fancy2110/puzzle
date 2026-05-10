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
            "cat" -> drawFriendlyCat(canvas, width, height, theme, rng)
            "balloon" -> drawBalloons(canvas, width, height, theme, rng)
            "ocean" -> drawOcean(canvas, width, height, theme, rng)
            "forest" -> drawForest(canvas, width, height, theme, rng)
            "space" -> drawSpace(canvas, width, height, theme, rng)
            "flower" -> drawFlowers(canvas, width, height, theme, rng)
        }

        return bitmap
    }

    private fun drawBackground(canvas: Canvas, w: Int, h: Int, theme: ThemeData) {
        val paint = Paint().apply { color = theme.secondary }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
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
