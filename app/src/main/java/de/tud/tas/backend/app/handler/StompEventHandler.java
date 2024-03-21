/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Sebastian Kucharski)
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
package de.tud.tas.backend.app.handler;

import de.tud.tas.backend.app.service.AssistanceCommunicationService;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceBundle;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;

@AllArgsConstructor
public class StompEventHandler extends StompSessionHandlerAdapter {
    private final AssistanceCommunicationService assistanceCommunicationService;
    private static final Logger log = LoggerFactory.getLogger(StompEventHandler.class);

    @Override
    public void afterConnected(@NonNull StompSession session, @NonNull StompHeaders connectedHeaders) {
        log.info("Connected to Backbone");

        session.subscribe("/assistance", new StompFrameHandler() {

            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return AssistanceBundle.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                assistanceCommunicationService.handleAssistanceForUsers((AssistanceBundle) payload);
            }
        });
    }
}
