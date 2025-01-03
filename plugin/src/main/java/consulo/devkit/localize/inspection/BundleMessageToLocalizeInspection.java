package consulo.devkit.localize.inspection;

import com.intellij.java.language.impl.psi.impl.JavaConstantExpressionEvaluator;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.codeStyle.JavaCodeStyleManager;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.WriteAction;
import consulo.devkit.localize.DevKitLocalize;
import consulo.devkit.localize.LocalizeUtil;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import org.jetbrains.idea.devkit.inspections.internal.InternalInspection;

/**
 * @author UNV
 * @since 2024-06-07
 */
@ExtensionImpl
public class BundleMessageToLocalizeInspection extends InternalInspection {
    protected static final String
        BUNDLE_SUFFIX = "Bundle",
        LOCALIZE_SUFFIX = "Localize",
        MESSAGE_METHOD_NAME = "message";

    @Nonnull
    @Override
    public String getDisplayName() {
        return DevKitLocalize.inspectionsReplaceWithXxxlocalizeTitle().get();
    }

    @Nonnull
    @Override
    public PsiElementVisitor buildInternalVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly) {
        return new BundleCallVisitor(holder);
    }

    public static String getString(PsiExpression expr) {
        return (String)getObject(expr);
    }

    private static Object getObject(PsiExpression expr) {
        return JavaConstantExpressionEvaluator.computeConstantExpression(expr, true);
    }

    private static class BundleCallVisitor extends JavaElementVisitor {
        private final ProblemsHolder myHolder;

        private BundleCallVisitor(ProblemsHolder holder) {
            myHolder = holder;
        }

        @Override
        @RequiredReadAction
        public void visitMethodCallExpression(@Nonnull PsiMethodCallExpression expression) {
            new TransformToLocalizeInspector(expression).registerProblem();
        }

        private class TransformToLocalizeInspector extends LocalizeClassExistsChecker {
            protected String myReplacementCodeBlock;

            protected TransformToLocalizeInspector(@Nonnull PsiMethodCallExpression expression) {
                super(expression);
            }

            @RequiredReadAction
            public void registerProblem() {
                if (!isApplicable()) {
                    return;
                }

                initReplacementCodeBlock();

                LocalizeValue inspectionName = DevKitLocalize.inspectionsReplaceWithXxxlocalize(myLocalizeClassName, myLocalizeMethodName);

                myHolder.newProblem(inspectionName)
                    .range(myExpression)
                    .withFix(new TransformToLocalizeFix(myExpression, inspectionName, myReplacementCodeBlock))
                    .create();
            }

            @RequiredReadAction
            private void initReplacementCodeBlock() {
                StringBuilder codeBlock = new StringBuilder()
                    .append(myLocalizeClassQualifiedName)
                    .append('.').append(myLocalizeMethodName).append('(');

                for (int i = 1, n = myArgExpressions.length; i < n; i++) {
                    PsiExpression argExpression = myArgExpressions[i];
                    if (i > 1) {
                        codeBlock.append(", ");
                    }
                    codeBlock.append(argExpression.getText());
                }

                codeBlock.append(").get()");

                myReplacementCodeBlock = codeBlock.toString();
            }
        }
    }

    private static class TransformToLocalizeFix extends LocalQuickFixOnPsiElement {
        protected final LocalizeValue myInspectionName;
        protected final String myReplacementCodeBlock;

        protected TransformToLocalizeFix(@Nonnull PsiElement element, LocalizeValue inspectionName, String replacementCodeBlock) {
            super(element);
            myInspectionName = inspectionName;
            myReplacementCodeBlock = replacementCodeBlock;
        }

        @Nonnull
        @Override
        public String getText() {
            return myInspectionName.get();
        }

        @Override
        public void invoke(
            @Nonnull Project project,
            @Nonnull PsiFile psiFile,
            @Nonnull PsiElement expression,
            @Nonnull PsiElement endElement
        ) {
            PsiExpression newExpression = JavaPsiFacade.getElementFactory(project)
                .createExpressionFromText(myReplacementCodeBlock, expression);

            WriteAction.run(() -> {
                PsiElement newElement = expression.replace(newExpression);

                JavaCodeStyleManager.getInstance(project).shortenClassReferences(newElement);
            });
        }

        @Nonnull
        @Override
        public String getFamilyName() {
            return DevKitLocalize.inspectionsGroupName().get();
        }
    }

    private abstract static class MethodCallExpressionChecker {
        @Nonnull
        protected final PsiMethodCallExpression myExpression;
        @Nonnull
        protected final PsiReferenceExpression myMethodExpression;
        @Nonnull
        protected Project myProject;

        protected MethodCallExpressionChecker(@Nonnull PsiMethodCallExpression expression) {
            myExpression = expression;
            myMethodExpression = expression.getMethodExpression();
            myProject = expression.getProject();
        }

        abstract boolean isApplicable();
    }

    private static class CallsBundleMessageChecker extends MethodCallExpressionChecker {
        protected CallsBundleMessageChecker(@Nonnull PsiMethodCallExpression expression) {
            super(expression);
        }

        @Override
        @RequiredReadAction
        boolean isApplicable() {
            if (!MESSAGE_METHOD_NAME.equals(myMethodExpression.getReferenceName())) {
                return false;
            }

            PsiElement qualifier = myMethodExpression.getQualifier();
            if (qualifier == null || !qualifier.getText().endsWith(BUNDLE_SUFFIX)) {
                return false;
            }

            return !myExpression.getArgumentList().isEmpty();
        }
    }

    private static class BundleClassChecker extends CallsBundleMessageChecker {
        protected PsiElement myMethod;
        protected PsiClass myClass;

        protected BundleClassChecker(@Nonnull PsiMethodCallExpression expression) {
            super(expression);
        }

        @Override
        @RequiredReadAction
        boolean isApplicable() {
            if (!super.isApplicable()) {
                return false;
            }

            myMethod = myMethodExpression.resolve();
            PsiElement parent = (myMethod == null) ? null : myMethod.getParent();
            myClass = (parent instanceof PsiClass psiClass) ? psiClass : null;

            return myClass != null;
        }
    }

    private static class LocalizeClassExistsChecker extends BundleClassChecker {
        protected String
            myClassName,
            myLocalizeClassName,
            myLocalizeClassQualifiedName,
            myLocalizeMethodName;

        protected PsiExpression[] myArgExpressions;

        protected LocalizeClassExistsChecker(@Nonnull PsiMethodCallExpression expression) {
            super(expression);
        }

        @Override
        @RequiredReadAction
        boolean isApplicable() {
            if (!super.isApplicable()) {
                return false;
            }

            myClassName = myClass.getName();

            if (!initLocalizeClass()) {
                return false;
            }

            myArgExpressions = myExpression.getArgumentList().getExpressions();

            String key = getString(myArgExpressions[0]);
            if (key == null) {
                return false;
            }

            myLocalizeMethodName = LocalizeUtil.formatMethodName(myProject, key);

            return true;
        }

        @RequiredReadAction
        private boolean initLocalizeClass() {
            PsiClass localizeClass = LocalizeClassResolver.resolveByBundle(myClass);

            if (localizeClass == null) {
                return false;
            }

            myLocalizeClassName = localizeClass.getName();
            myLocalizeClassQualifiedName = localizeClass.getQualifiedName();
            return true;
        }
    }
}
