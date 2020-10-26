package com.microsoft.conference.registration.controller;

import com.microsoft.conference.registration.readmodel.service.OrderLineVO;
import com.microsoft.conference.registration.readmodel.service.SeatTypeVO;
import lombok.Data;

@Data
public class OrderItemViewModel {
    private SeatTypeVO seatTypeVO;
    private OrderLineVO orderLineVO;
    private int quantity;
    private int maxSelectionQuantity;
}