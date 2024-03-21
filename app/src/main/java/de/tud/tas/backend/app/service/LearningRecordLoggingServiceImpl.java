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
package de.tud.tas.backend.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tud.tas.backend.app.exceptions.UserNotFoundException;
import de.tud.tas.backend.app.model.AssistanceCommunicationObject;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.Assistance;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceBundle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LearningRecordLoggingServiceImpl implements LearningRecordLoggingService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LearningRecordLoggingServiceImpl.class);
    private static final com.savoirtech.logging.slf4j.json.logger.Logger jsonLogger =
            com.savoirtech.logging.slf4j.json.LoggerFactory.getLogger("LearningRecordLogger");

    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Override
    public void logStatement(JsonNode statement) {
        try {
            log.info("Recorded statement for learner '{}'",
                    statement.get("actor").get("account").get("name"));
            jsonLogger.info()
                    .message("Recorded statement for learner")
                    .map("statement", objectMapper
                            .convertValue(statement, new TypeReference<Map<String, Object>>() {
                            }))
                    .log();
        } catch (Exception e) {
            log.warn("Could not log statement!");
        }
    }

    @Override
    public void logAssistanceBundle(AssistanceBundle assistanceBundle) {
        try {
            for (Assistance assistance : assistanceBundle.getAssistance()) {
                try {
                    log.info("Recorded assistance bundle for learner '{}'",
                        userService.getUserByActorAccountName(assistance.getUserId()).getActorAccountName());
                } catch (UserNotFoundException e) {
                    log.info("Recorded assistance bundle for unknown user '{}'", assistance.getUserId());
                }
                jsonLogger.info()
                        .message("Recorded assistance bundle for learner")
                        .map("assistanceBundle", objectMapper
                                .convertValue(assistanceBundle, new TypeReference<Map<String, Object>>() {
                                }))
                        .log();
            }
        } catch (Exception e) {
            log.warn("Could not log assistance bundle!");
        }
    }

    @Override
    public void logAssistanceCommunicationObject(AssistanceCommunicationObject assistanceCommunicationObject) {
        try {
            log.info("Recorded assistance communication object for learner '{}'",
                    userService.getUserByActorAccountName(assistanceCommunicationObject.getAId()).getActorAccountName());
            jsonLogger.info()
                    .message("Recorded assistance communication object for learner")
                    .map("assistanceCommunicationObject", objectMapper
                            .convertValue(assistanceCommunicationObject, new TypeReference<Map<String, Object>>() {
                            }))
                    .log();
        } catch (Exception e) {
            log.warn("Could not log assistance communication object!");
        }
    }
}
