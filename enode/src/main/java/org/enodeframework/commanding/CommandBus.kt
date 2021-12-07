package org.enodeframework.commanding

import java.util.concurrent.CompletableFuture

/**
 * Represents a command service.
 */
interface CommandBus {
    /**
     * Send a command asynchronously.
     *
     * @param command The command to send.
     * @return A task which contains the send result of the command.
     */
    fun sendAsync(command: CommandMessage<*>): CompletableFuture<Boolean>

    /**
     * Send a command synchronously.
     */
    suspend fun send(command: CommandMessage<*>): Boolean

    /**
     * Execute a command asynchronously with the default command return type.
     *
     * @param command The command to execute.
     * @return A task which contains the result of the command.
     */
    fun executeAsync(command: CommandMessage<*>): CompletableFuture<CommandResult>

    /**
     * Execute a command asynchronously with the default command return type.
     */
    suspend fun execute(command: CommandMessage<*>): CommandResult

    /**
     * Execute a command asynchronously with the specified command return type.
     *
     * @param command           The command to execute.
     * @param commandReturnType The return type of the command.
     * @return A task which contains the result of the command.
     */
    fun executeAsync(command: CommandMessage<*>, commandReturnType: CommandReturnType): CompletableFuture<CommandResult>

    /**
     * Execute a command synchronously with the specified command return type.
     */
    suspend fun execute(command: CommandMessage<*>, commandReturnType: CommandReturnType): CommandResult
}