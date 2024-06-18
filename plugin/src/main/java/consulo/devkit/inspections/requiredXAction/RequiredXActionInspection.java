/*
 * Copyright 2013-2016 consulo.io
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

package consulo.devkit.inspections.requiredXAction;

import com.intellij.java.analysis.impl.codeInspection.AnnotateMethodFix;
import com.intellij.java.language.psi.*;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.devkit.inspections.requiredXAction.stateResolver.AnonymousClassStateResolver;
import consulo.devkit.inspections.requiredXAction.stateResolver.LambdaStateResolver;
import consulo.devkit.inspections.requiredXAction.stateResolver.MethodReferenceResolver;
import consulo.devkit.inspections.requiredXAction.stateResolver.StateResolver;
import consulo.devkit.localize.DevKitLocalize;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;
import org.jetbrains.idea.devkit.inspections.internal.InternalInspection;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18.02.2015
 */
@ExtensionImpl
public class RequiredXActionInspection extends InternalInspection {
  public static class RequiredXActionVisitor extends JavaElementVisitor {
    private final ProblemsHolder myHolder;

    public RequiredXActionVisitor(ProblemsHolder holder) {
      myHolder = holder;
    }

    @Override
    @RequiredReadAction
    public void visitMethodReferenceExpression(PsiMethodReferenceExpression expression) {
      PsiElement psiElement = expression.resolve();
      if (!(psiElement instanceof PsiMethod)) {
        return;
      }

      reportError(expression, (PsiMethod)psiElement, MethodReferenceResolver.INSTANCE);
    }

    @Override
    @RequiredReadAction
    public void visitCallExpression(PsiCallExpression expression) {
      PsiMethod psiMethod = expression.resolveMethod();
      if (psiMethod == null) {
        return;
      }

      reportError(expression, psiMethod, LambdaStateResolver.INSTANCE, AnonymousClassStateResolver.INSTANCE);
    }

    @RequiredReadAction
    private void reportError(PsiExpression expression, PsiMethod psiMethod, StateResolver... stateResolvers) {
      CallStateType actionType = CallStateType.findActionType(psiMethod);
      if (actionType == CallStateType.NONE) {
        return;
      }

      for (StateResolver stateResolver : stateResolvers) {
        Boolean state = stateResolver.resolveState(actionType, expression);
        if (state == null) {
          continue;
        }

        if (state) {
          break;
        }

        reportError(expression, actionType);
        break;
      }
    }

    private void reportError(@Nonnull PsiExpression expression, @Nonnull CallStateType type) {
      LocalQuickFix[] quickFixes = LocalQuickFix.EMPTY_ARRAY;
      LocalizeValue text;
      switch (type) {
        case READ:
        case WRITE:
          text = DevKitLocalize.inspectionsAnnotation0IsRequiredAtOwnerOrAppRun(StringUtil.capitalize(type.name().toLowerCase()));
          break;
        case UI_ACCESS:
          text = DevKitLocalize.inspectionsAnnotation0IsRequiredAtOwnerOrAppRunUi();
          quickFixes = new LocalQuickFix[]{new AnnotateMethodFix(RequiredUIAccess.class.getName())};
          break;
        default:
          throw new IllegalArgumentException();
      }
      myHolder.registerProblem(expression, text.get(), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, quickFixes);
    }
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Invocation state(read, write, dispatch) validate inspection";
  }

  @Nonnull
  @Override
  public PsiElementVisitor buildInternalVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly) {
    return new RequiredXActionVisitor(holder);
  }
}
