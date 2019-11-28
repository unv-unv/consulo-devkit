/*
 * Copyright 2013-2017 consulo.io
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

package consulo.devkit.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import consulo.devkit.util.PluginModuleUtil;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 28-Jan-17
 */
public abstract class InternalAction extends AnAction
{
	protected InternalAction()
	{
	}

	protected InternalAction(Image icon)
	{
		super(icon);
	}

	protected InternalAction(@Nullable String text)
	{
		super(text);
	}

	protected InternalAction(@Nullable String text, @Nullable String description, @Nullable Image icon)
	{
		super(text, description, icon);
	}

	@RequiredUIAccess
	@Override
	public void update(@Nonnull AnActionEvent e)
	{
		Project project = e.getProject();
		Module module = e.getData(CommonDataKeys.MODULE);
		e.getPresentation().setEnabledAndVisible(project != null && PluginModuleUtil.isConsuloOrPluginProject(project, module));
	}
}
