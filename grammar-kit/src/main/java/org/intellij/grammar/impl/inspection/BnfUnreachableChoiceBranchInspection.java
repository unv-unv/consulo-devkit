/*
 * Copyright 2011-present Greg Shrago
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

package org.intellij.grammar.impl.inspection;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.ast.ASTNode;
import consulo.document.util.TextRange;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.impl.ast.TreeUtil;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.psi.PsiRecursiveElementWalkingVisitor;
import org.intellij.grammar.analysis.BnfFirstNextAnalyzer;
import org.intellij.grammar.psi.BnfChoice;
import org.intellij.grammar.psi.BnfExpression;
import org.intellij.grammar.psi.BnfTypes;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author gregsh
 */
@ExtensionImpl
public class BnfUnreachableChoiceBranchInspection extends LocalInspectionTool {
    @Nls
    @Nonnull
    @Override
    public String getGroupDisplayName() {
        return "Grammar/BNF";
    }

    @Nls
    @Nonnull
    @Override
    public String getDisplayName() {
        return "Unreachable choice branch";
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "BnfUnreachableChoiceBranchInspection";
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public ProblemDescriptor[] checkFile(@Nonnull PsiFile file, @Nonnull InspectionManager manager, boolean isOnTheFly) {
        ProblemsHolder problemsHolder = new ProblemsHolder(manager, file, isOnTheFly);
        checkFile(file, problemsHolder);
        return problemsHolder.getResultsArray();
    }

    @RequiredReadAction
    private static void checkFile(PsiFile file, final ProblemsHolder problemsHolder) {
        file.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (element instanceof BnfChoice choice) {
                    //noinspection RequiredXAction
                    checkChoice(choice, problemsHolder);
                }
                super.visitElement(element);
            }
        });
    }

    @RequiredReadAction
    private static void checkChoice(BnfChoice choice, ProblemsHolder problemsHolder) {
        Set<BnfExpression> visited = new HashSet<>();
        Set<BnfExpression> first = new HashSet<>();
        BnfFirstNextAnalyzer analyzer = new BnfFirstNextAnalyzer().setPredicateLookAhead(true);
        List<BnfExpression> list = choice.getExpressionList();
        for (int i = 0, listSize = list.size() - 1; i < listSize; i++) {
            BnfExpression child = list.get(i);
            Set<BnfExpression> firstSet = analyzer.calcFirstInner(child, first, visited);
            if (firstSet.contains(BnfFirstNextAnalyzer.BNF_MATCHES_NOTHING)) {
                registerProblem(choice, child, "Branch is unable to match anything due to & or ! conditions", problemsHolder);
            }
            else if (firstSet.contains(BnfFirstNextAnalyzer.BNF_MATCHES_EOF)) {
                registerProblem(choice, child, "Branch matches empty input making the rest branches unreachable", problemsHolder);
                break;
            }
            first.clear();
            visited.clear();
        }
    }

    @RequiredReadAction
    static void registerProblem(
        BnfExpression choice,
        BnfExpression branch,
        String message,
        ProblemsHolder problemsHolder,
        LocalQuickFix... fixes
    ) {
        TextRange textRange = branch.getTextRange();
        if (textRange.isEmpty()) {
            ASTNode nextOr = TreeUtil.findSibling(branch.getNode(), BnfTypes.BNF_OP_OR);
            ASTNode prevOr = TreeUtil.findSiblingBackward(branch.getNode(), BnfTypes.BNF_OP_OR);

            int shift = choice.getTextRange().getStartOffset();
            int startOffset = prevOr != null ? prevOr.getStartOffset() - shift : 0;
            TextRange range = new TextRange(
                startOffset,
                nextOr != null ? nextOr.getStartOffset() + 1 - shift : Math.min(startOffset + 2, choice.getTextLength())
            );
            problemsHolder.registerProblem(choice, range, message, fixes);
        }
        else {
            problemsHolder.registerProblem(branch, message, fixes);
        }
    }
}
