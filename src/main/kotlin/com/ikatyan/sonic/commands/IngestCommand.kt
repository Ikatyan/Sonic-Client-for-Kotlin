package com.ikatyan.sonic.commands

import java.util.*

interface IngestCommand {
    suspend fun push(collection: String, bucket: String, obj: String, text: String): Int
    suspend fun pop(collection: String, bucket: String, obj: String, text: String): Int
    suspend fun count(collection: String, bucket: String? = null, obj: String? = null): Int
    suspend fun flushCollection(collection: String): Int
    suspend fun flushBucket(collection: String, bucket: String): Int
    suspend fun flushObject(collection: String, bucket: String, obj: String): Int

}

class IngestCommandImpl(private val command: CommandImpl) : Command by command,
    IngestCommand {

    override suspend fun push(collection: String, bucket: String, obj: String, text: String): Int {
        checkStringArg(collection, "Collection")
        checkStringArg(bucket, "Bucket")
        checkStringArg(obj, "Object")
        checkStringArg(text, "Text", true)

        val commandText = "PUSH $collection $bucket $obj ${quote(text)}"
        val pushResult = exec(commandText).trim()

        if (!pushResult.startsWith("OK"))
            throw buildCommandInvalidException(pushResult, commandText)

        return count(collection, bucket, obj)
    }

    override suspend fun pop(collection: String, bucket: String, obj: String, text: String): Int {
        checkStringArg(collection, "Collection")
        checkStringArg(bucket, "Bucket")
        checkStringArg(obj, "Object")
        checkStringArg(text, "Text", true)

        val command = "POP $collection $bucket $obj ${quote(text)}"
        val pushResult = exec(command)
        if (!pushResult.startsWith("RESULT ")) throw buildCommandInvalidException(
            pushResult,
            command
        )
        return count(collection, bucket, obj)
    }

    override suspend fun count(collection: String, bucket: String?, obj: String?): Int {
        checkStringArg(collection, "Collection")
        checkStringArg(bucket, "Bucket")
        checkStringArg(obj, "Object")

        val command = StringJoiner(" ").run {
            add("COUNT")
            add(collection)
            if (bucket != null) add(bucket)
            if (obj != null) add(obj)
            toString()
        }
        val countResult = exec(command)

        if (!countResult.startsWith("RESULT ")) throw buildCommandInvalidException(
            countResult,
            command
        )

        val split = countResult.split(' ')
        return split[1].toInt()
    }

    override suspend fun flushCollection(collection: String): Int {
        checkStringArg(collection, "Collection")
        val command = "FLUSHC $collection"
        val response = exec(command)
        return getFlushTermsCount(response, command)
    }

    override suspend fun flushBucket(collection: String, bucket: String): Int {
        checkStringArg(collection, "Collection")
        checkStringArg(bucket, "Bucket")

        val command = "FLUSHB $collection $bucket"
        val response = exec(command)
        return getFlushTermsCount(response, command)
    }

    override suspend fun flushObject(collection: String, bucket: String, obj: String): Int {
        checkStringArg(collection, "Collection")
        checkStringArg(bucket, "Bucket")
        checkStringArg(obj, "Object")

        val command = "FLUSHO $collection $bucket $obj"
        val response = exec(command)
        return getFlushTermsCount(response, command)
    }

    private fun getFlushTermsCount(response: String, command: String): Int {
        if (!response.startsWith("RESULT ")) throw buildCommandInvalidException(response, command)
        return response.split(' ')[1].toInt()
    }
}