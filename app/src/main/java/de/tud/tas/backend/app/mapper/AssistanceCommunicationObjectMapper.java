/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Max Schaible, Sebastian Kucharski)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ***************************************************************************************/
package de.tud.tas.backend.app.mapper;

import de.tud.tas.backend.app.dto.AssistanceCommunicationObjectDto;
import de.tud.tas.backend.app.model.AssistanceCommunicationObject;
import de.tud.tas.backend.app.model.User;
import de.tud.tas.backend.app.service.UserService;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceObject;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceObjectRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AssistanceCommunicationObjectMapper {
    @Autowired
    private UserService userService;

    @Mapping(target = "timestamp", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public abstract AssistanceCommunicationObjectDto toAssistanceCommunicationObjectDto(
            AssistanceCommunicationObject value);

    @Mapping(target = "timestamp", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public abstract AssistanceCommunicationObject toAssistanceCommunicationObject(
            AssistanceCommunicationObjectDto value, String contextId);

    public AssistanceCommunicationObject toAssistanceCommunicationObject(AssistanceObjectRecord value) {
        User user = userService.getUserByActorAccountName(value.getUserId());
        return new AssistanceCommunicationObject(LocalDateTime.ofInstant(
                Instant.parse(value.getTimestamp()), ZoneId.systemDefault()), user.getId().toString(), value.getaId(),
                value.getAssistanceType(), value.getAoId(), value.getParameters(), value.getContextId());
    }

    public AssistanceCommunicationObject toAssistanceCommunicationObject(AssistanceObject value, String aId) {
        User user = userService.getUserByActorAccountName(value.getUserId());
        return new AssistanceCommunicationObject(LocalDateTime.ofInstant(
                Instant.parse(value.getTimestamp()), ZoneId.systemDefault()), user.getId().toString(), aId,
                value.getAssistanceType(), value.getAoId(), value.getParameters(), value.getContextId());
    }
}
