package org.enodeframework.commanding

/**
 * @author anruence@gmail.com
 */
class CommandResult {
    /**
     * Represents the result status of the command.
     */
    var status: CommandStatus = CommandStatus.Failed
        private set

    /**
     * Represents the unique identifier of the command.
     */
    var commandId: String = ""
        private set

    /**
     * Represents the aggregate root id associated with the command.
     */
    var aggregateRootId: String = ""
        private set

    /**
     * Represents the command result data.
     */
    var result: String? = ""
        private set

    /**
     * Represents the command result data type.
     */
    var resultType: String = ""
        private set

    constructor()

    /**
     * Parameterized constructor.
     */
    constructor(status: CommandStatus, commandId: String, aggregateRootId: String, result: String?, resultType: String) {
        this.status = status
        this.commandId = commandId
        this.aggregateRootId = aggregateRootId
        this.result = result
        this.resultType = resultType
    }
}