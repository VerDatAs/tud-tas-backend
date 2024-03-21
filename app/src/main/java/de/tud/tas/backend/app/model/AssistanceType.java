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
package de.tud.tas.backend.app.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;

/**
 * This class encapsulates the parameters used to describe an assistance type.
 */
@Document
@AllArgsConstructor
@Getter
@Setter
public class AssistanceType implements Comparable<AssistanceType> {
    /**
     * The key of the assistance type.
     */
    @Id
    private String key;
    /**
     * The features that have to be activated to support the assistance type.
     */
    private List<Feature> requiredFeatures;

    @Override
    public int compareTo(AssistanceType assistanceType) {
        return this.key.compareTo(assistanceType.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof AssistanceType && ((AssistanceType) obj).key.equals(this.key));

    }
}
