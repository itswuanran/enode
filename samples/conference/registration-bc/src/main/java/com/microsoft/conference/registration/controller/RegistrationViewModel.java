package com.microsoft.conference.registration.controller;

import com.microsoft.conference.registration.readmodel.service.OrderVO;
import lombok.Data;

@Data
public class RegistrationViewModel {
    private RegistrantDetails registrantDetails;
    private OrderVO orderVO;
}
