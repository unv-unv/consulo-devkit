/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package org.jetbrains.idea.devkit.util;

import javax.annotation.Nullable;

import org.jetbrains.idea.devkit.DevKitBundle;
import org.jetbrains.idea.devkit.dom.IdeaPlugin;
import consulo.devkit.module.extension.PluginModuleExtension;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import consulo.devkit.util.PluginModuleUtil;

/**
 * @author swr
 */
public class DescriptorUtil
{

	public static void processComponents(XmlTag root, ComponentType.Processor processor)
	{
		final ComponentType[] types = ComponentType.values();
		for(ComponentType type : types)
		{
			type.process(root, processor);
		}
	}

	public static void processActions(XmlTag root, ActionType.Processor processor)
	{
		final ActionType[] types = ActionType.values();
		for(ActionType type : types)
		{
			type.process(root, processor);
		}
	}

	public interface Patcher
	{
		void patchPluginXml(XmlFile pluginXml, PsiClass klass) throws IncorrectOperationException;
	}

	public static void patchPluginXml(Patcher patcher, PsiClass klass, XmlFile... pluginXmls) throws IncorrectOperationException
	{
		final VirtualFile[] files = new VirtualFile[pluginXmls.length];
		int i = 0;
		for(XmlFile pluginXml : pluginXmls)
		{
			files[i++] = pluginXml.getVirtualFile();
		}

		final ReadonlyStatusHandler readonlyStatusHandler = ReadonlyStatusHandler.getInstance(klass.getProject());
		final ReadonlyStatusHandler.OperationStatus status = readonlyStatusHandler.ensureFilesWritable(files);
		if(status.hasReadonlyFiles())
		{
			throw new IncorrectOperationException(DevKitBundle.message("error.plugin.xml.readonly"));
		}

		for(XmlFile pluginXml : pluginXmls)
		{
			patcher.patchPluginXml(pluginXml, klass);
		}
	}

	@Nullable
	public static DomFileElement<IdeaPlugin> getIdeaPlugin(XmlFile file)
	{
		return DomManager.getDomManager(file.getProject()).getFileElement(file, IdeaPlugin.class);
	}

	public static boolean isPluginXml(PsiFile file)
	{
		if(!(file instanceof XmlFile))
		{
			return false;
		}
		return getIdeaPlugin((XmlFile) file) != null;
	}

	@Nullable
	public static String getPluginId(Module plugin)
	{
		assert ModuleUtil.getExtension(plugin, PluginModuleExtension.class) != null;

		final XmlFile pluginXml = PluginModuleUtil.getPluginXml(plugin);
		if(pluginXml != null)
		{
			final XmlTag rootTag = pluginXml.getDocument().getRootTag();
			if(rootTag != null)
			{
				final XmlTag idTag = rootTag.findFirstSubTag("id");
				if(idTag != null)
				{
					return idTag.getValue().getTrimmedText();
				}

				final XmlTag nameTag = rootTag.findFirstSubTag("name");
				if(nameTag != null)
				{
					return nameTag.getValue().getTrimmedText();
				}
			}
		}
		return null;
	}
}
