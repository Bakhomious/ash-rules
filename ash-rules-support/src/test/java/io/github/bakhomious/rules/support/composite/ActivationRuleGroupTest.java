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
package io.github.bakhomious.rules.support.composite;

import io.github.bakhomious.rules.annotation.Action;
import io.github.bakhomious.rules.annotation.Condition;
import io.github.bakhomious.rules.api.Facts;
import io.github.bakhomious.rules.api.Rules;
import io.github.bakhomious.rules.core.DefaultRulesEngine;

import lombok.Getter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class ActivationRuleGroupTest {

    private final Facts facts = new Facts();
    private final Rules rules = new Rules();

    private final DefaultRulesEngine rulesEngine = new DefaultRulesEngine();

    @Test
    public void onlySelectedRuleShouldBeExecuted_whenComposingRulesHaveDifferentPriorities() {
        // given
        final Rule1 rule1 = new Rule1();
        final Rule2 rule2 = new Rule2();
        final ActivationRuleGroup activationRuleGroup = new ActivationRuleGroup(
            "my activation rule",
            "rule1 xor rule2"
        );
        activationRuleGroup.addRule(rule1);
        activationRuleGroup.addRule(rule2);
        rules.register(activationRuleGroup);

        // when
        rulesEngine.fire(rules, facts);

        // then
        assertThat(rule1.isExecuted()).isTrue();
        assertThat(rule2.isExecuted()).isFalse();
    }

    @Test
    public void onlySelectedRuleShouldBeExecuted_whenComposingRulesHaveSamePriority() {
        // given
        final Rule2 rule2 = new Rule2();
        final Rule3 rule3 = new Rule3();
        final ActivationRuleGroup activationRuleGroup = new ActivationRuleGroup(
            "my activation rule",
            "rule2 xor rule3"
        );
        activationRuleGroup.addRule(rule2);
        activationRuleGroup.addRule(rule3);
        rules.register(activationRuleGroup);

        // when
        rulesEngine.fire(rules, facts);

        // then
        // we don't know upfront which rule will be selected, but only one of them should be executed
        if (rule2.isExecuted()) {
            assertThat(rule3.isExecuted()).isFalse();
        } else {
            assertThat(rule3.isExecuted()).isTrue();
        }
    }

    @Test
    public void whenNoSelectedRule_thenNothingShouldHappen() {
        // given
        final Rule4 rule4 = new Rule4();
        final ActivationRuleGroup activationRuleGroup = new ActivationRuleGroup("my activation rule", "rule4");
        activationRuleGroup.addRule(rule4);

        //when
        rules.register(activationRuleGroup);

        //then
        rulesEngine.fire(rules, facts);

        // rule4 will not be selected, so it should not be executed
        assertThat(rule4.isExecuted()).isFalse();
    }

    @Getter
    @io.github.bakhomious.rules.annotation.Rule(priority = 1)
    public static class Rule1 {

        private boolean executed;

        @Condition
        public boolean when() {
            return true;
        }

        @Action
        public void then() {
            executed = true;
        }

    }

    @Getter
    @io.github.bakhomious.rules.annotation.Rule(priority = 2)
    public static class Rule2 {

        private boolean executed;

        @Condition
        public boolean when() {
            return true;
        }

        @Action
        public void then() {
            executed = true;
        }

    }

    @Getter
    @io.github.bakhomious.rules.annotation.Rule(priority = 2)
    public static class Rule3 {

        private boolean executed;

        @Condition
        public boolean when() {
            return true;
        }

        @Action
        public void then() {
            executed = true;
        }

    }

    @Getter
    @io.github.bakhomious.rules.annotation.Rule(priority = 1)
    public static class Rule4 {

        private boolean executed;

        @Condition
        public boolean when() {
            return false;
        }

        @Action
        public void then() {
            executed = true;
        }

    }

}
