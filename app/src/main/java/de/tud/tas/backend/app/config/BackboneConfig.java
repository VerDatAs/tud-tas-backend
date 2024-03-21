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
package de.tud.tas.backend.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tud.tas.backend.app.handler.StompEventHandler;
import de.tud.tas.backend.app.model.UserRole;
import de.tud.tas.backend.app.service.AssistanceCommunicationService;
import de.tud.tas.backend.app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@Configuration
@RequiredArgsConstructor
public class BackboneConfig {
    /**
     * Desired interval for heartbeats to receive in milliseconds.
     */
    @Value("${stomp.incoming.heartbeat.interval:0}")
    private Integer incomingHeartbeatInterval;
    /**
     * Intended interval for heartbeats to send in milliseconds.
     */
    @Value("${stomp.outgoing.heartbeat.interval:0}")
    private Integer outgoingHeartbeatInterval;
    @Value("${tud-assistance-backbone.url}")
    private String tudAssistanceBackboneUrl;

    private final AssistanceCommunicationService assistanceCommunicationService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private TaskScheduler messageBrokerTaskScheduler;

    @Autowired
    public void setMessageBrokerTaskScheduler(@Lazy TaskScheduler taskScheduler) {
        messageBrokerTaskScheduler = taskScheduler;
    }

    @Bean
    public WebSocketStompClient backboneStompClient() {
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);

        MappingJackson2MessageConverter mappingJackson2MessageConverter = new MappingJackson2MessageConverter();
        mappingJackson2MessageConverter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(mappingJackson2MessageConverter);

        stompClient.setDefaultHeartbeat(new long[]{outgoingHeartbeatInterval, incomingHeartbeatInterval});
        stompClient.setTaskScheduler(messageBrokerTaskScheduler);

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("token", authService.createJwt("tud_tas_backend", UserRole.ADMIN));

        String websocketUrl = tudAssistanceBackboneUrl
                .replaceFirst("http://", "ws://")
                .replaceFirst("https://", "wss://") + "/api/v1/websocket";

        StompSessionHandler sessionHandler = new StompEventHandler(assistanceCommunicationService);
        stompClient.connect(websocketUrl, (WebSocketHttpHeaders) null, stompHeaders, sessionHandler);
        return stompClient;
    }
}
