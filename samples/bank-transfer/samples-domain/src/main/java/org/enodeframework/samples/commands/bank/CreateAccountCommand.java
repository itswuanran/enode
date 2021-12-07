package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.AbstractCommandMessage;

public class CreateAccountCommand extends AbstractCommandMessage<String> {
    public String owner;

    public CreateAccountCommand() {
    }

    public CreateAccountCommand(String accountId, String owner) {
        super(accountId);
        this.owner = owner;
    }
}
