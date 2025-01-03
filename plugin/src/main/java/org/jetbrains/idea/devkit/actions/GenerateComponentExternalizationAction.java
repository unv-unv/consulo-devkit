/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package org.jetbrains.idea.devkit.actions;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.codeStyle.JavaCodeStyleManager;
import consulo.codeEditor.Editor;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.dataContext.DataContext;
import consulo.devkit.localize.DevKitLocalize;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.LangDataKeys;
import consulo.language.editor.PlatformDataKeys;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.undoRedo.CommandProcessor;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author max
 */
// TODO review and resurrect
public class GenerateComponentExternalizationAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(GenerateComponentExternalizationAction.class);
    private final static String PERSISTENCE_STATE_COMPONENT = PersistentStateComponent.class.getName();
    private final static String STATE = State.class.getName();
    private final static String STORAGE = Storage.class.getName();

    @Override
    @RequiredUIAccess
    public void actionPerformed(@Nonnull AnActionEvent e) {
        final PsiClass target = getComponentInContext(e.getDataContext());
        assert target != null;

        final PsiElementFactory factory = JavaPsiFacade.getInstance(target.getProject()).getElementFactory();
        final CodeStyleManager formatter = CodeStyleManager.getInstance(target.getManager().getProject());
        final JavaCodeStyleManager styler = JavaCodeStyleManager.getInstance(target.getProject());
        final String qualifiedName = target.getQualifiedName();
        Runnable runnable = () -> target.getProject().getApplication().runWriteAction(() -> {
            try {
                final PsiReferenceList implList = target.getImplementsList();
                assert implList != null;
                final PsiJavaCodeReferenceElement referenceElement =
                    factory.createReferenceFromText(PERSISTENCE_STATE_COMPONENT + "<" + qualifiedName + ">", target);
                implList.add(styler.shortenClassReferences(referenceElement.copy()));
                PsiMethod read = factory.createMethodFromText(
                    "public void loadState(" + qualifiedName + " state) {\n" +
                        "    consulo.util.xml.serializer.XmlSerializerUtil.copyBean(state, this);\n" +
                        "}",
                    target
                );

                read = (PsiMethod)formatter.reformat(target.add(read));
                styler.shortenClassReferences(read);

                PsiMethod write = factory.createMethodFromText(
                    "public " + qualifiedName + " getState() {\n" +
                        "    return this;\n" +
                        "}\n",
                    target
                );
                write = (PsiMethod)formatter.reformat(target.add(write));
                styler.shortenClassReferences(write);

                PsiAnnotation annotation = target.getModifierList().addAnnotation(STATE);

                annotation =
                    (PsiAnnotation)formatter.reformat(annotation.replace(factory.createAnnotationFromText(
                        "@" + STATE + "(name = \"" + qualifiedName + "\", " + "storages = {@" + STORAGE + "(StoragePathMacros.DEFAULT_FILE)})",
                        target
                    )));
                styler.shortenClassReferences(annotation);
            }
            catch (IncorrectOperationException e1) {
                LOG.error(e1);
            }
        });

        CommandProcessor.getInstance()
            .executeCommand(target.getProject(), runnable, DevKitLocalize.commandImplementExternalizable().get(), null);
    }

    @Nullable
    private PsiClass getComponentInContext(DataContext context) {
        Editor editor = context.getData(PlatformDataKeys.EDITOR);
        Project project = context.getData(PlatformDataKeys.PROJECT);
        if (editor == null || project == null) {
            return null;
        }

        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());

        PsiFile file = context.getData(LangDataKeys.PSI_FILE);
        if (file == null) {
            return null;
        }

        PsiClass contextClass = PsiTreeUtil.findElementOfClassAtOffset(file, editor.getCaretModel().getOffset(), PsiClass.class, false);
        if (contextClass == null || contextClass.isEnum() || contextClass.isInterface() || contextClass instanceof PsiAnonymousClass) {
            return null;
        }

        PsiClass externClass = JavaPsiFacade.getInstance(file.getProject()).findClass(PERSISTENCE_STATE_COMPONENT, file.getResolveScope());
        if (externClass == null || contextClass.isInheritor(externClass, true)) {
            return null;
        }


        return contextClass;
    }

    @Override
    @RequiredUIAccess
    public void update(@Nonnull AnActionEvent e) {
        super.update(e);
        final PsiClass target = getComponentInContext(e.getDataContext());

        final Presentation presentation = e.getPresentation();
        presentation.setEnabled(target != null);
        presentation.setVisible(target != null);
    }
}
