package com.ikatyan.sonic.commands

import com.ikatyan.sonic.CommandInvalidException

fun buildCommandInvalidException(msg: String): CommandInvalidException {
    return CommandInvalidException(msg)
}

fun buildCommandInvalidException(msg: String, command: String): CommandInvalidException {
    return CommandInvalidException("$msg [$command]")
}

fun checkStringArg(s: String?, argName: String, hasQuote: Boolean = false) {
    s?.run {
        if (isBlank()) throw IllegalArgumentException("$argName argument must be not blank strings.")
        if (!hasQuote) {
            if (s.any { it.isWhitespace() })
                throw IllegalArgumentException("$argName argument must be not contained any whitespace.")
        }
    }
}

fun quote(s: String): String {
    return "\"$s\""
}