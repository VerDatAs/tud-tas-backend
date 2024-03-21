/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Max Schaible, Robert Schmidt, Sebastian Kucharski, Tommy Kubica)
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
package de.tud.tas.backend.app.controller;

import de.tud.tas.backend.app.dto.AssistanceCommunicationObjectDto;
import de.tud.tas.backend.app.exceptions.UnauthorizedAccessException;
import de.tud.tas.backend.app.mapper.AssistanceCommunicationObjectMapper;
import de.tud.tas.backend.app.model.User;
import de.tud.tas.backend.app.model.UserRole;
import de.tud.tas.backend.app.service.AssistanceCommunicationService;
import de.tud.tas.backend.app.service.ChatbotMessageService;
import de.tud.tas.backend.app.service.UserService;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceBundle;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final AssistanceCommunicationService assistanceCommunicationService;
    private final AssistanceCommunicationObjectMapper assistanceCommunicationObjectMapper;
    private final UserService userService;

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(WebSocketController.class);

    @MessageMapping("/user/queue/chat/{contextId}")
    public void handleAssistanceCommunicationObjectFromUser(
            @Payload AssistanceCommunicationObjectDto assistanceCommunicationObjectDto,
            @DestinationVariable String contextId, Principal principal) {
        User user = userService.getUser(UUID.fromString(principal.getName()));
        log.debug("Received Chatbot message from user '" + user.getActorAccountName() + "' with contextId " + "'" + contextId + "'.");
        assistanceCommunicationService.handleAssistanceCommunicationObjectFromUser(user,
                assistanceCommunicationObjectMapper.toAssistanceCommunicationObject(assistanceCommunicationObjectDto, contextId));
    }
}
