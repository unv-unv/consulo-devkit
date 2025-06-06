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

import com.intellij.java.language.psi.PsiClass;
import consulo.devkit.localize.DevKitLocalize;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.awt.UIUtil;
import consulo.virtualFileSystem.ReadonlyStatusHandler;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;

/**
 * @author swr
 */
abstract class BaseFix implements LocalQuickFix {
    protected final PsiElement myElement;
    protected final boolean myOnTheFly;

    protected BaseFix(PsiElement element, boolean onTheFly) {
        myElement = element;
        myOnTheFly = onTheFly;
    }

    @Override
    @RequiredUIAccess
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
        // can happen during batch-inspection if resolution has already been applied
        // to plugin.xml or java class
        if (!myElement.isValid()) {
            return;
        }

        final boolean external = descriptor.getPsiElement().getContainingFile() != myElement.getContainingFile();
        if (external) {
            final PsiClass clazz = PsiTreeUtil.getParentOfType(myElement, PsiClass.class, false);
            final ReadonlyStatusHandler readonlyStatusHandler = ReadonlyStatusHandler.getInstance(project);
            final VirtualFile[] files = new VirtualFile[]{myElement.getContainingFile().getVirtualFile()};
            final ReadonlyStatusHandler.OperationStatus status = readonlyStatusHandler.ensureFilesWritable(files);

            if (status.hasReadonlyFiles()) {
                final String className = clazz != null ? clazz.getQualifiedName() : myElement.getContainingFile().getName();

                Messages.showMessageDialog(
                    project,
                    DevKitLocalize.inspectionsRegistrationProblemsQuickfixReadOnly(className).get(),
                    getName(),
                    UIUtil.getErrorIcon()
                );
                return;
            }
        }

        try {
            doFix(project, descriptor, external);
        }
        catch (IncorrectOperationException e) {
            Logger.getInstance("#" + getClass().getName()).error(e);
        }
    }

    protected abstract void doFix(Project project, ProblemDescriptor descriptor, boolean external) throws IncorrectOperationException;
}
