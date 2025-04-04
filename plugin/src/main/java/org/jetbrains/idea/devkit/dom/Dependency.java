/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

// Generated on Wed Nov 07 17:26:02 MSK 2007
// DTD/Schema  :    plugin.dtd

package org.jetbrains.idea.devkit.dom;

import consulo.container.plugin.PluginDescriptor;
import consulo.devkit.dom.impl.PluginDescriptorResolver;
import consulo.language.psi.path.PathReference;
import consulo.xml.util.xml.Convert;
import consulo.xml.util.xml.GenericAttributeValue;
import consulo.xml.util.xml.GenericDomValue;
import consulo.xml.util.xml.Stubbed;
import jakarta.annotation.Nonnull;

@Stubbed
@Convert(PluginDescriptorResolver.class)
public interface Dependency extends GenericDomValue<PluginDescriptor> {
    @Nonnull
    GenericAttributeValue<Boolean> getOptional();

    @Nonnull
    @Convert(DependencyConfigFileConverter.class)
    GenericAttributeValue<PathReference> getConfigFile();
}
