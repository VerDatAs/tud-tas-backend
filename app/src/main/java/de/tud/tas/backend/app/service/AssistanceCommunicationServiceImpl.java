/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Sebastian Kucharski, Tommy Kubica)
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

import de.tud.tas.backend.app.exceptions.UserNotFoundException;
import de.tud.tas.backend.app.factory.StatementFactory;
import de.tud.tas.backend.app.mapper.AssistanceCommunicationObjectMapper;
import de.tud.tas.backend.app.model.AssistanceCommunicationObject;
import de.tud.tas.backend.app.model.User;
import de.tud.tas.backend.app.provision.TimeProvider;
import de.tud.tas.backend.app.repository.AssistanceCommunicationObjectRepository;
import de.tud.tas.backend.learning_locker_api_client.api.StatementsApi;
import de.tud.tas.backend.learning_locker_api_client.model.Statement;
import de.tud.tas.backend.tud_assistance_backbone_api_client.api.TutorialModuleApi;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
@Service
@RequiredArgsConstructor
public class AssistanceCommunicationServiceImpl implements AssistanceCommunicationService {
    @Value("${tud-assistance-backbone.url}")
    private String tudAssistanceBackboneUrl;

    private final AssistanceCommunicationObjectMapper assistanceCommunicationObjectMapper;
    private final AssistanceCommunicationObjectRepository assistanceCommunicationObjectRepository;
    private final ChatbotMessageService chatbotMessageService;
    private final LearningRecordLoggingService learningRecordLoggingService;
    private final StatementFactory statementFactory;
    private final StatementsApi statementsApi;
    private final TimeProvider timeProvider;
    private final TutorialModuleApi tutorialModuleApi;
    private final UserService userService;

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AssistanceCommunicationServiceImpl.class);

    private final String ASSISTANCE_PARAMETER_KEY_JUST_LOGGED_IN = "just_logged_in";
    private final String ASSISTANCE_PARAMETER_KEY_LMS_URL = "lms_url";
    private final String ASSISTANCE_PARAMETER_KEY_PREVIOUS_MESSAGES = "previous_messages";
    private final String ASSISTANCE_PARAMETER_KEY_UNACKNOWLEDGED_OBJECTS = "unacknowledged_messages";

    @Override
    public void handleAssistanceCommunicationObjectFromUser(
            User user, AssistanceCommunicationObject assistanceCommunicationObject) {
        // Check if a communication object is acknowledged
        if ((assistanceCommunicationObject.getParameters() == null
                || assistanceCommunicationObject.getParameters().isEmpty())
                && assistanceCommunicationObject.getMessageId() != null) {
            assistanceCommunicationObjectRepository.deleteAssistanceCommunicationObjectsByMessageId(
                    assistanceCommunicationObject.getMessageId());
            return;
        }

        if (assistanceCommunicationObject.getParameters() == null
                || assistanceCommunicationObject.getParameters().isEmpty()) {
            throw new IllegalArgumentException();
        }

        // Check if a login should be handled
        AssistanceParameter justLoggedInParameter = getAssistanceParameterByKey(assistanceCommunicationObject,
                ASSISTANCE_PARAMETER_KEY_JUST_LOGGED_IN);
        if (justLoggedInParameter != null) {
            handleJustLoggedInAssistanceCommunicationObjectFromUser(
                    user, assistanceCommunicationObject, justLoggedInParameter);
            return;
        }

        // Send assistance object to the TUD Assistance Backbone
        if (assistanceCommunicationObject.getAId() == null) {
            throw new IllegalArgumentException();
        }
        try {
            tutorialModuleApi.updateAssistanceProcess(assistanceCommunicationObject.getAId(),
                    List.of((new AssistanceResponseObject())
                            .aoId(assistanceCommunicationObject.getAoId())
                            .userId(user.getActorAccountName())
                            .parameters(assistanceCommunicationObject.getParameters())));
        } catch (Exception e) {
            log.error("Failed to send Assistance Object to the assistance system!");
        }
    }

    @Override
    public void handleAssistanceForUsers(AssistanceBundle assistanceBundle) {
        learningRecordLoggingService.logAssistanceBundle(assistanceBundle);
        if (assistanceBundle == null) {
            throw new IllegalStateException();
        }

        List<Statement> assistanceStatements = new ArrayList<>();
        for (Assistance assistance : assistanceBundle.getAssistance()) {
            for (AssistanceObject assistanceObject : assistance.getAssistanceObjects()) {
                User user;
                try {
                    user = userService.getUserByActorAccountName(assistanceObject.getUserId());
                } catch (UserNotFoundException e) {
                    log.warn("Can't send assistance object to user {}. User not found!", assistanceObject.getUserId());
                    assistanceStatements.add(statementFactory.generateStatement(
                            "anonymous", tudAssistanceBackboneUrl, "got_assisted_by", tudAssistanceBackboneUrl));
                    continue;
                }
                AssistanceCommunicationObject assistanceCommunicationObject = assistanceCommunicationObjectMapper
                        .toAssistanceCommunicationObject(assistanceObject, assistance.getaId());
                assistanceCommunicationObjectRepository.save(assistanceCommunicationObject);
                chatbotMessageService.sendAssistanceCommunicationObjectToUser(user, assistanceCommunicationObject);
                assistanceStatements.add(statementFactory.generateStatement(
                        user.getActorAccountName(), tudAssistanceBackboneUrl, "got_assisted_by", tudAssistanceBackboneUrl));
            }
        }
        try {
            statementsApi.storeStatements(assistanceStatements);
        } catch (Exception e) {
            log.error("Failed to send statement(s) for initiated assistance to Learning Locker - {}!", e.getMessage());
        }
    }

    @Override
    public void removeExpiredAssistanceCommunicationObjects(LocalDateTime expiredBeforeDate) {
        List<AssistanceCommunicationObject> expiredAssistanceCommunicationObjects =
                assistanceCommunicationObjectRepository.getAssistanceCommunicationObjectsByTimestampBefore(
                        expiredBeforeDate);
        if (expiredAssistanceCommunicationObjects.isEmpty()) {
            return;
        }
        assistanceCommunicationObjectRepository.deleteAll(expiredAssistanceCommunicationObjects);
    }

    private AssistanceParameter getAssistanceParameterByKey(AssistanceCommunicationObject assistanceCommunicationObject,
                                                            String assistanceParameterKey) {
        return assistanceCommunicationObject.getParameters()
                .stream()
                .filter(parameter -> parameter.getKey().equals(assistanceParameterKey))
                .findAny()
                .orElse(null);
    }

    private void handleJustLoggedInAssistanceCommunicationObjectFromUser(
            User user, AssistanceCommunicationObject assistanceCommunicationObject,
            AssistanceParameter justLoggedInParameter) {
        boolean justLoggedIn =
                Boolean.parseBoolean(Objects.requireNonNull(justLoggedInParameter.getValue()).toString());

        if (justLoggedIn) {
            // Send all communication objects if just logged in is true
            try {
                // Load previous messages
                List<AssistanceCommunicationObject> previousMessages =
                        tutorialModuleApi.searchForAssistanceObjects(
                                        List.of(new AssistanceParameterSearchParameter()
                                                .key("userId")
                                                .value(user.getActorAccountName())),
                                        null, null).getAssistanceObjectRecords()
                                .stream()
                                .map(assistanceCommunicationObjectMapper::toAssistanceCommunicationObject)
                                .sorted(Comparator.comparing(AssistanceCommunicationObject::getTimestamp))
                                .toList();
                // Reset unacknowledged messages
                assistanceCommunicationObjectRepository.deleteAll();
                // Save object
                AssistanceCommunicationObject objectToSend = new AssistanceCommunicationObject(
                        timeProvider.getCurrentTime(), user.getId().toString(),
                        List.of(new AssistanceParameter()
                                .key(ASSISTANCE_PARAMETER_KEY_PREVIOUS_MESSAGES)
                                .value(previousMessages)));
                assistanceCommunicationObjectRepository.save(objectToSend);
                // Send object
                chatbotMessageService.sendAssistanceCommunicationObjectToUser(user, objectToSend);
            } catch (Exception e) {
                log.error("Failed to load previous messages for user {}!", user.getActorAccountName());
            }
        } else {
            // Send unacknowledged communication objects if just logged in is false
            List<AssistanceCommunicationObject> unacknowledgedMessages = assistanceCommunicationObjectRepository
                    .getAssistanceCommunicationObjectsByUserIdOrderByTimestamp(user.getId().toString());
            AssistanceCommunicationObject objectToSend = new AssistanceCommunicationObject(
                    List.of(new AssistanceParameter()
                            .key(ASSISTANCE_PARAMETER_KEY_UNACKNOWLEDGED_OBJECTS)
                            .value(unacknowledgedMessages)));
            chatbotMessageService.sendAssistanceCommunicationObjectToUser(user, objectToSend);
        }

        // Send login statement to LRS
        if (justLoggedIn) {
            String lmsUrlParameter = Objects.requireNonNull(Objects.requireNonNull(
                            getAssistanceParameterByKey(
                                    assistanceCommunicationObject, ASSISTANCE_PARAMETER_KEY_LMS_URL))
                    .getValue()).toString();
            userService.updateLastLoggedInLmsUrl(user.getId(), lmsUrlParameter);
            try {
                statementsApi.storeStatements(List.of(statementFactory.generateStatement(
                        user.getActorAccountName(), lmsUrlParameter, "loggedin", lmsUrlParameter)));
            } catch (Exception e) {
                log.error("Sending of login statement to Learning Locker failed!");
            }
        }
    }
}
