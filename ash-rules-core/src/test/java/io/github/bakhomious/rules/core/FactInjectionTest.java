/*
 * The MIT License
 *
 *  Copyright (c) 2020, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
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
package io.github.bakhomious.rules.core;

import io.github.bakhomious.rules.annotation.Action;
import io.github.bakhomious.rules.annotation.Condition;
import io.github.bakhomious.rules.annotation.Fact;
import io.github.bakhomious.rules.annotation.Rule;
import io.github.bakhomious.rules.api.Facts;
import io.github.bakhomious.rules.api.Rules;
import io.github.bakhomious.rules.api.RulesEngine;
import io.github.bakhomious.rules.core.DefaultRulesEngine;

import lombok.Getter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class FactInjectionTest {

    @Test
    public void declaredFactsShouldBeCorrectlyInjectedByNameOrType() {
        // Given
        final Object fact1 = new Object();
        final Object fact2 = new Object();
        final Facts facts = new Facts();
        facts.put("fact1", fact1);
        facts.put("fact2", fact2);

        final DummyRule rule = new DummyRule();
        final Rules rules = new Rules(rule);

        // When
        final RulesEngine rulesEngine = new DefaultRulesEngine();
        rulesEngine.fire(rules, facts);

        // Then
        assertThat(rule.getFact1()).isSameAs(fact1);
        assertThat(rule.getFact2()).isSameAs(fact2);
        assertThat(rule.getFacts()).isSameAs(facts);
    }

    @Test
    public void rulesShouldBeExecutedWhenFactsAreCorrectlyInjected() {
        // Given
        final Facts facts = new Facts();
        facts.put("rain", true);
        facts.put("age", 18);

        final WeatherRule weatherRule = new WeatherRule();
        final AgeRule ageRule = new AgeRule();
        final Rules rules = new Rules(weatherRule, ageRule);

        // When
        final RulesEngine rulesEngine = new DefaultRulesEngine();
        rulesEngine.fire(rules, facts);

        // Then
        assertThat(ageRule.isExecuted()).isTrue();
        assertThat(weatherRule.isExecuted()).isTrue();
    }

    @Test
    public void whenFactTypeDoesNotMatchParameterType_thenTheRuleShouldNotBeExecuted() {
        // Given
        final Facts facts = new Facts();
        facts.put("age", "foo");
        final AgeRule ageRule = new AgeRule();
        final Rules rules = new Rules(ageRule);
        final RulesEngine rulesEngine = new DefaultRulesEngine();

        // When
        rulesEngine.fire(rules, facts);

        // Then
        assertThat(ageRule.isExecuted()).isFalse();
    }

    @Test
    public void whenADeclaredFactIsMissingInEvaluateMethod_thenTheRuleShouldNotBeExecuted() {
        // Given
        final Facts facts = new Facts();
        final AgeRule ageRule = new AgeRule();
        final Rules rules = new Rules(ageRule);
        final RulesEngine rulesEngine = new DefaultRulesEngine();

        // When
        rulesEngine.fire(rules, facts);

        // Then
        assertThat(ageRule.isExecuted()).isFalse();
    }

    @Test
    public void whenADeclaredFactIsMissingInExecuteMethod_thenTheRuleShouldNotBeExecuted() {
        // Given
        final Facts facts = new Facts();
        final AnotherDummyRule rule = new AnotherDummyRule();
        final Rules rules = new Rules(rule);
        final RulesEngine rulesEngine = new DefaultRulesEngine();

        // When
        rulesEngine.fire(rules, facts);

        // Then
        assertThat(rule.isExecuted()).isFalse();
    }

    @Getter
    @Rule
    static class DummyRule {

        private Object fact1, fact2;
        private Facts facts;

        @Condition
        public boolean when(@Fact("fact1") final Object fact1, @Fact("fact2") final Object fact2) {
            this.fact1 = fact1;
            this.fact2 = fact2;
            return true;
        }

        @Action
        public void then(final Facts facts) {
            this.facts = facts;
        }

    }

    @Getter
    @Rule
    static class AnotherDummyRule {

        private boolean isExecuted;

        @Condition
        public boolean when() {
            return true;
        }

        @Action
        public void then(@Fact("foo") final Object fact) {
            isExecuted = true;
        }

    }

    @Getter
    @Rule
    static class AgeRule {

        private boolean isExecuted;

        @Condition
        public boolean isAdult(@Fact("age") final int age) {
            return age >= 18;
        }

        @Action
        public void printYourAreAdult() {
            System.out.println("You are an adult");
            isExecuted = true;
        }

    }

    @Getter
    @Rule
    static class WeatherRule {

        private boolean isExecuted;

        @Condition
        public boolean itRains(@Fact("rain") final boolean rain) {
            return rain;
        }

        @Action
        public void takeAnUmbrella(final Facts facts) {
            System.out.println("It rains, take an umbrella!");
            isExecuted = true;
        }

    }

}
