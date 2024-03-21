/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Robert Schmidt, Sebastian Kucharski)
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

import de.tud.tas.backend.app.model.User;
import de.tud.tas.backend.app.model.UserRole;
import de.tud.tas.backend.app.model.WebSocketDisconnect;
import de.tud.tas.backend.app.provision.TimeProvider;
import de.tud.tas.backend.app.repository.WebSocketDisconnectRepository;
import de.tud.tas.backend.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.security.Principal;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class WebSocketEventHandler {
    private final TimeProvider timeProvider;
    private final UserService userService;
    private final WebSocketDisconnectRepository webSocketDisconnectRepository;

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventHandler.class);

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        log.debug("WebSocket session received CONNECT");

        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        User user = userService.getUserByIdOrActorAccountName(Objects.requireNonNull(headers.getUser()).getName());

        log.debug("WebSocket session for user {}({}) is going to be connected",
                user.getId(), user.getActorAccountName());

        webSocketDisconnectRepository.deleteById(user.getId());
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        log.debug("WebSocket session connected");
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        log.debug("WebSocket session received DISCONNECT");

        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Principal userPrincipal = headers.getUser();
        if (userPrincipal == null) {
            return;
        }
        User user = userService.getUserByIdOrActorAccountName(userPrincipal.getName());

        log.debug("WebSocket session for user {}({}) is going to be disconnected",
                user.getId(), user.getActorAccountName());

        webSocketDisconnectRepository.save(new WebSocketDisconnect(user.getId(), timeProvider.getCurrentTime()));
    }

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        log.debug("WebSocket session received SUBSCRIBE");
    }

    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        log.debug("WebSocket session received UNSUBSCRIBE");
    }
}
