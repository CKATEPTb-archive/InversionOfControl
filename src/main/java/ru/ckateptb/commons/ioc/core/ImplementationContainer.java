/*
 * Copyright (c) 2022 CKATEPTb <https://github.com/CKATEPTb>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.ckateptb.commons.ioc.core;


import ru.ckateptb.commons.ioc.exceptions.IoCException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ImplementationContainer {
    private final Map<Class<?>, Class<?>> implementationsMap = new HashMap<>(10);

    public void putImplementationClass(Class<?> implementationClass, Class<?> interfaceClass) {
        implementationsMap.put(implementationClass, interfaceClass);
    }

    public Class<?> getImplementationClass(Class<?> interfaceClass, final String fieldName, final String qualifier) {
        Set<Map.Entry<Class<?>, Class<?>>> implementationClasses =
                implementationsMap.entrySet().stream().filter(entry ->
                        entry.getValue() == interfaceClass).collect(Collectors.toSet());
        String errorMessage;
        if (implementationClasses.isEmpty()) {
            errorMessage = "No implementation found for interface " + interfaceClass.getName();
        } else if (implementationClasses.size() == 1) {
            Optional<Map.Entry<Class<?>, Class<?>>> optional = implementationClasses.stream().findFirst();
            return optional.get().getKey();
        } else {
            final String findBy = (qualifier == null || qualifier.trim().length() == 0) ? fieldName : qualifier;
            Optional<Map.Entry<Class<?>, Class<?>>> optional =
                    implementationClasses.stream()
                            .filter(entry ->
                                    entry.getKey().getSimpleName()
                                            .equalsIgnoreCase(findBy)).findAny();
            if (optional.isPresent()) {
                return optional.get().getKey();
            } else {
                errorMessage = "There are " + implementationClasses.size()
                        + " of interface " + interfaceClass.getName()
                        + " Expected single implementation or make use of"
                        + " @Qualifier to resolve conflict";
            }
        }
        throw new IoCException(errorMessage);
    }
}