/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

import consulo.xml.util.xml.DomElement;
import consulo.xml.util.xml.GenericAttributeValue;
import consulo.xml.util.xml.Required;

import jakarta.annotation.Nonnull;

/**
 * plugin.dtd:mouse-shortcut interface.
 */
public interface MouseShortcut extends DomElement {

  /**
   * Returns the value of the keymap child.
   * Attribute keymap
   *
   * @return the value of the keymap child.
   */
  @Nonnull
  @Required
  GenericAttributeValue<String> getKeymap();


  /**
   * Returns the value of the keystroke child.
   * Attribute keystroke
   *
   * @return the value of the keystroke child.
   */
  @Nonnull
  @Required
  GenericAttributeValue<String> getKeystroke();
}
