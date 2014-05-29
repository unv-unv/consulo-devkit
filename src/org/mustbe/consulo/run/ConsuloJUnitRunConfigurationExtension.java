/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.run;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Location;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;

/**
 * @author VISTALL
 * @since 28.05.14
 */
public class ConsuloJUnitRunConfigurationExtension extends RunConfigurationExtension
{

	@Override
	public void updateJavaParameters(RunConfigurationBase t, JavaParameters javaParameters, RunnerSettings runnerSettings) throws ExecutionException
	{

	}

	@NotNull
	@Override
	protected String getSerializationId()
	{
		return "consulo-junit";
	}

	@Override
	protected void readExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) throws InvalidDataException
	{

	}

	@Override
	protected void writeExternal(@NotNull RunConfigurationBase runConfiguration, @NotNull Element element) throws WriteExternalException
	{

	}

	@Nullable
	@Override
	protected String getEditorTitle()
	{
		return null;
	}

	@Override
	protected boolean isApplicableFor(@NotNull RunConfigurationBase configuration)
	{
		return configuration instanceof JUnitConfiguration;
	}

	@Override
	protected void validateConfiguration(@NotNull RunConfigurationBase configuration, boolean isExecution) throws Exception
	{

	}

	@Override
	protected void extendCreatedConfiguration(@NotNull RunConfigurationBase configuration, @NotNull Location location)
	{

	}

	@Override
	protected void attachToProcess(@NotNull RunConfigurationBase configuration, @NotNull ProcessHandler handler, RunnerSettings runnerSettings)
	{

	}

	@Nullable
	@Override
	protected SettingsEditor createEditor(@NotNull RunConfigurationBase configuration)
	{
		return null;
	}
}
