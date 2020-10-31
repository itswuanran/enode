package com.microsoft.conference.management.request;

import lombok.Data;

@Data
public class LocateConference {
    private String email;
    private String accessCode;
}