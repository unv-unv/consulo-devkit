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

import consulo.devkit.util.PluginModuleUtil;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DefaultActionGroup;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 28-Jan-17
 */
public class InternalGroup extends DefaultActionGroup {
    @RequiredUIAccess
    @Override
    public void update(@Nonnull AnActionEvent e) {
        Project project = e.getData(Project.KEY);
        e.getPresentation().setVisible(project != null && PluginModuleUtil.isConsuloOrPluginProject(project, null));
    }
}
