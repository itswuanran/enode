package com.microsoft.conference.management.readmodel;

import lombok.Data;

@Data
public class AttendeeVO {
    private int position;
    private String seatTypeName;
    private String attendeeFirstName;
    private String attendeeLastName;
    private String attendeeEmail;
}
