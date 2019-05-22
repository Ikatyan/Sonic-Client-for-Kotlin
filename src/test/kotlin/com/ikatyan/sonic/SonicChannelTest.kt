package com.ikatyan.sonic

import com.ikatyan.sonic.commands.Action
import io.kotlintest.*
import io.kotlintest.matchers.startWith
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.runBlocking

class SonicChannelTest : StringSpec() {
    private val sonic = SonicChannel {
        host = "localhost"
    }
    private val collection = "collection"
    private val bucket = "bucket"

    init {
        "sonic.search" {
            shouldNotThrowAny {
                sonic.search {
                    query(collection, bucket, "user", 10)
                }
            }

            shouldThrow<IllegalArgumentException> {
                sonic.search {
                    query("", bucket, "user", 10)
                }
            }

            shouldThrow<IllegalArgumentException> {
                sonic.search {
                    query(collection, "", "user", 10)
                }
            }
            shouldNotThrowAnyUnit {
                sonic.search {
                    query(collection, bucket, "user")
                }
            }

            shouldNotThrowAny {
                sonic.search {
                    suggest(collection, bucket, "word", 10)
                }
            }

            shouldNotThrowAnyUnit {
                sonic.search {
                    suggest(collection, " ", "word", 10)
                }
            }
        }

        "sonic.ingest" {
            shouldNotThrowAnyUnit {
                sonic.ingest {
                    push(collection, bucket, "obj", "hogehoge")
                    push(collection, bucket, "obj", "hogehoge2")
                    pop(collection, bucket, "obj", "hogehoge2")
                    count(collection)
                    count(collection, bucket)
                    count(collection, bucket, "obj")
                    flushCollection(collection)
                    flushBucket(collection, bucket)
                    flushObject(collection, bucket, "obj")
                }
            }
        }

        "sonic.control" {
            shouldNotThrowAnyUnit {
                sonic.control {
                    trigger(Action.Consolidate)
                    trigger(Action.Backup("/hogehoge"))
                    trigger(Action.Restore("/hogehoge"))
                }
            }
        }
    }
}
