/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Robert Schmidt, Sebastian Kucharski, Tommy Kubica)
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

import de.tud.tas.backend.app.model.LongLivedToken;
import de.tud.tas.backend.app.model.User;
import de.tud.tas.backend.app.model.UserRole;

import java.util.UUID;

/**
 * This is a class that handles the authentication to the backend.
 */
public interface AuthService {

    /**
     * The function for creating a token and logging in a user.
     *
     * @param actorAccountName name of the user
     * @param rawPassword      password of the user
     * @return JWT token for the user
     */
    String login(String actorAccountName, String rawPassword);

    /**
     * The function to create a JWT for a specific user.
     *
     * @param user the user to create the JWT for
     * @return JWT token for the user
     */
    String createJwt(User user);

    /**
     * The function to create a JWT for a specific user ID with a given role.
     *
     * @param userId the user ID to create the JWT for
     * @param role the role to use in the token
     * @return JWT token for the user
     */
    String createJwt(String userId, UserRole role);

    /**
     * The function to create a JWT for a specific user that does not expire.
     *
     * @param user             the user to create the JWT for
     * @param longLivedTokenId the ID of the long-lived token
     * @return JWT token for the user
     */
    String createLongLivedJwt(User user, UUID longLivedTokenId);

    /**
     * The function that allows the creation of a long-lived token for a user, or the retrieval of an existing token from a user.
     *
     * @param userId ID of the user
     * @return LongLivedToken of the user
     */
    LongLivedToken createLongLivedTokenOrGetTokenId(UUID userId);
}
