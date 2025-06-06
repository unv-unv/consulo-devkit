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
package org.intellij.grammar.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.CodeDocumentationAwareCommenter;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiComment;
import org.intellij.grammar.BnfLanguage;
import org.intellij.grammar.BnfParserDefinition;

import jakarta.annotation.Nonnull;

/**
 * @author gregsh
 */
@ExtensionImpl
public class BnfCommenter implements CodeDocumentationAwareCommenter {
    @Override
    public String getLineCommentPrefix() {
        return "//";
    }

    @Override
    public String getBlockCommentPrefix() {
        return "/*";
    }

    @Override
    public String getBlockCommentSuffix() {
        return "*/";
    }

    @Override
    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Override
    public String getCommentedBlockCommentSuffix() {
        return null;
    }

    @Override
    public IElementType getLineCommentTokenType() {
        return BnfParserDefinition.BNF_LINE_COMMENT;
    }

    @Override
    public IElementType getBlockCommentTokenType() {
        return BnfParserDefinition.BNF_BLOCK_COMMENT;
    }

    @Override
    public IElementType getDocumentationCommentTokenType() {
        return null;
    }

    @Override
    public String getDocumentationCommentPrefix() {
        return null;
    }

    @Override
    public String getDocumentationCommentLinePrefix() {
        return null;
    }

    @Override
    public String getDocumentationCommentSuffix() {
        return null;
    }

    @Override
    public boolean isDocumentationComment(PsiComment element) {
        return false;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return BnfLanguage.INSTANCE;
    }
}
