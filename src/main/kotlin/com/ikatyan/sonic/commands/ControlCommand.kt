package com.ikatyan.sonic.commands

interface ControlCommand {
    suspend fun trigger(action: Action)
}

class ControlCommandImpl(private val command: CommandImpl):
    Command by command,
    ControlCommand {

    override suspend fun trigger(action: Action) {
        when(action) {
            is Action.Consolidate -> runConsolidate()
            is Action.Backup -> runBackup(action.path)
            is Action.Restore -> runRestore(action.path)
        }
    }

    private suspend fun runConsolidate() {
        val command = "TRIGGER consolidate"
        runTrigger(command)
    }

    private suspend fun runBackup(path: String) {
        val command = "TRIGGER backup $path"
        runTrigger(command)
    }

    private suspend fun runRestore(path: String) {
        val command = "TRIGGER restore $path"
        runTrigger(command)
    }

    private suspend fun runTrigger(command: String) {
        val response = exec(command)
        if(response.startsWith("ERR")) {
            throw buildCommandInvalidException(response, command)
        }
    }
}

sealed class Action {
    object Consolidate : Action()
    class Backup(val path: String): Action()
    class Restore(val path: String): Action()
}