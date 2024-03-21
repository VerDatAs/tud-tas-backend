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

import de.tud.tas.backend.app.dto.CourseAssistanceTypeDto;
import de.tud.tas.backend.app.dto.CourseDto;
import de.tud.tas.backend.app.dto.CourseFeatureDto;
import de.tud.tas.backend.app.model.CourseAssistanceType;
import de.tud.tas.backend.app.model.CourseFeature;

import java.util.List;

/**
 * This is a class for handling courses.
 */
public interface CourseService {
    /**
     * The function to remove all courses that are not represented by an LCO in the TUD Assistance Backbone.
     */
    void syncCourses();

    /**
     * The function to get a specific course.
     *
     * @param objectId The object ID of the course
     * @return specific course
     */
    CourseDto getCourseDto(String objectId);

    /**
     * The function to retrieve all saved courses.
     *
     * @return all saved courses
     */
    List<CourseDto> getAllCourseDtos();

    /**
     * The function to get the course features of a specific course.
     *
     * @param objectId The object ID of the course
     * @return course features of the course
     */
    List<CourseFeatureDto> getCourseFeatureDtos(String objectId);

    /**
     * The function to update the course features of a specific course.
     *
     * @param objectId       The object ID of the course
     * @param courseFeatures new course features
     * @return new updated course features
     */
    List<CourseFeature> updateCourseFeatures(String objectId, List<CourseFeature> courseFeatures);

    /**
     * The function to get the assistance types of a specific course.
     *
     * @param objectId The object ID of the course
     * @return course assistance types of the course
     */
    List<CourseAssistanceTypeDto> getCourseAssistanceTypeDtos(String objectId);

    /**
     * The function to set the enabled attribute for specific assistance types of a specific course.
     *
     * @param objectId              The object ID of the course
     * @param courseAssistanceTypes course assistance types with new enabled attribute
     * @return new updated course assistance types
     */
    List<CourseAssistanceType> configureCourseAssistanceTypes(String objectId,
                                                              List<CourseAssistanceType> courseAssistanceTypes);
}
