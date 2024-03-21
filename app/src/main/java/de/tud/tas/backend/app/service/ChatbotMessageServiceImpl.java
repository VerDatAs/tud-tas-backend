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
package de.tud.tas.backend.app.service;

import de.tud.tas.backend.app.dto.AssistanceCommunicationObjectDto;
import de.tud.tas.backend.app.mapper.AssistanceCommunicationObjectMapper;
import de.tud.tas.backend.app.model.User;
import de.tud.tas.backend.app.model.AssistanceCommunicationObject;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatbotMessageServiceImpl implements ChatbotMessageService {
    public final static String MESSAGE_DESTINATION = "/queue/chat/";

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final AssistanceCommunicationObjectMapper assistanceCommunicationObjectMapper;

    @Override
    public void sendAssistanceCommunicationObjectToUser(User user,
                                                        AssistanceCommunicationObject assistanceCommunicationObject) {
        if (assistanceCommunicationObject == null) {
            return;
        }
        String contextId = assistanceCommunicationObject.getContextId();
        if (contextId == null) {
            contextId = "0";
        }
        String destination = MESSAGE_DESTINATION + contextId;
        AssistanceCommunicationObjectDto assistanceCommunicationObjectDto =
                assistanceCommunicationObjectMapper.toAssistanceCommunicationObjectDto(assistanceCommunicationObject);
        simpMessagingTemplate.convertAndSendToUser(
                user.getId().toString(), destination, assistanceCommunicationObjectDto);
    }
}
