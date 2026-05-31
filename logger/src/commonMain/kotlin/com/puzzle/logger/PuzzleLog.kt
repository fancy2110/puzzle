package com.puzzle.logger

/**
 * Cross-platform logger — mirrors android.util.Log API.
 *
 * Each platform provides its own [actual] implementation:
 * - Android: delegates to [android.util.Log]
 * - iOS:     delegates to [platform.Foundation.NSLog]
 *
 * Usage in commonMain:
 * ```
 * PuzzleLog.d("GameVM", "Generating puzzle with ${pieces.size} pieces")
 * PuzzleLog.e("Network", "Download failed", exception)
 * ```
 */
expect object PuzzleLog {

    /** VERBOSE — fine-grained debug info, stripped in release. */
    fun v(tag: String, msg: String)

    /** DEBUG — developer-level diagnostics. */
    fun d(tag: String, msg: String)

    /** INFO — noteworthy runtime events. */
    fun i(tag: String, msg: String)

    /** WARN — recoverable issues, degraded behaviour. */
    fun w(tag: String, msg: String)

    /** WARN with exception. */
    fun w(tag: String, msg: String, tr: Throwable)

    /** ERROR — non-fatal failures, user-visible problems. */
    fun e(tag: String, msg: String)

    /** ERROR with exception. */
    fun e(tag: String, msg: String, tr: Throwable)
}
