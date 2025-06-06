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

package org.intellij.grammar.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.documentation.LanguageDocumentationProvider;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.ui.ex.JBColor;
import consulo.ui.ex.awt.util.ColorUtil;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nullable;
import org.intellij.grammar.BnfLanguage;
import org.intellij.grammar.KnownAttribute;
import org.intellij.grammar.analysis.BnfFirstNextAnalyzer;
import org.intellij.grammar.generator.BnfConstants;
import org.intellij.grammar.generator.ExpressionHelper;
import org.intellij.grammar.generator.ParserGeneratorUtil;
import org.intellij.grammar.generator.RuleGraphHelper;
import org.intellij.grammar.psi.BnfAttr;
import org.intellij.grammar.psi.BnfExpression;
import org.intellij.grammar.psi.BnfFile;
import org.intellij.grammar.psi.BnfRule;

import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author gregsh
 */
@ExtensionImpl
public class BnfDocumentationProvider implements LanguageDocumentationProvider {
    @Nonnull
    @Override
    public Language getLanguage() {
        return BnfLanguage.INSTANCE;
    }

    @Nullable
    @Override
    @RequiredReadAction
    public String generateDoc(final PsiElement element, final PsiElement originalElement) {
        if (element instanceof BnfRule rule) {
            BnfFirstNextAnalyzer analyzer = new BnfFirstNextAnalyzer();
            Set<String> first = analyzer.asStrings(analyzer.calcFirst(rule));
            Set<String> next = analyzer.asStrings(analyzer.calcNext(rule).keySet());

            StringBuilder docBuilder = new StringBuilder();
            String[] firstS = first.toArray(new String[first.size()]);
            Arrays.sort(firstS);
            docBuilder.append("<h1>Starts with:</h1>");
            docBuilder.append("<code>").append(StringUtil.escapeXml(StringUtil.join(firstS, " | "))).append("</code>");

            String[] nextS = next.toArray(new String[next.size()]);
            Arrays.sort(nextS);
            docBuilder.append("<br><h1>Followed by:</h1>");
            docBuilder.append("<code>").append(StringUtil.escapeXml(StringUtil.join(nextS, " | "))).append("</code>");

            BnfFile file = (BnfFile)rule.getContainingFile();
            String recover = file.findAttributeValue(file.getVersion(), rule, KnownAttribute.RECOVER_WHILE, null);
            if (BnfConstants.RECOVER_AUTO.equals(recover)) {
                docBuilder.append("<br><h1>#auto recovery predicate:</h1>");
                docBuilder.append("<code>");
                docBuilder.append("private ").append(rule.getName()).append("_recover ::= !(");
                boolean f = true;
                for (String s : nextS) {
                    if (s.startsWith("-") || s.startsWith("<")) {
                        continue;
                    }
                    if (file.getRule(s) != null) {
                        continue;
                    }
                    if (f) {
                        f = false;
                    }
                    else {
                        docBuilder.append(" | ");
                    }
                    docBuilder.append(StringUtil.escapeXml(s));
                }
                docBuilder.append(")");
                docBuilder.append("</code>");
            }
            dumpPriorityTable(docBuilder, rule, file);
            dumpContents(docBuilder, rule, file);
            return docBuilder.toString();
        }
        else if (element instanceof BnfAttr bnfAttr) {
            KnownAttribute attribute = KnownAttribute.getAttribute(bnfAttr.getName());
            if (attribute != null) {
                return attribute.getDescription();
            }
        }
        return null;
    }

    @RequiredReadAction
    private static void dumpContents(StringBuilder docBuilder, BnfRule rule, BnfFile file) {
        Map<PsiElement, RuleGraphHelper.Cardinality> map = RuleGraphHelper.getCached(file).getFor(rule);
        Collection<BnfRule> sortedPublicRules = ParserGeneratorUtil.getSortedPublicRules(map.keySet());
        Collection<BnfExpression> sortedTokens = ParserGeneratorUtil.getSortedTokens(map.keySet());
        Collection<LeafPsiElement> sortedExternalRules = ParserGeneratorUtil.getSortedExternalRules(map.keySet());
        if (sortedPublicRules.isEmpty() && sortedTokens.isEmpty()) {
            docBuilder.append("\n<br><h1>Contains no public rules and no tokens</h1>");
        }
        else {
            if (sortedPublicRules.size() > 0) {
                printElements(map, sortedPublicRules, docBuilder.append("\n<br><h1>Contains public rules:</h1>"));
            }
            else {
                docBuilder.append("<h2>Contains no public rules</h2>");
            }
            if (sortedTokens.size() > 0) {
                printElements(map, sortedTokens, docBuilder.append("\n<br><h1>Contains tokens:</h1>"));
            }
            else {
                docBuilder.append("<h2>Contains no tokens</h2>");
            }
        }
        if (!sortedExternalRules.isEmpty()) {
            printElements(map, sortedExternalRules, docBuilder.append("\n<br><h1>Contains external rules:</h1>"));
        }
    }

    private static void dumpPriorityTable(StringBuilder docBuilder, BnfRule rule, BnfFile file) {
        ExpressionHelper.ExpressionInfo expressionInfo = ExpressionHelper.getCached(file).getExpressionInfo(rule);
        if (expressionInfo == null) {
            return;
        }
        final ExpressionHelper.OperatorInfo ruleOperator = expressionInfo.operatorMap.get(rule);

        docBuilder.append("\n<br><h1>Priority table:");
        if (ruleOperator != null) {
            appendColored(docBuilder, " " + ruleOperator.type + "-" + expressionInfo.getPriority(rule));
        }
        docBuilder.append("</h1>");
        expressionInfo.dumpPriorityTable(docBuilder.append("<code><pre>"), (sb, operatorInfo) -> {
            if (operatorInfo == ruleOperator) {
                appendColored(sb, operatorInfo);
            }
            else {
                sb.append(operatorInfo);
            }

        }).append("</pre></code>");
    }

    private static void appendColored(StringBuilder sb, Object o) {
        sb.append("<font").append(" color=\"#").append(ColorUtil.toHex(JBColor.BLUE)).append("\">");
        sb.append(o);
        sb.append("</font>");
    }

    @RequiredReadAction
    public static void printElements(
        Map<PsiElement, RuleGraphHelper.Cardinality> map,
        Collection<? extends PsiElement> collection,
        StringBuilder sb
    ) {
        for (PsiElement r : collection) {
            sb.append(" ").append(r instanceof PsiNamedElement namedElement ? namedElement.getName() : r.getText()).
                append(RuleGraphHelper.getCardinalityText(map.get(r)));
        }
    }
}
