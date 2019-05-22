package com.ikatyan.sonic.commands

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket

interface Command {
    fun readLineAsync(): Deferred<String>
    fun writeLineAsync(s: String): Deferred<Unit>
    suspend fun exec(command: String): String
}

class CommandImpl(private val socket: Socket) : Command, AutoCloseable {
    private val reader: BufferedReader = socket.getInputStream().bufferedReader()
    private val writer: BufferedWriter = socket.getOutputStream().bufferedWriter()

    override fun readLineAsync(): Deferred<String> {
        return GlobalScope.async(Dispatchers.IO) { reader.readLine() }
    }

    override fun writeLineAsync(s: String): Deferred<Unit> {
        return GlobalScope.async(Dispatchers.IO) {
            writer.write(s + "\n")
            writer.flush()
        }
    }

    override fun close() {
        socket.close()
    }


    override suspend fun exec(command: String): String {
        writeLineAsync(command).await()
        return readLineAsync().await()
    }
}

