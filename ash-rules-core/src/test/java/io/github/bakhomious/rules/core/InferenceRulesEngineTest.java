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
import io.github.bakhomious.rules.annotation.Priority;
import io.github.bakhomious.rules.annotation.Rule;
import io.github.bakhomious.rules.api.Facts;
import io.github.bakhomious.rules.api.Rules;
import io.github.bakhomious.rules.api.RulesEngine;
import io.github.bakhomious.rules.api.RulesEngineListener;
import io.github.bakhomious.rules.core.InferenceRulesEngine;

import lombok.Getter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


public class InferenceRulesEngineTest {

    @Test
    public void whenFireRules_thenNullRulesShouldNotBeAccepted() {
        final InferenceRulesEngine engine = new InferenceRulesEngine();
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> engine.fire(null, new Facts()));
    }

    @Test
    public void whenFireRules_thenNullFactsShouldNotBeAccepted() {
        final InferenceRulesEngine engine = new InferenceRulesEngine();
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> engine.fire(new Rules(), null));
    }

    @Test
    public void whenCheckRules_thenNullRulesShouldNotBeAccepted() {
        final InferenceRulesEngine engine = new InferenceRulesEngine();
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> engine.check(null, new Facts()));
    }

    @Test
    public void whenCheckRules_thenNullFactsShouldNotBeAccepted() {
        final InferenceRulesEngine engine = new InferenceRulesEngine();
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> engine.check(new Rules(), null));
    }

    @Test
    public void testCandidateSelection() {
        // Given
        final Facts facts = new Facts();
        facts.put("foo", true);
        final DummyRule dummyRule = new DummyRule();
        final AnotherDummyRule anotherDummyRule = new AnotherDummyRule();
        final Rules rules = new Rules(dummyRule, anotherDummyRule);
        final RulesEngine rulesEngine = new InferenceRulesEngine();

        // When
        rulesEngine.fire(rules, facts);

        // Then
        assertThat(dummyRule.isExecuted()).isTrue();
        assertThat(anotherDummyRule.isExecuted()).isFalse();
    }

    @Test
    public void testCandidateOrdering() {
        // Given
        final Facts facts = new Facts();
        facts.put("foo", true);
        facts.put("bar", true);
        final DummyRule dummyRule = new DummyRule();
        final AnotherDummyRule anotherDummyRule = new AnotherDummyRule();
        final Rules rules = new Rules(dummyRule, anotherDummyRule);
        final RulesEngine rulesEngine = new InferenceRulesEngine();

        // When
        rulesEngine.fire(rules, facts);

        // Then
        assertThat(dummyRule.isExecuted()).isTrue();
        assertThat(anotherDummyRule.isExecuted()).isTrue();
        assertThat(dummyRule.getTimestamp()).isLessThanOrEqualTo(anotherDummyRule.getTimestamp());
    }

    @Test
    public void testRulesEngineListener() {
        // Given
        class StubRulesEngineListener implements RulesEngineListener {

            private boolean executedBeforeEvaluate;
            private boolean executedAfterExecute;

            @Override
            public void beforeEvaluate(final Rules rules, final Facts facts) {
                executedBeforeEvaluate = true;
            }

            @Override
            public void afterExecute(final Rules rules, final Facts facts) {
                executedAfterExecute = true;
            }

            private boolean isExecutedBeforeEvaluate() {
                return executedBeforeEvaluate;
            }

            private boolean isExecutedAfterExecute() {
                return executedAfterExecute;
            }

        }

        final Facts facts = new Facts();
        facts.put("foo", true);
        final DummyRule rule = new DummyRule();
        final Rules rules = new Rules(rule);
        final StubRulesEngineListener rulesEngineListener = new StubRulesEngineListener();

        // When
        final InferenceRulesEngine rulesEngine = new InferenceRulesEngine();
        rulesEngine.registerRulesEngineListener(rulesEngineListener);
        rulesEngine.fire(rules, facts);

        // Then
        // Rules engine listener should be invoked
        assertThat(rulesEngineListener.isExecutedBeforeEvaluate()).isTrue();
        assertThat(rulesEngineListener.isExecutedAfterExecute()).isTrue();
        assertThat(rule.isExecuted()).isTrue();
    }

    @Getter
    @Rule
    static class DummyRule {

        private boolean isExecuted;
        private long timestamp;

        @Condition
        public boolean when(@Fact("foo") final boolean foo) {
            return foo;
        }

        @Action
        public void then(final Facts facts) {
            isExecuted = true;
            timestamp = System.currentTimeMillis();
            facts.remove("foo");
        }

        @Priority
        public int priority() {
            return 1;
        }

    }

    @Getter
    @Rule
    static class AnotherDummyRule {

        private boolean isExecuted;
        private long timestamp;

        @Condition
        public boolean when(@Fact("bar") final boolean bar) {
            return bar;
        }

        @Action
        public void then(final Facts facts) {
            isExecuted = true;
            timestamp = System.currentTimeMillis();
            facts.remove("bar");
        }

        @Priority
        public int priority() {
            return 2;
        }

    }

}
