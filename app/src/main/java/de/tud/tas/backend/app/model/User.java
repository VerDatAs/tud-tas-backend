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
package de.tud.tas.backend.app.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * This class encapsulates the parameters used to describe a User.
 *
 */
@Document
@NoArgsConstructor
@Getter
@Setter
public class User {
    /**
     * The ID of a user.
     */
    @Id
    private UUID id;
    /**
     * The name of a user.
     */
    @Indexed(unique = true)
    private String actorAccountName;
    /**
     * The password of a user.
     */
    private String password;
    /**
     * The ID for a long-lived token of a user.
     */
    private UUID longLivedTokenId;
    /**
     * The URL of the last LMS the user was observed as logged in.
     */
    private String lastLoggedInLmsUrl;
    /**
     * The language of a user.
     */
    private UserLanguage language;
    /**
     * The role of a user.
     */
    private UserRole role;

    /**
     * This is the constructor of the class.
     *
     * @param id ID of a user
     * @param actorAccountName name of a user
     * @param language language of a user
     * @param role role of a user
     */
    public User(UUID id, String actorAccountName, UserLanguage language, UserRole role) {
        this.id = id;
        this.actorAccountName = actorAccountName;
        this.language = language;
        this.role = role;
    }
}
