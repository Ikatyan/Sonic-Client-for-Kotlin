package com.ikatyan.sonic

import com.ikatyan.sonic.commands.*
import java.net.InetAddress
import java.net.Socket

class SonicChannelBuilder {
    var host = "localhost"
    var port: Int = 1491
    var password = "SecretPassword"
    var timeout = 0
}

class SonicChannel(builder: SonicChannelBuilder){
    val host = builder.host
    val port = builder.port
    val password = builder.password
    val timeout = builder.timeout
    val inetAddress = InetAddress.getByName(host)

    constructor(init: SonicChannelBuilder.() -> Unit)
            : this(SonicChannelBuilder().also { init(it) })

    private fun createCommandImpl(): CommandImpl {
        return Socket(inetAddress, port).apply {
            soTimeout = timeout
        }.let { CommandImpl(it) }
    }

    private suspend fun <T> execCommand(mode: Mode, command: Command, callback: suspend () -> T): T {
        val connectedResponse = command.readLineAsync().await().trim().split(' ')
        if(connectedResponse[0] != "CONNECTED") throw NotSonicProtocolException("This connection is not SonicProtocol.")

        val resultStart = command
            .exec("START ${mode.mode} $password")
            .trim()
        val split = resultStart.split(' ')

        if (split[0] != "STARTED") throw CommandInvalidException(resultStart)

        val result = callback()

        command.exec("QUIT")
        return result
    }

    suspend fun <T> search(callback: suspend SearchCommand.() -> T): T {
        return createCommandImpl().use { command ->
            execCommand(Mode.SEARCH, command) {
                SearchCommandImpl(command).use {
                    val result = callback(it)
                    result
                }
            }
        }
    }

    suspend fun <T> ingest(callback: suspend IngestCommand.() -> T): T {
        createCommandImpl().use {
            return execCommand(Mode.INGEST, it) {
                callback(IngestCommandImpl(it))
            }
        }
    }

    suspend fun <T> control(callback: suspend ControlCommand.() -> T): T {
        createCommandImpl().use {
            return execCommand(Mode.CONTROL, it) {
                callback(ControlCommandImpl(it))
            }
        }
    }
}
