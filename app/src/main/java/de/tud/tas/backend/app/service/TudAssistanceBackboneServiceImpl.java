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

import com.fasterxml.jackson.databind.JsonNode;
import de.tud.tas.backend.app.dto.CourseDto;
import de.tud.tas.backend.app.exceptions.CourseNotFoundException;
import de.tud.tas.backend.app.factory.StatementFactory;
import de.tud.tas.backend.app.mapper.CourseMapper;
import de.tud.tas.backend.app.model.CourseAssistanceType;
import de.tud.tas.backend.app.model.User;
import de.tud.tas.backend.learning_locker_api_client.api.StatementsApi;
import de.tud.tas.backend.learning_locker_api_client.model.Statement;
import de.tud.tas.backend.tud_assistance_backbone_api_client.api.TutorialModuleApi;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TudAssistanceBackboneServiceImpl implements TudAssistanceBackboneService {
    @Value("${tud-assistance-backbone.url}")
    private String tudAssistanceBackboneUrl;

    private final String USER_ID_ASSISTANCE_PARAMETER_KEY = "learner_id";

    private static final Logger log = LoggerFactory.getLogger(TudAssistanceBackboneServiceImpl.class);

    private final AssistanceTypeService assistanceTypeService;
    private final CourseMapper courseMapper;
    private final CourseService courseService;
    private final LearningRecordLoggingService learningRecordLoggingService;
    private final StatementFactory statementFactory;
    private final StatementsApi statementsApi;
    private final TutorialModuleApi tutorialModuleApi;
    private final UserService userService;

    @Override
    public void handleStatement(JsonNode statement) {
        learningRecordLoggingService.logStatement(statement);

        final StatementProcessingRequest statementProcessingRequest = (new StatementProcessingRequest())
                .statement(statement)
                .supportedAssistanceTypes(assistanceTypeService.getAssistanceTypeDtos()
                        .stream()
                        .map(assistanceType ->
                                new StatementProcessingRequestSupportedAssistanceTypesInner()
                                        .key(assistanceType.getKey()))
                        .toList());

        final String courseObjectId = getCourseObjectId(statement);
        if (courseObjectId == null) {
            log.info("Course object ID for received statement {} can not be determined.", statement.get("id"));
            tutorialModuleApi.processXapiStatement(
                    statementProcessingRequest);
            return;
        }

        String courseObjectIdEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(courseObjectId.getBytes());
        CourseDto courseDto;
        try {
            courseDto = courseService.getCourseDto(courseObjectIdEncoded);
        } catch (CourseNotFoundException e) {
            log.info("Course referenced in statement {} is not known.", statement.get("id"));
            tutorialModuleApi.processXapiStatement(statementProcessingRequest);
            return;
        }
        log.info("Handle statement for course - " + courseObjectIdEncoded);

        List<StatementProcessingRequestSupportedAssistanceTypesInner> courseAssistanceTypes = courseMapper
                .toCourse(courseDto)
                .getCourseAssistanceTypes()
                .stream()
                .filter(CourseAssistanceType::isEnabled)
                .filter(CourseAssistanceType::isPreConditionFulfilled)
                .map(courseAssistanceType ->
                        new StatementProcessingRequestSupportedAssistanceTypesInner()
                                .key(courseAssistanceType.getKey()))
                .toList();
        tutorialModuleApi.processXapiStatement(
                (new StatementProcessingRequest())
                        .statement(statement)
                        .supportedAssistanceTypes(courseAssistanceTypes));
    }

    private String getCourseObjectId(JsonNode statement) {
        String courseTypeString = "http://adlnet.gov/expapi/activities/course";

        if (statement.has("object")
                && statement.get("object").has("definition")
                && statement.get("object").get("definition").has("type")
                && statement.get("object").get("definition").get("type").asText().equals(courseTypeString)) {
            return statement.get("object").get("id").asText();
        }

        if (!statement.has("context") || !statement.get("context").has("contextActivities")) {
            return null;
        }
        JsonNode parent = null;
        if (statement.get("context").get("contextActivities").has("parent")) {
            parent = statement.get("context").get("contextActivities").get("parent").get(0);
        }
        if (parent != null
                && parent.has("definition")
                && parent.get("definition").has("type")
                && parent.get("definition").get("type").asText().equals(courseTypeString)) {
            return parent.get("id").asText();
        }

        JsonNode grouping = statement.get("context").get("contextActivities").get("grouping");
        if (grouping != null) {
            for (JsonNode groupingElement : grouping) {
                if (!groupingElement.has("definition") || !groupingElement.get("definition").has("type")) {
                    continue;
                }
                if (groupingElement.get("definition").get("type").asText().equals(courseTypeString)) {
                    return groupingElement.get("id").asText();
                }
            }
        }

        return null;
    }

    @Override
    public AssistanceBundle initiateAssistance(String type, List<AssistanceInitiationRequestParameter> parameters) {
        AssistanceInitiationRequest assistanceInitiationRequest = new AssistanceInitiationRequest();
        assistanceInitiationRequest.setType(type);

        AssistanceInitiationRequestParameter userIdParameter = parameters.stream()
                .filter(p -> p.getKey().equals(USER_ID_ASSISTANCE_PARAMETER_KEY))
                .findAny()
                .orElse(null);
        User user = null;
        if (userIdParameter == null) {
            // TODO: Make this configurable
            assistanceInitiationRequest.setLanguage("de");
        } else {
            user = userService.getUser(UUID.fromString((String) userIdParameter.getValue()));
            assistanceInitiationRequest.setLanguage(user.getLanguage().toString());
        }
        assistanceInitiationRequest.parameters(parameters);

        AssistanceBundle assistanceBundle = tutorialModuleApi.initiateAssistanceProcess(assistanceInitiationRequest);

        Statement statement = statementFactory.generateStatement(
                user == null ? "anonymous" : user.getActorAccountName(), tudAssistanceBackboneUrl, "got_assisted_by", tudAssistanceBackboneUrl);
        try {
            statementsApi.storeStatements(List.of(statement));
        } catch (Exception e) {
            log.error("Failed to send statement for initiated assistance to Learning Locker - {}!", e.getMessage());
        }

        return assistanceBundle;
    }
}
