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
package de.tud.tas.backend.app.util;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class StreamHelper {

    /**
     * Helper function to filter within a lambda stream. Only the elements that, with regard to a user defined key, are
     * contained in a given collection are kept.
     *
     * @param collection   The collection that should be checked for incoming elements.
     * @param keyExtractor The function to determine which key should be used for the collection contains element check.
     * @param <T>          The type of the objects to filter.
     * @return A predicate used to determine if an element should be filtered out or not.
     */
    public static <T> Predicate<T> isContainedInCollection(Collection<T> collection,
                                                           Function<? super T, Object> keyExtractor) {
        return t -> collection.stream()
                .filter(e -> keyExtractor.apply(t).equals(keyExtractor.apply(e)))
                .findAny()
                .orElse(null) != null;
    }

    /**
     * Helper function to filter within a lambda stream. Only the elements that, with regard to a user defined key, are
     * not contained in a given collection are kept.
     *
     * @param collection   The collection that should be checked for incoming elements.
     * @param keyExtractor The function to determine which key should be used for the collection contains element check.
     * @param <T>          The type of the objects to filter.
     * @return A predicate used to determine if an element should be filtered out or not.
     */
    public static <T> Predicate<T> isNotContainedInCollection(Collection<T> collection,
                                                              Function<? super T, Object> keyExtractor) {
        return t -> collection.stream()
                .filter(e -> keyExtractor.apply(t).equals(keyExtractor.apply(e)))
                .findAny()
                .orElse(null) == null;
    }

}
