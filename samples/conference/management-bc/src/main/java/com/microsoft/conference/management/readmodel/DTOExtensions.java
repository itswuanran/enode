package com.microsoft.conference.management.readmodel;

import com.microsoft.conference.common.dataobject.ConferenceDO;
import com.microsoft.conference.common.dataobject.ConferenceSlugIndexDO;
import com.microsoft.conference.common.dataobject.OrderDO;
import com.microsoft.conference.common.dataobject.OrderSeatAssignmentDO;
import com.microsoft.conference.common.dataobject.SeatTypeDO;
import com.microsoft.conference.common.management.commands.AddSeatType;
import com.microsoft.conference.common.management.commands.CreateConference;
import com.microsoft.conference.common.management.commands.UpdateConference;
import com.microsoft.conference.common.management.commands.UpdateSeatType;
import com.microsoft.conference.management.domain.event.ConferenceCreated;
import com.microsoft.conference.management.domain.event.ConferenceUpdated;
import com.microsoft.conference.management.domain.model.ConferenceEditableInfo;
import com.microsoft.conference.management.domain.model.ConferenceSlugIndex;
import com.microsoft.conference.management.domain.model.SeatType;
import com.microsoft.conference.management.request.ConferenceInfo;
import com.microsoft.conference.management.request.EditableConferenceInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DTOExtensions {

    DTOExtensions INSTANCE = Mappers.getMapper(DTOExtensions.class);

    CreateConference toCreateConferenceCommand(ConferenceInfo conference);

    UpdateConference toUpdateConferenceCommand(EditableConferenceInfo model);

    @Mapping(source = "model.id", target = "id")
    AddSeatType toAddSeatTypeCommand(SeatType model, ConferenceInfo conference);

    @Mapping(source = "model.id", target = "id")
    UpdateSeatType toUpdateSeatTypeCommand(SeatType model, ConferenceInfo conference);

    OrderVO toVO(OrderDO orderDO);

    AttendeeVO toVO(OrderSeatAssignmentDO assignmentDO);

    @Mapping(source = "evnt.version", target = "version")
    @Mapping(source = "evnt.sequence", target = "eventSequence")
    @Mapping(source = "evnt.aggregateRootId", target = "conferenceId")
    @Mapping(target = "id", ignore = true)
    ConferenceDO toDO(ConferenceCreated evnt, com.microsoft.conference.management.domain.model.ConferenceInfo info);

    @Mapping(source = "isPublished", target = "isPublished", qualifiedByName = "toBoolean")
    ConferenceVO toVO(ConferenceDO conferenceDO);

    @Named("toBoolean")
    default boolean toBoolean(Byte v) {
        return v == (byte) 1;
    }

    ConferenceSlugIndex toSlugIndex(ConferenceSlugIndexDO slugIndexDO);

    SeatTypeVO toVO(SeatTypeDO seatTypeDO);

    @Mapping(source = "evnt.version", target = "version")
    @Mapping(source = "evnt.sequence", target = "eventSequence")
    @Mapping(source = "evnt.aggregateRootId", target = "conferenceId")
    @Mapping(target = "id", ignore = true)
    ConferenceDO toDO(ConferenceUpdated evnt, ConferenceEditableInfo info);
}