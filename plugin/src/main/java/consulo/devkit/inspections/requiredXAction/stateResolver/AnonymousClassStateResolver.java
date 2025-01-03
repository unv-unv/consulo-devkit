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

package consulo.devkit.inspections.requiredXAction.stateResolver;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.InheritanceUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.devkit.inspections.requiredXAction.CallStateType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2016-10-01
 */
public class AnonymousClassStateResolver extends StateResolver {
    public static final StateResolver INSTANCE = new AnonymousClassStateResolver();

    @RequiredReadAction
    @Override
    @Nullable
    public Boolean resolveState(CallStateType actionType, PsiExpression expression) {
        PsiMethod callMethod = PsiTreeUtil.getParentOfType(expression, PsiMethod.class);
        if (callMethod == null) {
            return null;
        }

        // method annotated by annotation
        CallStateType callMethodActionType = CallStateType.findActionType(callMethod);
        if (actionType.isAcceptableActionType(callMethodActionType, expression)) {
            return true;
        }

        if (callMethod.getParameterList().getParametersCount() == 0) {
            Class[] qualifiedNames = ourInterfaces.get(callMethod.getName());
            if (qualifiedNames == null) {
                return false;
            }

            PsiClass containingClass = callMethod.getContainingClass();
            if (containingClass == null) {
                return false;
            }

            boolean inherit = false;
            for (Class clazz : qualifiedNames) {
                if (InheritanceUtil.isInheritor(containingClass, clazz.getName())) {
                    inherit = true;
                    break;
                }
            }

            if (inherit) {
                // non anonym class - cant handle
                if (!(containingClass instanceof PsiAnonymousClass)) {
                    return false;
                }

                PsiElement parent = containingClass.getParent();
                if (parent instanceof PsiNewExpression) {
                    PsiElement maybeParameterListOrVariable = parent.getParent();
                    return resolveByMaybeParameterListOrVariable(maybeParameterListOrVariable, actionType);
                }
            }
        }

        return false;
    }
}
