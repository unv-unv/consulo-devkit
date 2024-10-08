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
package org.jetbrains.idea.devkit.inspections.internal;

import com.intellij.java.language.impl.psi.impl.JavaConstantExpressionEvaluator;
import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.ui.ex.Gray;
import consulo.util.lang.NullUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.idea.devkit.inspections.quickfix.ConvertToGrayQuickFix;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Konstantin Bulenkov
 */
@ExtensionImpl
public class UseGrayInspection extends InternalInspection {
    @Nonnull
    @Override
    public PsiElementVisitor buildInternalVisitor(@Nonnull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitNewExpression(@Nonnull PsiNewExpression expression) {
                final ProblemDescriptor descriptor = checkNewExpression(expression, holder.getManager(), isOnTheFly);
                if (descriptor != null) {
                    holder.registerProblem(descriptor);
                }
            }
        };
    }

    @Nullable
    private static ProblemDescriptor checkNewExpression(PsiNewExpression expression, InspectionManager manager, boolean isOnTheFly) {
        final Project project = manager.getProject();
        final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        final PsiClass grayClass = facade.findClass(Gray.class.getName(), GlobalSearchScope.allScope(project));
        final PsiType type = expression.getType();
        if (type != null && grayClass != null) {
            final PsiExpressionList arguments = expression.getArgumentList();
            if (arguments != null) {
                final PsiExpression[] expressions = arguments.getExpressions();
                if (expressions.length == 3 && "java.awt.Color".equals(type.getCanonicalText())) {
                    if (!PsiResolveHelper.getInstance(project).isAccessible(grayClass, expression, grayClass)) {
                        return null;
                    }
                    final PsiExpression r = expressions[0];
                    final PsiExpression g = expressions[1];
                    final PsiExpression b = expressions[2];
                    if (r instanceof PsiLiteralExpression && g instanceof PsiLiteralExpression && b instanceof PsiLiteralExpression) {
                        final Object red = JavaConstantExpressionEvaluator.computeConstantExpression(r, false);
                        final Object green = JavaConstantExpressionEvaluator.computeConstantExpression(g, false);
                        final Object blue = JavaConstantExpressionEvaluator.computeConstantExpression(b, false);
                        if (NullUtils.notNull(red, green, blue)) {
                            try {
                                int rr = Integer.parseInt(red.toString());
                                int gg = Integer.parseInt(green.toString());
                                int bb = Integer.parseInt(blue.toString());
                                if (rr == gg && gg == bb && 0 <= rr && rr < 256) {
                                    return manager.createProblemDescriptor(
                                        expression,
                                        "Convert to Gray._" + rr,
                                        new ConvertToGrayQuickFix(rr),
                                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                        isOnTheFly
                                    );
                                }
                            }
                            catch (Exception ignore) {
                            }
                        }
                    }
                }
                else if (expressions.length == 1 && "com.intellij.ui.Gray".equals(type.getCanonicalText())) {
                    final PsiExpression e = expressions[0];
                    if (e instanceof PsiLiteralExpression) {
                        final Object literal = JavaConstantExpressionEvaluator.computeConstantExpression(e, false);
                        if (literal != null) {
                            try {
                                int num = Integer.parseInt(literal.toString());
                                if (0 <= num && num < 256) {
                                    return manager.createProblemDescriptor(
                                        expression,
                                        "Convert to Gray_" + num,
                                        new ConvertToGrayQuickFix(num),
                                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                        isOnTheFly
                                    );
                                }
                            }
                            catch (Exception ignore) {
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Nls
    @Nonnull
    @Override
    public String getDisplayName() {
        return "Using new Color(a,a,a)";
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "InspectionUsingGrayColors";
    }
}
