package com.microsoft.conference.registration.controller;

import com.microsoft.conference.common.ActionResult;
import com.microsoft.conference.registration.readmodel.service.ConferenceAlias;
import com.microsoft.conference.registration.readmodel.service.ConferenceQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RConferenceController {
    @Autowired
    private ConferenceQueryService conferenceQueryService;

    @GetMapping("/rconference/{id}")
    public ActionResult<ConferenceAlias> Display(@PathVariable("id") String conferenceCode) {
        return ActionResult.of(conferenceQueryService.getConferenceAlias(conferenceCode));
    }
}
