package com.microsoft.conference.registration.controller;

import lombok.Data;

import java.util.List;

@Data
public class OrderViewModel {
    private String orderId;
    private int orderVersion;
    private String conferenceId;
    private String conferenceCode;
    private String conferenceName;
    private List<OrderItemViewModel> items;
    private List<SeatQuantity> seats;
    private long reservationExpirationDate;
}
