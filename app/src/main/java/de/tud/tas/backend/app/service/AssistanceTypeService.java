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


import de.tud.tas.backend.app.dto.AssistanceTypeDto;
import de.tud.tas.backend.app.model.AssistanceType;
import de.tud.tas.backend.app.model.Course;

import java.util.List;

/**
 * This is a class for handling assistance types.
 */

public interface AssistanceTypeService {
    /**
     * The function to remove all assistance types that do not exist in the TUD Assistance Backbone.
     */
    void syncAssistanceTypes();

    /**
     * The function to get all assistance types that are supported by the TUD Assistance Backbone with the stored
     * precondition features.
     *
     * @return all assistance types
     */
    List<AssistanceTypeDto> getAssistanceTypeDtos();

    /**
     * The function to set the stored assistance types. Only assistance types with configured preconditions are
     * persisted.
     *
     * @param assistanceTypes assistance types to set.
     * @return new updated assistance types
     */
    List<AssistanceType> setAssistanceTypes(List<AssistanceType> assistanceTypes);

    /**
     * The function to update the course assistance types of a course.
     *
     * @param course                  course
     * @param assistanceTypeKeysToSet set of assistance types, where the preconditions have to be checked
     */
    void updateCourseAssistanceTypesPreconditionFulfilledByKeys(Course course, List<String> assistanceTypeKeysToSet);
}
