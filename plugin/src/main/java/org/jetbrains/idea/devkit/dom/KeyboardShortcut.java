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
 * plugin.dtd:keyboard-shortcut interface.
 */
public interface KeyboardShortcut extends DomElement {

  /**
   * Returns the value of the first-keystroke child.
   * Attribute first-keystroke
   *
   * @return the value of the first-keystroke child.
   */
  @Nonnull
  @Required
  GenericAttributeValue<String> getFirstKeystroke();


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
   * Returns the value of the use-shortcut-of child.
   * Attribute use-shortcut-of
   *
   * @return the value of the use-shortcut-of child.
   */
  @Nonnull
  GenericAttributeValue<String> getUseShortcutOf();


  /**
   * Returns the value of the second-keystroke child.
   * Attribute second-keystroke
   *
   * @return the value of the second-keystroke child.
   */
  @Nonnull
  GenericAttributeValue<String> getSecondKeystroke();


  /**
   * Returns the value of the should current shortcut be removed or not.
   * Attribute remove option
   *
   * @return the value of the should current shortcut be removed or not.
   */
  @Nonnull
  GenericAttributeValue<String> getRemove();

  /**
   * Returns the value of the should all previous shortcuts be removed by that one or not.
   * Attribute remove option
   *
   * @return the value of the should all previous shortcuts be removed by that one or not.
   */
  @Nonnull
  GenericAttributeValue<String> getReplaceAll();

}
