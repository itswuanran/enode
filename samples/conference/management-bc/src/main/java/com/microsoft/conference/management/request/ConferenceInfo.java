package com.microsoft.conference.management.request;

import com.microsoft.conference.management.domain.model.SeatType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * The full conference information.
 * This class inherits from {@link EditableConferenceInfo}
 * and exposes more information that is not user-editable once
 * it has been generated or provided.
 **/
@Getter
@Setter
public class ConferenceInfo extends EditableConferenceInfo {

    private String id;

    private String accessCode;

    private String ownerName;

    private String ownerEmail;

    private String slug;

    private Boolean wasEverPublished = false;

    private List<SeatType> seatTypes = new ArrayList<>();
}
