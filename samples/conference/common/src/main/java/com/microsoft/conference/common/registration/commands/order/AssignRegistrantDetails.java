package com.microsoft.conference.common.registration.commands.order;

import lombok.Data;
import org.enodeframework.commanding.Command;

@Data
public class AssignRegistrantDetails extends Command<String> {
    private String firstName;
    private String lastName;
    private String email;

    public AssignRegistrantDetails() {
    }

    public AssignRegistrantDetails(String orderId) {
        super(orderId);
    }
}
