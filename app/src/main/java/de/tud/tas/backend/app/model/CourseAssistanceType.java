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
package de.tud.tas.backend.app.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * This class encapsulates the parameters used to describe a course assistance type.
 *
 */
@AllArgsConstructor
@Getter
@Setter
public class CourseAssistanceType {
    /**
     * The key of the assistance type.
     */
    private String key;
    /**
     * This indicates whether the assistance type is enabled or not.
     */
    private boolean enabled;
    /**
     * Whether the features that must be set before the assistance type is enabled are set.
     */
    private boolean preConditionFulfilled;
}
