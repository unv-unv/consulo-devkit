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
package consulo.devkit.module.library;

import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.roots.libraries.DummyLibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryKind;
import com.intellij.openapi.roots.libraries.LibraryPresentationProvider;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 14:59/13.06.13
 */
public class PluginLibraryPresentationProvider extends LibraryPresentationProvider<DummyLibraryProperties> {
  public static final LibraryKind KIND = LibraryKind.create("consulo-plugin");

  public PluginLibraryPresentationProvider() {
    super(KIND);
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return AllIcons.Icon16;
  }

  @Nullable
  @Override
  public DummyLibraryProperties detect(@NotNull List<VirtualFile> classesRoots) {
    if (classesRoots.isEmpty()) {
      return null;
    }

    for (VirtualFile virtualFile : classesRoots) {
      final VirtualFile fileByRelativePath = virtualFile.findFileByRelativePath("META-INF/plugin.xml");
      if (fileByRelativePath != null) {
        return DummyLibraryProperties.INSTANCE;
      }
    }
    return null;
  }
}