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


import io.github.bakhomious.rules.api.Condition;
import io.github.bakhomious.rules.api.Facts;

import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;


public class SpELConditionTest {

    @Test
    public void testSpELExpressionEvaluation() {
        // given
        final Condition isAdult = new SpELCondition("#{ ['person'].age > 18 }");
        final Facts facts = new Facts();
        facts.put("person", new Person("foo", 20));
        // when
        final boolean evaluationResult = isAdult.evaluate(facts);

        // then
        assertThat(evaluationResult).isTrue();
    }

    // Note this behaviour is different in MVEL, where a missing fact yields an exception
    @Test
    public void whenDeclaredFactIsNotPresent_thenShouldReturnFalse() {
        // given
        final Condition isHot = new SpELCondition("#{ ['temperature'] > 30 }");
        final Facts facts = new Facts();

        // when
        final boolean evaluationResult = isHot.evaluate(facts);

        // then
        assertThat(evaluationResult).isFalse();
    }

    @Test
    public void testSpELConditionWithExpressionAndParserContext() {
        // given
        final ParserContext context = new TemplateParserContext("%{", "}"); // custom parser context
        final Condition condition = new SpELCondition("%{ T(java.lang.Integer).MAX_VALUE > 1 }", context);
        final Facts facts = new Facts();

        // when
        final boolean evaluationResult = condition.evaluate(facts);

        // then
        assertThat(evaluationResult).isTrue();
    }

    @Test
    public void testSpELConditionWithExpressionAndParserContextAndBeanResolver() throws Exception {

        final ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MySpringAppConfig.class);
        final BeanResolver beanResolver = new SimpleBeanResolver(applicationContext);

        final SpELRule spELRule = new SpELRule(beanResolver);
        // setting an condition to be evaluated
        spELRule.when("#{ ['person'].age >= 18 }");
        // provided an bean resolver that can resolve "myGreeter"
        spELRule.then("#{ @myGreeter.greeting(#person.name) }");

        // given
        final Facts facts = new Facts();
        facts.put("person", new Person("jack", 19));

        // then
        final boolean evaluationResult = spELRule.evaluate(facts);
        Assertions.assertThat(evaluationResult).isTrue();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream originalOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            spELRule.execute(facts);
        } finally {
            System.setOut(originalOut);
        }
        final String output = baos.toString().replace("\r\n", "\n");
        assertThat(output).isEqualTo("Bonjour jack!\n");

    }

}
