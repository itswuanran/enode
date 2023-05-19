package org.enodeframework.test.commandhandler

import org.enodeframework.annotation.Command
import org.enodeframework.annotation.Subscribe
import org.enodeframework.commanding.CommandContext
import org.enodeframework.test.command.AsyncHandlerBaseCommand
import org.enodeframework.test.command.AsyncHandlerChildCommand
import org.enodeframework.test.commandhandler.TestCommandHandler2
import org.slf4j.LoggerFactory

@Command
class TestCommandHandler2 {

    @Subscribe
    suspend fun handleAsync(context: CommandContext, command: AsyncHandlerBaseCommand) {
//        val testAggregate = context.get(command.aggregateRootId, TestAggregate::class.java)
//        logger.info("command exec: {}, {}, {}", command.id, command.aggregateRootId, testAggregate.version)
    }

    @Subscribe
    suspend fun handleAsync(context: CommandContext, command: AsyncHandlerChildCommand) {
//        val testAggregate = context.get(command.aggregateRootId, TestAggregate::class.java)
//        logger.info("command exec: {}, {}, {}", command.id, command.aggregateRootId, testAggregate.version)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TestCommandHandler2::class.java)
    }
}