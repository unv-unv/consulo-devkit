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

package consulo.devkit.util;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.DeprecationInfo;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.devkit.module.extension.PluginModuleExtension;
import consulo.java.impl.roots.SpecialDirUtil;
import consulo.java.language.module.extension.JavaModuleExtension;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.extension.ModuleExtension;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.util.xml.DomElement;
import jakarta.annotation.Nullable;
import org.jetbrains.idea.devkit.build.PluginBuildUtil;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2013-05-23
 */
public class PluginModuleUtil {
    public static final String PLUGIN_XML = "plugin.xml";

    public static boolean isConsuloV3(@Nonnull DomElement element) {
        Module module = element.getModule();
        return module != null && JavaPsiFacade.getInstance(module.getProject())
            .findClass("consulo.application.Application", GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)) != null;
    }

    @RequiredReadAction
    public static Module[] getAllPluginModules(final Project project) {
        List<Module> modules = new ArrayList<>();
        Module[] allModules = ModuleManager.getInstance(project).getModules();
        for (Module module : allModules) {
            if (ModuleUtilCore.getExtension(module, PluginModuleExtension.class) != null) {
                modules.add(module);
            }
        }
        return modules.toArray(new Module[modules.size()]);
    }

    @Nullable
    @RequiredReadAction
    public static XmlFile getPluginXml(Module module) {
        if (module == null) {
            return null;
        }
        if (ModuleUtilCore.getExtension(module, PluginModuleExtension.class) == null) {
            return null;
        }

        PsiManager psiManager = PsiManager.getInstance(module.getProject());

        List<VirtualFile> virtualFiles = SpecialDirUtil.collectSpecialDirs(module, SpecialDirUtil.META_INF);
        for (VirtualFile virtualFile : virtualFiles) {
            VirtualFile child = virtualFile.findChild(PLUGIN_XML);
            if (child == null) {
                continue;
            }

            PsiFile file = psiManager.findFile(child);
            if (file instanceof XmlFile xmlFile) {
                return xmlFile;
            }
        }
        return null;
    }

    @RequiredReadAction
    public static boolean isPluginModuleOrDependency(@Nullable Module module) {
        //noinspection SimplifiableIfStatement
        if (module == null) {
            return false;
        }

        return ModuleUtilCore.getExtension(module, PluginModuleExtension.class) != null
            || getCandidateModules(module).size() > 0;
    }

    @RequiredReadAction
    @Nonnull
    public static List<Module> getCandidateModules(Module module) {
        Module[] modules = ModuleManager.getInstance(module.getProject()).getModules();
        List<Module> candidates = new ArrayList<>(modules.length);
        Set<Module> deps = new HashSet<>(modules.length);
        for (Module m : modules) {
            if (ModuleUtilCore.getExtension(module, PluginModuleExtension.class) != null) {
                deps.clear();
                PluginBuildUtil.getDependencies(m, deps);

                if (deps.contains(module) && getPluginXml(m) != null) {
                    candidates.add(m);
                }
            }
        }
        return candidates;
    }

    public static boolean isConsuloProject(Project project) {
        return project.getName().equals("consulo");
    }

    @RequiredReadAction
    @Deprecated
    @DeprecationInfo("Make it more clear. See IconLibraryChecker")
    public static boolean isConsuloOrPluginProject(@Nonnull PsiElement element) {
        return LanguageCachedValueUtil.getCachedValue(
            element,
            () -> {
                Module module = element.getModule();
                return CachedValueProvider.Result.create(
                    isConsuloOrPluginProject(element.getProject(), module),
                    PsiModificationTracker.MODIFICATION_COUNT
                );
            }
        );
    }

    @RequiredReadAction
    public static boolean isConsuloOrPluginProject(@Nonnull Project project, @Nullable Module module) {
        if (PluginModuleUtil.isConsuloProject(project)) {
            return true;
        }

        if (module != null) {
            PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(
                ModuleExtension.class.getName(),
                GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false)
            );
            if (psiClass != null) {
                return true;
            }

            if (PluginModuleUtil.isPluginModuleOrDependency(module)) {
                return true;
            }
        }

        for (Module temp : ModuleManager.getInstance(project).getModules()) {
            if (PluginModuleUtil.isPluginModuleOrDependency(temp)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @RequiredReadAction
    public static PsiClass searchClassInFileUseScope(@Nonnull PsiFile file, @Nonnull String qName) {
        JavaModuleExtension extension = ModuleUtilCore.getExtension(file, JavaModuleExtension.class);
        if (extension == null) {
            return null;
        }

        return JavaPsiFacade.getInstance(file.getProject()).findClass(qName, file.getResolveScope());
    }
}
