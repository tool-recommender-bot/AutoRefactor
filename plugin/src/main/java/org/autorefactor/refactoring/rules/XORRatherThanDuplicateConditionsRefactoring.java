/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2013-2015 Jean-Noël Rouvignac - initial API and implementation
 * Copyright (C) 2016-2017 Fabrice Tiercelin - Make sure we do not visit again modified nodes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program under LICENSE-GNUGPL.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution under LICENSE-ECLIPSE, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.autorefactor.refactoring.rules;

import static org.autorefactor.refactoring.ASTHelper.DO_NOT_VISIT_SUBTREE;
import static org.autorefactor.refactoring.ASTHelper.VISIT_SUBTREE;
import static org.autorefactor.refactoring.ASTHelper.as;
import static org.autorefactor.refactoring.ASTHelper.hasOperator;
import static org.autorefactor.refactoring.ASTHelper.isPassive;
import static org.autorefactor.refactoring.ASTHelper.match;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_AND;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.CONDITIONAL_OR;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.OR;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.XOR;
import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.NOT;

import java.util.concurrent.atomic.AtomicBoolean;

import org.autorefactor.refactoring.ASTBuilder;
import org.autorefactor.refactoring.ASTSemanticMatcher;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

/** See {@link #getDescription()} method. */
public class XORRatherThanDuplicateConditionsRefactoring extends AbstractRefactoringRule {
    /**
     * Get the name.
     *
     * @return the name.
     */
    public String getName() {
        return "XOR rather than duplicate conditions";
    }

    /**
     * Get the description.
     *
     * @return the description.
     */
    public String getDescription() {
        return ""
            + "Replace (X && !Y) || (!X && Y) by X ^ Y.";
    }

    /**
     * Get the reason.
     *
     * @return the reason.
     */
    public String getReason() {
        return "It improves the readibility.";
    }

    @Override
    public boolean visit(InfixExpression node) {
        if (hasOperator(node, CONDITIONAL_OR, OR) && !node.hasExtendedOperands()) {
            final InfixExpression firstCondition = as(node.getLeftOperand(), InfixExpression.class);
            final InfixExpression secondCondition = as(node.getRightOperand(), InfixExpression.class);

            if (firstCondition != null
                    && !firstCondition.hasExtendedOperands()
                    && hasOperator(firstCondition, CONDITIONAL_AND, AND)
                    && secondCondition != null
                    && !secondCondition.hasExtendedOperands()
                    && hasOperator(secondCondition, CONDITIONAL_AND, AND)) {
                final AtomicBoolean isFirstExprPositive = new AtomicBoolean();
                final AtomicBoolean isSecondExprPositive = new AtomicBoolean();
                final AtomicBoolean isThirdExprPositive = new AtomicBoolean();
                final AtomicBoolean isFourthExprPositive = new AtomicBoolean();

                final Expression firstExpr = getBasisExpression(firstCondition.getLeftOperand(), isFirstExprPositive);
                final Expression secondExpr = getBasisExpression(firstCondition.getRightOperand(),
                        isSecondExprPositive);
                final Expression thirdExpr = getBasisExpression(secondCondition.getLeftOperand(), isThirdExprPositive);
                final Expression fourthExpr = getBasisExpression(secondCondition.getRightOperand(),
                        isFourthExprPositive);

                if (isPassive(firstExpr) && isPassive(secondExpr)
                        && ((match(new ASTSemanticMatcher(), firstExpr, thirdExpr)
                                && match(new ASTSemanticMatcher(), secondExpr, fourthExpr)
                        && isFirstExprPositive.get() ^ isThirdExprPositive.get()
                        && isSecondExprPositive.get() ^ isFourthExprPositive.get())
                        || (match(new ASTSemanticMatcher(), firstExpr, fourthExpr)
                                && match(new ASTSemanticMatcher(), secondExpr, thirdExpr)
                        && isFirstExprPositive.get() ^ isFourthExprPositive.get()
                        && isSecondExprPositive.get() ^ isThirdExprPositive.get()))) {
                    replaceDuplicateExpr(node, firstExpr, secondExpr, isFirstExprPositive, isSecondExprPositive);
                    return DO_NOT_VISIT_SUBTREE;
                }
            }
        }
        return VISIT_SUBTREE;
    }

    private Expression getBasisExpression(final Expression originalExpr, final AtomicBoolean isExprPositive) {
        Expression basisExpr = null;
        final PrefixExpression negateExpr = as(originalExpr, PrefixExpression.class);
        if (hasOperator(negateExpr, NOT)) {
            basisExpr = negateExpr.getOperand();
            isExprPositive.set(false);
        } else {
            basisExpr = originalExpr;
            isExprPositive.set(true);
        }
        return basisExpr;
    }

    private void replaceDuplicateExpr(final InfixExpression node, final Expression firstExpr,
            final Expression secondExpr,
            final AtomicBoolean isFirstExprPositive,
            final AtomicBoolean isSecondExprPositive) {
        final ASTBuilder b = ctx.getASTBuilder();
        if (isFirstExprPositive.get() == isSecondExprPositive.get()) {
            ctx.getRefactorings().replace(node,
                    b.infixExpr(b.copy(firstExpr), EQUALS, b.copy(secondExpr)));
        } else {
            ctx.getRefactorings().replace(node,
                    b.infixExpr(b.copy(firstExpr), XOR, b.copy(secondExpr)));
        }
    }
}
