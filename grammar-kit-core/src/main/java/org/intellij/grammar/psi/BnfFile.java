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

package org.intellij.grammar.psi;

import com.intellij.psi.PsiFile;
import org.intellij.grammar.KnownAttribute;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

/**
 * @author gregsh
 */
public interface BnfFile extends PsiFile {
  @Nonnull
  List<BnfRule> getRules();

  @Nonnull
  List<BnfAttrs> getAttributes();

  @Nullable
  BnfRule getRule(@Nullable String ruleName);

  @Nullable
  BnfAttr findAttribute(@Nullable BnfRule rule, @Nonnull KnownAttribute<?> knownAttribute, @Nullable String match);

  @Nullable
  <T> T findAttributeValue(@Nullable BnfRule rule, @Nonnull KnownAttribute<T> knownAttribute, @Nullable String match);
}
