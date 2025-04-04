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
package org.intellij.grammar.impl.editor;

import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;
import org.intellij.grammar.psi.BnfRule;

/**
 * @author gregsh
 */
public class BnfExpressionMarkerAnnotator implements Annotator {
    @Override
    public void annotate(@Nonnull PsiElement psiElement, @Nonnull AnnotationHolder annotationHolder) {
        if (!(psiElement instanceof BnfRule)) {
            return;
        }
        BnfRule rule = (BnfRule)psiElement;
        // todo
        //boolean expression = ExpressionGeneratorHelper.getInfoForExpressionParsing(ExpressionHelper.getCached((BnfFile)rule.getContainingFile()), rule) != null;
        //if (expression) {
        //  annotationHolder.createInfoAnnotation(rule.getNameIdentifier(), null).setTextAttributes(key);
        //}
    }

}
