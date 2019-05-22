package com.ikatyan.sonic.commands

import com.ikatyan.sonic.CommandInvalidException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import java.util.*

interface SearchCommand {
    suspend fun query(
        collection: String, bucket: String, terms: String,
        limit: Int? = null, offset: Int? = null, lang: String? = null
    ): List<String>

    suspend fun suggest(collection: String, bucket: String, word: String, limit: Int? = null): List<String>
}

class SearchCommandImpl(private val command: CommandImpl) : Command by command,
    SearchCommand, AutoCloseable {
    private val sendResponseChannelQueue =
        Channel<SendChannel<List<String>>>()

    private val receiveResponseJob: Job

    init {
        receiveResponseJob = launchReceiveResponseJob()
    }

    private fun launchReceiveResponseJob(): Job {
        return GlobalScope.launch {
            val sendResponseChannelMap = emptyMap<String, SendChannel<List<String>>>().toMutableMap()
            while (true) {
                //デッドロック注意
                val response = readLineAsync().await()
                when {
                    response.startsWith("PENDING ") -> {
                        val pendingId = response.split(' ')[1]
                        sendResponseChannelMap[pendingId] = sendResponseChannelQueue.receive()
                    }

                    response.startsWith("EVENT QUERY ")
                            || response.startsWith("EVENT SUGGEST ") -> {
                        response.split(' ').let {
                            val pendingId = it[2]
                            val queryResult = it.slice(3 until it.size)
                            sendResponseChannelMap[pendingId]!!.run {
                                send(queryResult)
                            }
                            sendResponseChannelMap.remove(pendingId)
                        }
                    }

                    else -> throw CommandInvalidException(response)
                }
            }
        }
    }

    override suspend fun query(
        collection: String, bucket: String, terms: String,
        limit: Int?, offset: Int?, lang: String?
    ): List<String> {

        checkStringArg(collection, "Collection")
        checkStringArg(bucket, "Bucket")
        checkStringArg(terms, "Terms", hasQuote = true)
        checkStringArg(lang, "Lang")

        val command = StringJoiner(" ").run {
            add("QUERY")
            add(collection)
            add(bucket)
            add(quote(terms))
            if (limit != null) add("LIMIT($limit)")
            if (offset != null) add("OFFSET($offset)")
            if (lang != null) add("LANG($lang)")
            toString()
        }
        return execSearchCommand(command)
    }

    override suspend fun suggest(collection: String, bucket: String, word: String, limit: Int?): List<String> {
        checkStringArg(collection, "Collection")
        checkStringArg(bucket, "Bucket")
        checkStringArg(word, "Word", true)

        val command = StringJoiner(" ").run {
            add("SUGGEST")
            add(collection)
            add(bucket)
            add(quote(word))
            if (limit != null) add("LIMIT($limit)")
            toString()
        }

        return execSearchCommand(command)
    }

    private suspend fun execSearchCommand(command: String): List<String> {
        val channel = Channel<List<String>>()
        writeLineAsync(command).await()
        sendResponseChannelQueue.send(channel)
        val result = channel.receive()
        channel.close()
        return result
    }


    override fun close() {
        receiveResponseJob.cancel()
    }
}