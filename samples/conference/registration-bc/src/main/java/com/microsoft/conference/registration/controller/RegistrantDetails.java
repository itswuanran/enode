package com.microsoft.conference.registration.controller;

import lombok.Data;

@Data
public class RegistrantDetails {
    private String orderId;
    private String firstName;
    private String lastName;
    private String email;
}
