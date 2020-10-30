package com.microsoft.conference.registration.controller;

import com.microsoft.conference.registration.readmodel.service.OrderSeatAssignmentVO;
import lombok.Data;

import java.util.List;

@Data
public class OrderSeatsViewModel {
    private String orderId;

    private List<OrderSeatAssignmentVO> seatAssignments;
}
