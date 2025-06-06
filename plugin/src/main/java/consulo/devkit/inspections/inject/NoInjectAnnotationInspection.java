package consulo.devkit.inspections.inject;

import com.intellij.java.language.codeInsight.AnnotationUtil;
import com.intellij.java.language.psi.JavaElementVisitor;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.devkit.inspections.util.service.ServiceInfo;
import consulo.devkit.inspections.util.service.ServiceLocator;
import consulo.devkit.inspections.valhalla.ValhallaClasses;
import consulo.devkit.localize.DevKitLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElementVisitor;
import jakarta.annotation.Nonnull;
import org.jetbrains.idea.devkit.inspections.internal.InternalInspection;

import java.util.List;

/**
 * @author VISTALL
 * @since 2018-09-02
 */
@ExtensionImpl
public class NoInjectAnnotationInspection extends InternalInspection {
    public static final List<String> INJECT_ANNOTATIONS = List.of("jakarta.inject.Inject");

    private static class Visitor extends JavaElementVisitor {
        private final ProblemsHolder myHolder;

        public Visitor(ProblemsHolder holder) {
            myHolder = holder;
        }

        @Override
        @RequiredReadAction
        public void visitClass(@Nonnull PsiClass aClass) {
            if (!isInjectionTarget(aClass)) {
                return;
            }

            PsiMethod[] constructors = aClass.getConstructors();
            if (constructors.length == 0) {
                // default constructor
                if (aClass.isPublic()) {
                    return;
                }
            }
            else {
                PsiMethod defaultConstructor = null;
                for (PsiMethod constructor : constructors) {
                    if (constructor.isPublic() && constructor.getParameterList().getParametersCount() == 0) {
                        defaultConstructor = constructor;
                    }

                    if (AnnotationUtil.isAnnotated(constructor, INJECT_ANNOTATIONS, 0)) {
                        return;
                    }
                }

                if (constructors.length == 1 && defaultConstructor != null) {
                    return;
                }
            }

            myHolder.newProblem(DevKitLocalize.noInjectAnnotationInspectionMessage())
                .range(aClass.getNameIdentifier())
                .create();
        }
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return DevKitLocalize.noInjectAnnotationInspectionDisplayName().get();
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Override
    public PsiElementVisitor buildInternalVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly) {
        return new Visitor(holder);
    }

    @RequiredReadAction
    private static boolean isInjectionTarget(PsiClass psiClass) {
        ServiceInfo serviceInfo = ServiceLocator.findImplementationService(psiClass);
        if (serviceInfo != null) {
            // old XML service
            return true;
        }

        for (String annotation : ValhallaClasses.IMPL) {
            if (AnnotationUtil.isAnnotated(psiClass, annotation, 0)) {
                return true;
            }
        }
        return false;
    }
}
