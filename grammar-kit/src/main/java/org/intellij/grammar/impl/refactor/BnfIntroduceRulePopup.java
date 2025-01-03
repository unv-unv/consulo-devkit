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
package org.intellij.grammar.impl.refactor;

import consulo.application.AccessToken;
import consulo.application.Result;
import consulo.application.WriteAction;
import consulo.codeEditor.Editor;
import consulo.codeEditor.ScrollType;
import consulo.codeEditor.markup.RangeHighlighter;
import consulo.document.Document;
import consulo.document.RangeMarker;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.completion.lookup.LookupEx;
import consulo.language.editor.completion.lookup.LookupManager;
import consulo.language.editor.refactoring.introduce.inplace.InplaceVariableIntroducer;
import consulo.language.psi.PsiDocumentManager;
import consulo.project.Project;
import consulo.ui.ex.awt.NonFocusableCheckBox;
import consulo.util.collection.ContainerUtil;
import jakarta.annotation.Nonnull;
import org.intellij.grammar.psi.BnfExpression;
import org.intellij.grammar.psi.BnfRule;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * @author Vadim Romansky
 * @author gregsh
 */
public class BnfIntroduceRulePopup extends InplaceVariableIntroducer<BnfExpression> {
    private static final String PRIVATE = "private ";

    private final JPanel myPanel = new JPanel(new GridBagLayout());
    private final JCheckBox myCheckBox = new NonFocusableCheckBox("Declare private");

    public BnfIntroduceRulePopup(Project project, Editor editor, BnfRule rule, BnfExpression expr) {
        super(rule, editor, project, "Introduce Rule", new BnfExpression[0], expr);

        myCheckBox.setSelected(true);
        myCheckBox.setMnemonic('p');

        myPanel.setBorder(null);
        myPanel.add(
            myCheckBox,
            new GridBagConstraints(
                0,
                1,
                1,
                1,
                1,
                0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5),
                0,
                0
            )
        );
        myPanel.add(
            Box.createVerticalBox(),
            new GridBagConstraints(
                0,
                2,
                1,
                1,
                1,
                1,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0,
                0
            )
        );
    }

    @Override
    protected void moveOffsetAfter(boolean success) {
        RangeMarker exprMarker = getExprMarker();
        final AccessToken accessToken = WriteAction.start();
        try {
            Document document = myEditor.getDocument();
            // todo restore original expression if not success
            PsiDocumentManager.getInstance(myProject).commitDocument(document);
            if (exprMarker != null && exprMarker.isValid()) {
                myEditor.getCaretModel().moveToOffset(exprMarker.getStartOffset());
                myEditor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                exprMarker.dispose();
            }
        }
        finally {
            accessToken.finish();
        }
    }

    @Override
    protected JComponent getComponent() {
        myCheckBox.addActionListener(e -> new WriteCommandAction(
            myProject,
            BnfIntroduceRuleHandler.REFACTORING_NAME,
            BnfIntroduceRuleHandler.REFACTORING_NAME
        ) {
            @Override
            protected void run(@Nonnull Result result) throws Throwable {
                perform(myCheckBox.isSelected());
            }
        }.execute());
        return myPanel;
    }

    public void perform(final boolean generatePrivate) {
        final Runnable runnable = () -> {
            final Document document = myEditor.getDocument();

            int exprOffset = myExprMarker.getStartOffset();
            final int lineOffset = getLineOffset(document, exprOffset);
            if (generatePrivate) {
                final Collection<RangeMarker> leftGreedyMarker = ContainerUtil.newArrayList();
                final Collection<RangeMarker> emptyMarkers = ContainerUtil.newArrayList();
                for (RangeHighlighter rangeHighlighter : myEditor.getMarkupModel().getAllHighlighters()) {
                    collectRangeMarker(rangeHighlighter, lineOffset, leftGreedyMarker, emptyMarkers);
                }
                document.processRangeMarkers(rangeMarker -> {
                    collectRangeMarker(rangeMarker, lineOffset, leftGreedyMarker, emptyMarkers);
                    return true;
                });
                setLeftGreedy(leftGreedyMarker, false);
                setRightGreedy(emptyMarkers, true);

                // workaround for shifting empty ranges to the left
                document.insertString(lineOffset, " ");
                document.insertString(lineOffset, PRIVATE);
                document.deleteString(lineOffset + PRIVATE.length(), lineOffset + PRIVATE.length() + 1);

                setLeftGreedy(leftGreedyMarker, true);
                setRightGreedy(emptyMarkers, false);
            }
            else {
                int idx = document.getText().indexOf(PRIVATE, lineOffset);
                if (idx > -1 && idx < exprOffset) {
                    document.deleteString(idx, idx + PRIVATE.length());
                }
            }
            PsiDocumentManager.getInstance(myProject).commitDocument(document);
        };
        final LookupEx lookup = LookupManager.getActiveLookup(myEditor);
        if (lookup != null) {
            lookup.performGuardedChange(runnable);
        }
        else {
            runnable.run();
        }
    }

    private void setRightGreedy(Collection<RangeMarker> rightRestore, boolean greedyToRight) {
        for (RangeMarker rangeMarker : rightRestore) {
            rangeMarker.setGreedyToRight(greedyToRight);
        }
    }

    private void setLeftGreedy(Collection<RangeMarker> leftRestore, boolean greedyToLeft) {
        for (RangeMarker rangeMarker : leftRestore) {
            rangeMarker.setGreedyToLeft(greedyToLeft);
        }
    }

    private void collectRangeMarker(
        RangeMarker rangeMarker,
        int lineOffset,
        Collection<RangeMarker> leftGreedyMarkers,
        Collection<RangeMarker> emptyMarkers
    ) {
        if (rangeMarker.getStartOffset() == lineOffset && rangeMarker.isGreedyToLeft()) {
            leftGreedyMarkers.add(rangeMarker);
        }
        if (rangeMarker.getStartOffset() == lineOffset && rangeMarker.getEndOffset() == lineOffset && !rangeMarker.isGreedyToRight()) {
            emptyMarkers.add(rangeMarker);
        }
    }

    private int getLineOffset(Document document, final int offset) {
        return 0 <= offset && offset < document.getTextLength()
            ? document.getLineStartOffset(document.getLineNumber(offset))
            : 0;
    }
}
