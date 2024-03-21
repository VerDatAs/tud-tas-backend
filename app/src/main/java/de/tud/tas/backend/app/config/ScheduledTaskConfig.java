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
package de.tud.tas.backend.app.config;

import de.tud.tas.backend.app.factory.StatementFactory;
import de.tud.tas.backend.app.model.WebSocketDisconnect;
import de.tud.tas.backend.app.provision.TimeProvider;
import de.tud.tas.backend.app.repository.UserRepository;
import de.tud.tas.backend.app.repository.WebSocketDisconnectRepository;
import de.tud.tas.backend.app.service.AssistanceCommunicationService;
import de.tud.tas.backend.app.service.AssistanceTypeService;
import de.tud.tas.backend.app.service.CourseService;
import de.tud.tas.backend.learning_locker_api_client.api.StatementsApi;
import de.tud.tas.backend.learning_locker_api_client.model.Statement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTaskConfig {
    private final AssistanceCommunicationService assistanceCommunicationService;
    private final AssistanceTypeService assistanceTypeService;
    private final CourseService courseService;
    private final StatementFactory statementFactory;
    private final StatementsApi statementsApi;
    private final TimeProvider timeProvider;
    private final UserRepository userRepository;
    private final WebSocketDisconnectRepository webSocketDisconnectRepository;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskConfig.class);

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void scheduleDeletionOfExpiredAssistanceCommunicationObjects() {
        assistanceCommunicationService.removeExpiredAssistanceCommunicationObjects(
                timeProvider.getCurrentTime().minus(Duration.ofHours(1)));
    }

    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.SECONDS)
    public void scheduleHandlingOfDisconnectedWebSocketConnections() {
        LocalDateTime timestampThreshold = timeProvider.getCurrentTime().minus(Duration.ofSeconds(10));
        List<WebSocketDisconnect> expiredWebSocketDisconnects =
                webSocketDisconnectRepository.getWebSocketDisconnectsByDisconnectTimestampBefore(timestampThreshold);
        if (expiredWebSocketDisconnects.isEmpty()){
            return;
        }
        List<Statement> loggedOutStatements = StreamSupport.stream(
                        userRepository.findAllById(
                                        expiredWebSocketDisconnects
                                                .stream()
                                                .map(WebSocketDisconnect::getUserId)
                                                .toList())
                                .spliterator(), false)
                .map(disconnectedUser -> statementFactory.generateStatement(
                        disconnectedUser.getActorAccountName(),
                        disconnectedUser.getLastLoggedInLmsUrl(),
                        "loggedout",
                        disconnectedUser.getLastLoggedInLmsUrl()))
                .toList();
        try {
            statementsApi.storeStatements(loggedOutStatements);
        } catch (Exception e) {
            log.error("Sending of logout statement to Learning Locker failed!");
        }
        webSocketDisconnectRepository.deleteAll(expiredWebSocketDisconnects);
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
    public void scheduleSyncOfAssistanceTypes() {
        try {
            assistanceTypeService.syncAssistanceTypes();
        } catch (Exception e) {
            log.error("Failed to sync assistance types - {}!", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
    public void scheduleSyncOfCourses() {
        try {
            courseService.syncCourses();
        } catch (Exception e) {
            log.error("Failed to sync courses - {}!", e.getMessage());
        }
    }
}
