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

import java.util.List;
import java.util.UUID;

import de.tud.tas.backend.app.model.User;
import de.tud.tas.backend.app.model.UserLanguage;
import de.tud.tas.backend.app.model.UserRole;

/**
 * This is the class for handling users.
 */
public interface UserService {

    /**
     * The function to get all saved users.
     *
     * @return all saved users
     */
    List<User> getAllUsers();

    /**
     * The function to get a specific user.
     *
     * @param userId ID of the user
     * @return specific user
     */
    User getUser(UUID userId);

    /**
     * The function to get a specific user by their actor account name.
     *
     * @param actorAccountName actor account name of the user
     * @return specific user
     */
    User getUserByActorAccountName(String actorAccountName);

    /**
     * The function to get a specific user by their actor account name or to create a user if it does not exist.
     *
     * @param actorAccountName actor account name of the user
     * @return specific user
     */
    User getUserByActorAccountNameOrAddUser(String actorAccountName);

    /**
     * The function to get a specific user by their actor account name or ID.
     *
     * @param userIdOrActorAccountName ID or actor account name of the user
     * @return specific user
     */
    User getUserByIdOrActorAccountName(String userIdOrActorAccountName);

    /**
     * The function for adding a new user.
     *
     * @param actorAccountName actor account name of the user
     * @param role             role of the user
     * @return new created user
     */
    User addUser(String actorAccountName, UserRole role);

    /**
     * The function for adding a new user.
     *
     * @param actorAccountName actor account name of the user
     * @param role             role of the user
     * @param password         password of the user
     * @return new created user
     */
    User addUser(String actorAccountName, UserRole role, String password);

    /**
     * The function for deleting a user.
     *
     * @param actorAccountName actor account name of the user
     */
    void deleteUser(String actorAccountName);

    /**
     * The function to update the longLivedTokenId of a given user.
     *
     * @param userId           ID of the user
     * @param longLivedTokenId new long-lived token ID
     */
    void updateLongLivedTokenId(UUID userId, UUID longLivedTokenId);

    /**
     * The function to update the lastLoggedInLmsUrl of a given user.
     *
     * @param userId             ID of the user
     * @param lastLoggedInLmsUrl new last logged in LMS URL
     */
    void updateLastLoggedInLmsUrl(UUID userId, String lastLoggedInLmsUrl);

    /**
     * The function to update the role of a specific user.
     *
     * @param userId ID of the user
     * @param role   new role
     * @return new updated role
     */
    UserRole updateUserRole(UUID userId, UserRole role);

    /**
     * The function to update the language of a specific user.
     *
     * @param userId   ID of the user
     * @param language new language
     * @return new updated language
     */
    UserLanguage updateUserLanguage(UUID userId, UserLanguage language);

    /**
     * The function to update the password of a specific user.
     *
     * @param user        the user
     * @param rawPassword new password in raw format
     */
    void updatePassword(User user, String rawPassword);

    /**
     * The function to update the password of a specific user.
     *
     * @param userId      ID of the User
     * @param rawPassword new password in raw format
     */
    void updatePassword(UUID userId, String rawPassword);
}
