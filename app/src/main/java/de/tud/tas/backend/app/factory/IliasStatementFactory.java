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
package de.tud.tas.backend.app.factory;

import de.tud.tas.backend.app.provision.TimeProvider;
import de.tud.tas.backend.learning_locker_api_client.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
@Service
@RequiredArgsConstructor
public class IliasStatementFactory implements StatementFactory {

    private final TimeProvider timeProvider;

    private final String ACTOR_NAME = "-";
    private final String ACTOR_OBJECT_TYPE = "Agent";
    private final String AUTHORITY_MBOX = "mailto:hello@learninglocker.net";
    private final String AUTHORITY_NAME = "New Client";
    private final String AUTHORITY_OBJECT_TYPE = "Agent";
    private final String DEFINITION_NAME = "TUD_TAS_BACKEND";
    private final String DEFINITION_TYPE = "http://id.tincanapi.com/activitytype/lms";
    private final String MODEL_OBJECT_MODEL_TYPE = "Activity";
    private final String VERB_PREFIX = "https://brindlewaye.com/xAPITerms/verbs/";

    @Override
    public Statement generateStatement(String actorName, String actorHomePage, String verbName, String objectId) {
        return (new Statement())
                .id(UUID.randomUUID().toString())
                .authority((new Authority())
                        .objectType(AUTHORITY_OBJECT_TYPE)
                        .name(AUTHORITY_NAME)
                        .mbox(AUTHORITY_MBOX))
                .actor((new Actor())
                        .objectType(ACTOR_OBJECT_TYPE)
                        .name(ACTOR_NAME)
                        .account((new ActorAccount())
                                .name(actorName)
                                .homePage(actorHomePage)))
                .verb((new Verb())
                        .id(VERB_PREFIX + verbName + "/")
                        .display((new VerbDisplay())
                                .enUS(verbName)))
                ._object((new ModelObject())
                        .id(objectId)
                        .definition((new Definition())
                                .type(DEFINITION_TYPE)
                                .name((new DefinitionName())
                                        .enUS(DEFINITION_NAME))
                                .description((new DefinitionName())
                                        .enUS(DEFINITION_NAME)))
                        .objectType(MODEL_OBJECT_MODEL_TYPE));
    }
}
