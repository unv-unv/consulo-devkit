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

package consulo.devkit.run;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.debugger.DefaultDebugEnvironment;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.projectRoots.Sdk;

/**
 * @author VISTALL
 * @since 23-May-17
 */
class ConsuloDebugEnvironment extends DefaultDebugEnvironment
{
	private final ConsuloSandboxRunState myState;

	public ConsuloDebugEnvironment(@NotNull ExecutionEnvironment environment, @NotNull ConsuloSandboxRunState state, RemoteConnection remoteConnection, boolean pollConnection)
	{
		super(environment, state, remoteConnection, pollConnection);
		myState = state;
	}

	@Nullable
	@Override
	public Sdk getRunJre()
	{
		return myState.getJavaParameters().getJdk();
	}
}