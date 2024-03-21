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

import de.tud.tas.backend.app.model.User;
import de.tud.tas.backend.app.model.AssistanceCommunicationObject;

/**
 * This is a class to handle the sending of messages via a WebSocket connection.
 */
public interface ChatbotMessageService {
    /**
     * The function that allows sending an assistance communication object via a WebSocket connection to a user.
     *
     * @param user                          specific user
     * @param assistanceCommunicationObject assistance communication object to be sent
     */
    void sendAssistanceCommunicationObjectToUser(User user, AssistanceCommunicationObject assistanceCommunicationObject);

}
