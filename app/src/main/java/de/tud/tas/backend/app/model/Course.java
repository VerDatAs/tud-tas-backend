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
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * This class encapsulates the parameters that describe a course.
 *
 */
@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    /**
     * The object ID of the course Base64Url encoded.
     */
    @Id
    private String objectId;
    /**
     * The assistance types of the course, which describe whether a specific assistance type is enabled for the course or not, and the pre-condition that has to be fulfilled.
     */
    private List<CourseAssistanceType> courseAssistanceTypes;
    /**
     * The features of the course that describe whether a particular feature is enabled for the course.
     */
    private List<CourseFeature> courseFeatures;
}
