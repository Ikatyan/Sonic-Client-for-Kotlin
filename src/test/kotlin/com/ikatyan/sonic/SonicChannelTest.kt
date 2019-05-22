package com.ikatyan.sonic

import com.ikatyan.sonic.commands.Action
import io.kotlintest.shouldNotThrowAny
import io.kotlintest.shouldNotThrowAnyUnit
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

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

            shouldThrow<IllegalArgumentException> {
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
