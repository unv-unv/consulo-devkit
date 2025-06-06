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

import jakarta.annotation.Nonnull;

/**
 * plugin.dtd:vendor interface.
 * Type vendor documentation
 * <pre>
 *     <vendor> tag now could have 'url', 'email' and 'logo' attributes;
 * </pre>
 */
public interface Vendor extends DomElement {

  /**
   * Returns the value of the simple content.
   *
   * @return the value of the simple content.
   */
  @Nonnull
  String getValue();

  /**
   * Sets the value of the simple content.
   *
   * @param value the new value to set
   */
  void setValue(String value);


  /**
   * Returns the value of the email child.
   * Attribute email
   *
   * @return the value of the email child.
   */
  @Nonnull
  GenericAttributeValue<String> getEmail();


  /**
   * Returns the value of the url child.
   * Attribute url
   *
   * @return the value of the url child.
   */
  @Nonnull
  GenericAttributeValue<String> getUrl();
}
