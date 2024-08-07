/*
 * Copyright 2011-present Greg Shrago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.intellij.grammar.config;

import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;

import java.util.function.Supplier;

/**
 * @author gregsh
 */
abstract class Option<T> implements Supplier<T> {
    public final String id;
    public final T defValue;

    Option(String id, T defValue) {
        this.id = id;
        this.defValue = defValue;
    }

    public abstract T get();

    String innerValue() {
        return System.getProperty(id);
    }

    static Option<Integer> intOption(String id, int def) {
        return new Option<Integer>(id, def) {
            @Override
            public Integer get() {
                return StringUtil.parseInt(innerValue(), defValue);
            }
        };
    }

    static Option<String> strOption(String id, String def) {
        return new Option<String>(id, def) {
            @Override
            public String get() {
                return ObjectUtil.chooseNotNull(innerValue(), defValue);
            }
        };
    }
}
