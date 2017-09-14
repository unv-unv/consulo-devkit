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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ThrowableRunnable;
import consulo.annotations.RequiredReadAction;
import consulo.devkit.inspections.requiredXAction.AcceptableMethodCallCheck;
import consulo.devkit.inspections.requiredXAction.CallStateType;

/**
 * @author VISTALL
 * @since 01-Oct-16
 */
public abstract class StateResolver
{
	@Nullable
	public abstract Boolean resolveState(CallStateType actionType, PsiExpression expression);

	protected static Map<String, Class[]> ourInterfaces = new HashMap<String, Class[]>()
	{
		{
			put("compute", new Class[]{
					Computable.class,
					ThrowableComputable.class
			});
			put("run", new Class[]{
					Runnable.class,
					ThrowableRunnable.class
			});
		}
	};

	protected static boolean resolveByMaybeParameterListOrVariable(PsiElement maybeParameterListOrVariable, CallStateType actionType)
	{
		// Runnable run = new Runnable() {};
		// ApplicationManager.getApplication().runReadAction(run);
		if(maybeParameterListOrVariable instanceof PsiVariable)
		{
			CommonProcessors.CollectProcessor<PsiReference> processor = new CommonProcessors.CollectProcessor<>();
			ReferencesSearch.search(maybeParameterListOrVariable).forEach(processor);

			Collection<PsiReference> results = processor.getResults();
			if(results.isEmpty())
			{
				return false;
			}

			boolean weFoundRunAction = false;
			for(PsiReference result : results)
			{
				if(result instanceof PsiReferenceExpression)
				{
					PsiElement maybeExpressionList = ((PsiReferenceExpression) result).getParent();
					if(maybeExpressionList instanceof PsiExpressionList)
					{
						if(acceptActionTypeFromCall((PsiExpressionList) maybeExpressionList, actionType))
						{
							weFoundRunAction = true;
							break;
						}
					}
				}
			}

			if(weFoundRunAction)
			{
				return true;
			}
		}
		// ApplicationManager.getApplication().runReadAction(new Runnable() {});
		else if(maybeParameterListOrVariable instanceof PsiExpressionList)
		{
			if(acceptActionTypeFromCall((PsiExpressionList) maybeParameterListOrVariable, actionType))
			{
				return true;
			}
		}
		return false;
	}

	protected static boolean acceptActionTypeFromCall(@NotNull PsiExpressionList expressionList, @NotNull CallStateType actionType)
	{
		for(CallStateType type : CallStateType.values())
		{
			if(actionType.isAcceptableActionType(type))
			{
				PsiElement parent = expressionList.getParent();

				for(AcceptableMethodCallCheck acceptableMethodCallCheck : type.getAcceptableMethodCallChecks())
				{
					if(acceptableMethodCallCheck.accept(parent))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	@RequiredReadAction
	protected static boolean isAllowedFunctionCall(@NotNull PsiFunctionalExpression functionalExpression, @NotNull CallStateType actionType)
	{
		PsiElement superParent = functionalExpression.getParent();
		if(!(superParent instanceof PsiExpressionList))
		{
			return false;
		}

		if(!(superParent.getParent() instanceof PsiMethodCallExpression))
		{
			return false;
		}

		PsiExpression[] expressions = ((PsiExpressionList) superParent).getExpressions();
		int i = ArrayUtil.indexOf(expressions, functionalExpression);
		if(i == -1)
		{
			return false;
		}

		PsiElement target = ((PsiMethodCallExpression) superParent.getParent()).getMethodExpression().resolve();
		if(!(target instanceof PsiParameterListOwner))
		{
			return false;
		}

		PsiParameter parameter = ((PsiParameterListOwner) target).getParameterList().getParameters()[i];

		for(CallStateType type : CallStateType.values())
		{
			if(actionType.isAcceptableActionType(type))
			{
				if(AnnotationUtil.isAnnotated(parameter, type.getActionClass(), false))
				{
					return true;
				}
			}
		}
		return false;
	}
}