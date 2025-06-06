package org.intellij.grammar.impl.java;

import com.intellij.java.impl.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.java.language.psi.*;
import consulo.annotation.component.ServiceImpl;
import consulo.application.dumb.IndexNotReadyException;
import consulo.language.psi.NavigatablePsiElement;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.IncorrectOperationException;
import consulo.navigation.NavigationItem;
import consulo.util.lang.StringUtil;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.intellij.grammar.java.JavaHelper;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
@ServiceImpl
public class PsiJavaHelper extends JavaHelper {
    private final JavaPsiFacade myFacade;
    private final PsiElementFactory myElementFactory;

    @Inject
    public PsiJavaHelper(JavaPsiFacade facade, PsiElementFactory elementFactory) {
        myFacade = facade;
        myElementFactory = elementFactory;
    }

    @Override
    public PsiReferenceProvider getClassReferenceProvider() {
        JavaClassReferenceProvider provider = new JavaClassReferenceProvider();
        provider.setSoft(false);
        return provider;
    }

    @Override
    public NavigatablePsiElement findClass(String className) {
        PsiClass aClass = findClassSafe(className);
        return aClass != null ? aClass : super.findClass(className);
    }

    private PsiClass findClassSafe(String className) {
        if (className == null) {
            return null;
        }
        try {
            return myFacade.findClass(className, GlobalSearchScope.allScope(myFacade.getProject()));
        }
        catch (IndexNotReadyException e) {
            return null;
        }
    }

    @Override
    public NavigationItem findPackage(String packageName) {
        return myFacade.findPackage(packageName);
    }

    @Nonnull
    @Override
    public List<NavigatablePsiElement> findClassMethods(
        @Nullable String version,
        @Nullable String className,
        @Nonnull MethodType methodType,
        @Nullable String methodName,
        int paramCount,
        String... paramTypes
    ) {
        if (methodName == null) {
            return Collections.emptyList();
        }
        PsiClass aClass = findClassSafe(className);
        if (aClass == null) {
            return super.findClassMethods(version, className, methodType, methodName, paramCount, paramTypes);
        }
        List<NavigatablePsiElement> result = new ArrayList<>();
        PsiMethod[] methods = methodType == MethodType.CONSTRUCTOR ? aClass.getConstructors() : aClass.getMethods();
        for (PsiMethod method : methods) {
            if (!acceptsName(methodName, method.getName())) {
                continue;
            }
            if (!acceptsMethod(method, methodType == MethodType.STATIC)) {
                continue;
            }
            if (!acceptsMethod(myElementFactory, method, paramCount, paramTypes)) {
                continue;
            }
            result.add(method);
        }
        return result;
    }

    @Nullable
    @Override
    public String getSuperClassName(@Nullable String className) {
        PsiClass aClass = findClassSafe(className);
        PsiClass superClass = aClass != null ? aClass.getSuperClass() : null;
        return superClass != null ? superClass.getQualifiedName() : super.getSuperClassName(className);
    }

    private static boolean acceptsMethod(PsiElementFactory elementFactory, PsiMethod method, int paramCount, String... paramTypes) {
        PsiParameterList parameterList = method.getParameterList();
        if (paramCount >= 0 && paramCount != parameterList.getParametersCount()) {
            return false;
        }
        if (paramTypes.length == 0) {
            return true;
        }
        if (parameterList.getParametersCount() < paramTypes.length) {
            return false;
        }
        PsiParameter[] psiParameters = parameterList.getParameters();
        for (int i = 0; i < paramTypes.length; i++) {
            String paramType = paramTypes[i];
            PsiParameter parameter = psiParameters[i];
            PsiType psiType = parameter.getType();
            if (acceptsName(paramType, psiType.getCanonicalText())) {
                continue;
            }
            try {
                if (psiType.isAssignableFrom(elementFactory.createTypeFromText(paramType, parameter))) {
                    continue;
                }
            }
            catch (IncorrectOperationException ignored) {
            }
            return false;
        }
        return true;
    }

    private static boolean acceptsMethod(PsiMethod method, boolean staticMethods) {
        return staticMethods == method.isStatic() && !method.isAbstract()
            && (method.isPublic() || !(method.isProtected() || method.isPrivate()));
    }

    @Nonnull
    @Override
    public List<String> getMethodTypes(String version, NavigatablePsiElement method) {
        if (!(method instanceof PsiMethod psiMethod)) {
            return super.getMethodTypes(version, method);
        }
        PsiType returnType = psiMethod.getReturnType();
        List<String> strings = new ArrayList<>();
        strings.add(returnType == null ? "" : returnType.getCanonicalText());
        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
            PsiType type = parameter.getType();
            boolean generic = type instanceof PsiClassType classType && classType.resolve() instanceof PsiTypeParameter;
            strings.add((generic ? "<" : "") + type.getCanonicalText(false) + (generic ? ">" : ""));
            strings.add(parameter.getName());
        }
        return strings;
    }

    @Nonnull
    @Override
    public String getDeclaringClass(@Nullable NavigatablePsiElement method) {
        if (!(method instanceof PsiMethod psiMethod)) {
            return super.getDeclaringClass(method);
        }
        PsiClass aClass = psiMethod.getContainingClass();
        return aClass == null ? "" : StringUtil.notNullize(aClass.getQualifiedName());
    }

    @Nonnull
    @Override
    public List<String> getAnnotations(NavigatablePsiElement element) {
        if (!(element instanceof PsiModifierListOwner modifierListOwner)) {
            return super.getAnnotations(element);
        }
        PsiModifierList modifierList = modifierListOwner.getModifierList();
        if (modifierList == null) {
            return List.of();
        }
        List<String> strings = new ArrayList<>();
        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            if (annotation.getParameterList().getAttributes().length > 0) {
                continue;
            }
            strings.add(annotation.getQualifiedName());
        }
        return strings;
    }
}
