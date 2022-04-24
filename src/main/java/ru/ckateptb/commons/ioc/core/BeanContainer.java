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
import java.util.TreeMap;

public class BeanContainer {
    public final Map<Class<?>, Map<String, Object>> beans = new HashMap<>(10);

    public void putBean(Class<?> clazz, Object instance) {
        putBean(clazz, instance, clazz.getName());
    }

    public void putBean(Class<?> clazz, Object instance, String name) {
        Map<String, Object> map = beans.computeIfAbsent(clazz, k -> new TreeMap<>());
        map.putIfAbsent(name, instance);
    }

    public boolean containsBean(Class<?> clazz) {
        return containsBean(clazz, clazz.getName());
    }

    public boolean containsBean(Class<?> clazz, String name) {
        return Optional.ofNullable(beans.get(clazz)).map(map -> map.containsKey(name)).orElse(false);
    }

    public Object getBean(Class<?> clazz) {
        return getBean(clazz, clazz.getName());
    }

    public Object getBean(Class<?> clazz, String name) {
        Map<String, Object> map = beans.get(clazz);

        if (map == null || map.size() == 0) {
            throw new IoCException("No bean found for class " + clazz);
        }

        if (map.size() == 1) {
            return map.values().iterator().next();
        }

        Object bean = map.get(name);
        if (bean == null) {
            String errorMessage = "There are " + map.size()
                    + " of bean " + name
                    + " Expected single implementation or make use of"
                    + " @Qualifier to resolve conflict";
            throw new IoCException(errorMessage);
        }

        return bean;
    }
}