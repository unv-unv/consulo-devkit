/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package org.jetbrains.idea.devkit.dom;

import consulo.xml.util.xml.*;
import jakarta.annotation.Nonnull;

/**
 * @since 15
 */
public interface ActionOrGroup extends DomElement {
    @Nonnull
    @NameValue
    @Stubbed
    @Required(value = false, nonEmpty = true)
    GenericAttributeValue<String> getId();
}
