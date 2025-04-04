/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package org.jetbrains.idea.devkit.inspections.quickfix;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiElementFactory;
import com.intellij.java.language.psi.PsiExpression;
import com.intellij.java.language.psi.codeStyle.JavaCodeStyleManager;
import consulo.annotation.access.RequiredReadAction;
import consulo.devkit.localize.DevKitLocalize;
import consulo.language.editor.inspection.LocalQuickFixBase;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiElement;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

/**
 * @author Konstantin Bulenkov
 */
public class ConvertToGrayQuickFix extends LocalQuickFixBase {
    private final int myNum;

    public ConvertToGrayQuickFix(int num) {
        super(DevKitLocalize.useGrayInspectionMessage(num).get(), DevKitLocalize.useGrayInspectionQuickfixFamilyName().get());
        myNum = num;
    }

    @Override
    @RequiredReadAction
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
        final PsiElement element = descriptor.getPsiElement();
        final PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        final PsiExpression expression = factory.createExpressionFromText("com.intellij.ui.Gray._" + myNum, element.getContext());
        final PsiElement newElement = element.replace(expression);
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(newElement);
    }
}
