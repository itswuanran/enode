package com.microsoft.conference.common.registration.commands.seatassignment;

import lombok.Data;
import org.enodeframework.commanding.Command;

@Data
public class UnassignSeat extends Command<String> {
    private int position;

    public UnassignSeat() {
    }

    public UnassignSeat(String assignmentsId) {
        super(assignmentsId);
    }
}
