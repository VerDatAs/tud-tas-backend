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
package de.tud.tas.backend.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import de.tud.tas.backend.app.model.AssistanceCommunicationObject;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceBundle;

/**
 * This is a class to handle logging of different models.
 */
public interface LearningRecordLoggingService {
    /**
     * The function to log a statement.
     *
     * @param statement statement to log
     */
    void logStatement(JsonNode statement);

    /**
     * The function to log an assistance bundle.
     *
     * @param assistanceBundle assistance bundle to log
     */
    void logAssistanceBundle(AssistanceBundle assistanceBundle);

    /**
     * The function to log an assistance object communication.
     *
     * @param assistanceCommunicationObject assistance object communication to log
     */
    void logAssistanceCommunicationObject(AssistanceCommunicationObject assistanceCommunicationObject);
}
