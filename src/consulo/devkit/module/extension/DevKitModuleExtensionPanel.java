/*
 * Copyright 2013-2016 must-be.org
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

package consulo.devkit.module.extension;

import javax.swing.JPanel;

import com.intellij.openapi.ui.VerticalFlowLayout;
import consulo.annotations.RequiredDispatchThread;
import consulo.extension.ui.ModuleExtensionSdkBoxBuilder;

/**
 * @author VISTALL
 * @since 22.03.2015
 */
public class DevKitModuleExtensionPanel extends JPanel
{
	@RequiredDispatchThread
	public DevKitModuleExtensionPanel(PluginMutableModuleExtension mutableModuleExtension, Runnable updateOnCheck)
	{
		super(new VerticalFlowLayout(true, false));

		add(ModuleExtensionSdkBoxBuilder.createAndDefine(mutableModuleExtension, updateOnCheck).build());
	}
}
