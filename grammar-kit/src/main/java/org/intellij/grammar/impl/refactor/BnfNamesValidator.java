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
package org.intellij.grammar.impl.refactor;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.refactoring.NamesValidator;
import consulo.project.Project;
import org.intellij.grammar.BnfLanguage;
import org.intellij.grammar.parser.BnfLexer;
import org.intellij.grammar.psi.BnfTypes;

import jakarta.annotation.Nonnull;

/**
 * @author gregsh
 */
@ExtensionImpl
public class BnfNamesValidator implements NamesValidator {
    @Override
    public boolean isKeyword(@Nonnull String s, Project project) {
        return false;
    }

    @Override
    public boolean isIdentifier(@Nonnull String s, Project project) {
        BnfLexer lexer = new BnfLexer();
        lexer.start(s);
        return lexer.getTokenEnd() == s.length() && lexer.getTokenType() == BnfTypes.BNF_ID;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return BnfLanguage.INSTANCE;
    }
}
