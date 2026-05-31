package com.puzzle.logger

import android.util.Log as AndroidLog

actual object PuzzleLog {

    actual fun v(tag: String, msg: String) {
        AndroidLog.v(tag, msg)
    }

    actual fun d(tag: String, msg: String) {
        AndroidLog.d(tag, msg)
    }

    actual fun i(tag: String, msg: String) {
        AndroidLog.i(tag, msg)
    }

    actual fun w(tag: String, msg: String) {
        AndroidLog.w(tag, msg)
    }

    actual fun w(tag: String, msg: String, tr: Throwable) {
        AndroidLog.w(tag, msg, tr)
    }

    actual fun e(tag: String, msg: String) {
        AndroidLog.e(tag, msg)
    }

    actual fun e(tag: String, msg: String, tr: Throwable) {
        AndroidLog.e(tag, msg, tr)
    }
}
