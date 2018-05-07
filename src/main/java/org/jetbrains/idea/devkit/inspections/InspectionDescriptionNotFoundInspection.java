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

package org.jetbrains.idea.devkit.inspections;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.devkit.inspections.quickfix.CreateHtmlDescriptionFix;
import org.jetbrains.idea.devkit.util.PsiUtil;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import consulo.psi.PsiPackage;
import consulo.roots.ContentFolderScopes;

/**
 * @author Konstantin Bulenkov
 */
public class InspectionDescriptionNotFoundInspection extends DevKitInspectionBase {
  @NonNls static final String INSPECTION_PROFILE_ENTRY = InspectionProfileEntry.class.getName();
  @NonNls private static final String INSPECTION_DESCRIPTIONS = "inspectionDescriptions";

  @Override
  public ProblemDescriptor[] checkClass(@Nonnull PsiClass aClass, @Nonnull InspectionManager manager, boolean isOnTheFly) {
    final Project project = aClass.getProject();
    final PsiIdentifier nameIdentifier = aClass.getNameIdentifier();
    final Module module = ModuleUtil.findModuleForPsiElement(aClass);

    if (nameIdentifier == null || module == null || !PsiUtil.isInstantiable(aClass)) return null;

    final PsiClass base = JavaPsiFacade.getInstance(project).findClass(INSPECTION_PROFILE_ENTRY, GlobalSearchScope.allScope(project));

    if (base == null || !aClass.isInheritor(base, true) || isPathMethodsAreOverridden(aClass)) return null;

    PsiMethod method = findNearestMethod("getShortName", aClass);
    if (method != null && method.getContainingClass().getQualifiedName().equals(INSPECTION_PROFILE_ENTRY)) {
      method = null;
    }
    final String filename =
      method == null ? InspectionProfileEntry.getShortName(aClass.getName()) : PsiUtil.getReturnedLiteral(method, aClass);
    if (filename == null) return null;


    for (PsiDirectory description : getInspectionDescriptionsDirs(module)) {
      final PsiFile file = description.findFile(filename + ".html");
      if (file == null) continue;
      final VirtualFile vf = file.getVirtualFile();
      if (vf == null) continue;
      if (vf.getNameWithoutExtension().equals(filename)) {
        return null;
      }
    }


    final PsiElement problem = getProblemElement(aClass, method);
    final ProblemDescriptor problemDescriptor = manager
      .createProblemDescriptor(problem == null ? nameIdentifier : problem, "Inspection does not have a description", isOnTheFly,
                               new LocalQuickFix[]{new CreateHtmlDescriptionFix(filename, module, false)},
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    return new ProblemDescriptor[]{problemDescriptor};
  }

  @Nullable
  private static PsiElement getProblemElement(PsiClass aClass, @Nullable PsiMethod method) {
    if (method != null && method.getContainingClass() == aClass) {
      return PsiUtil.getReturnedExpression(method);
    }
    else {
      return aClass.getNameIdentifier();
    }
  }

  private static boolean isPathMethodsAreOverridden(PsiClass aClass) {
    return !(isLastMethodDefinitionIn("getStaticDescription", INSPECTION_PROFILE_ENTRY, aClass) &&
             isLastMethodDefinitionIn("getDescriptionUrl", INSPECTION_PROFILE_ENTRY, aClass) &&
             isLastMethodDefinitionIn("getDescriptionContextClass", INSPECTION_PROFILE_ENTRY, aClass) &&
             isLastMethodDefinitionIn("getDescriptionFileName", INSPECTION_PROFILE_ENTRY, aClass));
  }

  private static boolean isLastMethodDefinitionIn(@Nonnull String methodName, @Nonnull String classFQN, PsiClass cls) {
    if (cls == null) return false;
    for (PsiMethod method : cls.getMethods()) {
      if (method.getName().equals(methodName)) {
        final PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) return false;
        return classFQN.equals(containingClass.getQualifiedName());
      }
    }
    return isLastMethodDefinitionIn(methodName, classFQN, cls.getSuperClass());
  }

  public static List<VirtualFile> getPotentialRoots(Module module) {
    final PsiDirectory[] dirs = getInspectionDescriptionsDirs(module);
    final List<VirtualFile> result = new ArrayList<VirtualFile>();
    if (dirs.length != 0) {
      for (PsiDirectory dir : dirs) {
        final PsiDirectory parent = dir.getParentDirectory();
        if (parent != null) result.add(parent.getVirtualFile());
      }
    }
    else {
      ContainerUtil.addAll(result, ModuleRootManager.getInstance(module).getContentFolderFiles(ContentFolderScopes.productionAndTest()));
    }
    return result;
  }

  public static PsiDirectory[] getInspectionDescriptionsDirs(Module module) {
    final PsiPackage aPackage = JavaPsiFacade.getInstance(module.getProject()).findPackage(INSPECTION_DESCRIPTIONS);
    if (aPackage != null) {
      return aPackage.getDirectories(GlobalSearchScope.moduleWithDependenciesScope(module));
    }
    else {
      return PsiDirectory.EMPTY_ARRAY;
    }
  }

  @Nullable
  private static PsiMethod findNearestMethod(String name, @Nullable PsiClass cls) {
    if (cls == null) return null;
    for (PsiMethod method : cls.getMethods()) {
      if (method.getParameterList().getParametersCount() == 0 && method.getName().equals(name)) {
        return method.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT) ? null : method;
      }
    }
    return findNearestMethod(name, cls.getSuperClass());
  }

  @Nls
  @Nonnull
  public String getDisplayName() {
    return "Inspection Description Checker";
  }

  @Nonnull
  public String getShortName() {
    return "InspectionDescriptionNotFoundInspection";
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }
}
