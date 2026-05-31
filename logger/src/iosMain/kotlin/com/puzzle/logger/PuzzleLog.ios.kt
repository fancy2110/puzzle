package com.puzzle.logger

import platform.Foundation.NSLog

/**
 * iOS: delegates to [NSLog] which outputs to the system console.
 *
 * NSLog format: "timestamp AppName[pid:tid] message"
 * Thread-safe — can be called from any coroutine context.
 */
actual object PuzzleLog {

    actual fun v(tag: String, msg: String) = log("V", tag, msg)
    actual fun d(tag: String, msg: String) = log("D", tag, msg)
    actual fun i(tag: String, msg: String) = log("I", tag, msg)

    actual fun w(tag: String, msg: String) = log("W", tag, msg)
    actual fun w(tag: String, msg: String, tr: Throwable) = log("W", tag, "$msg | ${tr.stackTraceToString()}")

    actual fun e(tag: String, msg: String) = log("E", tag, msg)
    actual fun e(tag: String, msg: String, tr: Throwable) = log("E", tag, "$msg | ${tr.stackTraceToString()}")

    private fun log(level: String, tag: String, msg: String) {
        NSLog("[%s] %s: %s", level, tag, msg)
    }
}
