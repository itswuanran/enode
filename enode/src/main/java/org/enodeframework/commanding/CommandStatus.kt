package org.enodeframework.commanding

enum class CommandStatus(var value: String) {
    Success("Success"),
    NoChange("NoChange"),
    Failed("Failed"),
    SendFailed("SendFailed")
}