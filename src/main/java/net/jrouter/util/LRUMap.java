/*
 * Copyright (C) 2010-2111 sunjumper@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.jrouter.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple LRU cache that implements the{@code Map} interface. Instances are not
 * thread-safe and should be synchronized externally, for instance by using
 * {@link java.util.Collections#synchronizedMap}.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class LRUMap<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;

    /**
     * The max number of key-value mappings contained in this map.
     */
    private final int maxEntries;

    /**
     * 构造一个带指定最大条目数的空{@code LRUMap}实例。
     * @param maxEntries 最大条目数。
     */
    public LRUMap(int maxEntries) {
        this(128, maxEntries);
    }

    /**
     * 构造一个带指定初始容量、最大条目数的空{@code LRUMap}实例。
     * @param initialEntries 初始容量。
     * @param maxEntries 最大条目数。
     */
    public LRUMap(int initialEntries, int maxEntries) {
        super(initialEntries, .75f, true);
        this.maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxEntries;
    }

}
