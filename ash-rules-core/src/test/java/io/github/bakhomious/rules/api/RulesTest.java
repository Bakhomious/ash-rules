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
package io.github.bakhomious.rules.api;

import io.github.bakhomious.rules.annotation.Action;
import io.github.bakhomious.rules.annotation.Condition;
import io.github.bakhomious.rules.api.Rules;
import io.github.bakhomious.rules.core.BasicRule;
import io.github.bakhomious.rules.annotation.Rule;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;


public class RulesTest {

    private Rules rules = new Rules();

    @Test
    public void register() {
        rules.register(new DummyRule());

        assertThat(rules).hasSize(1);
    }

    @Test
    public void rulesMustHaveUniqueName() {
        final io.github.bakhomious.rules.api.Rule r1 = new BasicRule("rule");
        final io.github.bakhomious.rules.api.Rule r2 = new BasicRule("rule");
        final Set<io.github.bakhomious.rules.api.Rule> ruleSet = new HashSet<>();
        ruleSet.add(r1);
        ruleSet.add(r2);

        rules = new Rules(ruleSet);

        assertThat(rules).hasSize(1);
    }

    @Test
    public void unregister() {
        final DummyRule rule = new DummyRule();
        rules.register(rule);
        rules.unregister(rule);

        assertThat(rules).isEmpty();
    }

    @Test
    public void unregisterByName() {
        final io.github.bakhomious.rules.api.Rule r1 = new BasicRule("rule1");
        final io.github.bakhomious.rules.api.Rule r2 = new BasicRule("rule2");
        final Set<io.github.bakhomious.rules.api.Rule> ruleSet = new HashSet<>();
        ruleSet.add(r1);
        ruleSet.add(r2);

        rules = new Rules(ruleSet);
        rules.unregister("rule2");

        assertThat(rules).hasSize(1).containsExactly(r1);
    }

    @Test
    public void unregisterByNameNonExistingRule() {
        final io.github.bakhomious.rules.api.Rule r1 = new BasicRule("rule1");
        final Set<io.github.bakhomious.rules.api.Rule> ruleSet = new HashSet<>();
        ruleSet.add(r1);

        rules = new Rules(ruleSet);
        rules.unregister("rule2");

        assertThat(rules).hasSize(1).containsExactly(r1);
    }

    @Test
    public void isEmpty() {
        assertThat(rules.isEmpty()).isTrue();
    }

    @Test
    public void clear() {
        rules.register(new DummyRule());
        rules.clear();

        assertThat(rules).isEmpty();
    }

    @Test
    public void sort() {
        final io.github.bakhomious.rules.api.Rule r1 = new BasicRule("rule", "", 1);
        final io.github.bakhomious.rules.api.Rule r2 = new BasicRule("rule", "", Integer.MAX_VALUE);
        final DummyRule r3 = new DummyRule();

        rules.register(r3);
        rules.register(r1);
        rules.register(r2);

        assertThat(rules).startsWith(r1).endsWith(r2);
    }

    @Test
    public void size() {
        assertThat(rules.size()).isEqualTo(0);

        rules.register(new DummyRule());
        assertThat(rules.size()).isEqualTo(1);

        rules.unregister(new DummyRule());
        assertThat(rules.size()).isEqualTo(0);
    }

    @Test
    public void register_multiple() {
        rules.register(new BasicRule("ruleA"), new BasicRule("ruleB"));
        assertThat(rules.size()).isEqualTo(2);
    }

    @Test
    public void unregister_noneLeft() {
        rules.register(new BasicRule("ruleA"), new BasicRule("ruleB"));
        assertThat(rules.size()).isEqualTo(2);

        rules.unregister(new BasicRule("ruleA"), new BasicRule("ruleB"));
        assertThat(rules.size()).isEqualTo(0);
    }

    @Test
    public void unregister_oneLeft() {
        rules.register(new BasicRule("ruleA"), new BasicRule("ruleB"));
        assertThat(rules.size()).isEqualTo(2);

        rules.unregister(new BasicRule("ruleA"));
        assertThat(rules.size()).isEqualTo(1);
    }

    @Test
    public void whenRegisterNullRule_thenShouldThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> rules.register(null));
    }

    @Rule
    static class DummyRule {

        @Condition
        public boolean when() {
            return true;
        }

        @Action
        public void then() {
        }

    }

}
