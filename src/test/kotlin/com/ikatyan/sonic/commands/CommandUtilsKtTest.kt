package com.ikatyan.sonic.commands

import com.ikatyan.sonic.CommandInvalidException
import io.kotlintest.specs.StringSpec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow

class CommandUtilsKtTest : StringSpec() {

    init {
        "buildCommandInvalidException1" {
            buildCommandInvalidException("error").message shouldBe CommandInvalidException("error").message
        }

        "buildCommandInvalidException2" {
            buildCommandInvalidException("error", "TRIGGER backup /hoge").message shouldBe
                    CommandInvalidException("error [TRIGGER backup /hoge]").message
        }

        "checkStringArg" {
            val arg1 = "a"
            shouldNotThrow<IllegalArgumentException> { checkStringArg(arg1, "arg1") }

            val arg2 = "a a"
            shouldThrow<IllegalArgumentException> { checkStringArg(arg2, "arg2") }

            val arg3 = ""
            shouldThrow<IllegalArgumentException> { checkStringArg(arg3, "arg3") }

            val arg4 = "a a"
            shouldNotThrow<IllegalArgumentException> { checkStringArg(arg4, "arg4", true) }
        }

        "quote" {
            quote("hoge") shouldBe "\"hoge\""
        }
    }

}