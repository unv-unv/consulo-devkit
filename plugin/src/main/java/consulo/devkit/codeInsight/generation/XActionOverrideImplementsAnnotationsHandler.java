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

package consulo.devkit.codeInsight.generation;

import com.intellij.java.impl.codeInsight.generation.OverrideImplementsAnnotationsHandler;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.collection.ArrayUtil;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2015-06-10
 */
@ExtensionImpl
public class XActionOverrideImplementsAnnotationsHandler implements OverrideImplementsAnnotationsHandler {
    private static final String[] ourAnnotations = new String[]{
        RequiredReadAction.class.getName(),
        RequiredWriteAction.class.getName(),
        RequiredUIAccess.class.getName(),
        RequiredUIAccess.class.getName()
    };

    @Override
    public String[] getAnnotations(Project project) {
        return ourAnnotations;
    }

    @Nonnull
    @Override
    public String[] annotationsToRemove(Project project, @Nonnull String s) {
        if (ArrayUtil.contains(s, ourAnnotations)) {
            return ourAnnotations;
        }
        return ArrayUtil.EMPTY_STRING_ARRAY;
    }
}
