/*
 * The MIT License
 *
 *  Copyright (c) 2021, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package io.github.bakhomious.rules.spel;

import io.github.bakhomious.rules.api.Action;
import io.github.bakhomious.rules.api.Facts;

import lombok.extern.slf4j.Slf4j;

import org.springframework.expression.BeanResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;


/**
 * This class is an implementation of {@link Action} that uses
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#expressions">SpEL</a>
 * to execute the action.
 * <p>
 * Each fact is set as a variable in the {@link org.springframework.expression.EvaluationContext}.
 * <p>
 * The facts map is set as the root object of the {@link org.springframework.expression.EvaluationContext}.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com) from Easy Rules
 */
@Slf4j
public class SpELAction implements Action {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final String expression;
    private final Expression compiledExpression;
    private BeanResolver beanResolver;

    /**
     * Create a new {@link SpELAction}.
     *
     * @param expression the action written in expression language
     */
    public SpELAction(final String expression) {
        this(expression, ParserContext.TEMPLATE_EXPRESSION);
    }

    /**
     * Create a new {@link SpELAction}.
     *
     * @param expression    the action written in expression language
     * @param parserContext the SpEL parser context
     */
    public SpELAction(final String expression, final ParserContext parserContext) {
        this.expression = expression;
        compiledExpression = parser.parseExpression(expression, parserContext);
    }

    /**
     * Create a new {@link SpELAction}.
     *
     * @param expression   the action written in expression language
     * @param beanResolver the bean resolver used to resolve bean references
     */
    public SpELAction(final String expression, final BeanResolver beanResolver) {
        this(expression, ParserContext.TEMPLATE_EXPRESSION, beanResolver);
    }

    /**
     * Create a new {@link SpELAction}.
     *
     * @param expression    the action written in expression language
     * @param beanResolver  the bean resolver used to resolve bean references
     * @param parserContext the SpEL parser context
     */
    public SpELAction(final String expression, final ParserContext parserContext, final BeanResolver beanResolver) {
        this.expression = expression;
        this.beanResolver = beanResolver;
        compiledExpression = parser.parseExpression(expression, parserContext);
    }

    @Override
    public void execute(final Facts facts) {
        try {
            final StandardEvaluationContext context = new StandardEvaluationContext();
            context.setRootObject(facts.asMap());
            context.setVariables(facts.asMap());
            if (beanResolver != null) {
                context.setBeanResolver(beanResolver);
            }
            compiledExpression.getValue(context);
        } catch (final Exception e) {
            log.error("Unable to evaluate expression: '{}' on facts: {}", expression, facts, e);
            throw e;
        }
    }

}
