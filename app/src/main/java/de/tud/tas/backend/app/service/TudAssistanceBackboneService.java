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
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceBundle;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceInitiationRequestParameter;

import java.util.List;

/**
 * This is a class that handles communication with the TUD Assistance Backbone.
 */
public interface TudAssistanceBackboneService {
    /**
     * The function for handling a statement.
     *
     * @param statement statement to be sent
     */
    void handleStatement(JsonNode statement);

    /**
     * The function to initiate assistance.
     *
     * @param type       type of the assistance
     * @param parameters parameters to initiate assistance
     */
    AssistanceBundle initiateAssistance(String type, List<AssistanceInitiationRequestParameter> parameters);
}
