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

import io.github.bakhomious.rules.api.Fact;
import io.github.bakhomious.rules.api.Facts;
import io.github.bakhomious.rules.api.Rule;
import io.github.bakhomious.rules.api.Rules;
import io.github.bakhomious.rules.api.RulesEngine;
import io.github.bakhomious.rules.api.RulesEngineParameters;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Default {@link RulesEngine} implementation.
 * <p>
 * Rules are fired according to their natural order which is priority by default.
 * This implementation iterates over the sorted set of rules, evaluates the condition
 * of each rule and executes its actions if the condition evaluates to true.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com) from Easy Rules
 */
@Slf4j
public final class DefaultRulesEngine extends AbstractRulesEngine {

    /**
     * Create a new {@link DefaultRulesEngine} with default parameters.
     */
    public DefaultRulesEngine() {
        super();
    }

    /**
     * Create a new {@link DefaultRulesEngine}.
     *
     * @param parameters of the engine
     */
    public DefaultRulesEngine(final RulesEngineParameters parameters) {
        super(parameters);
    }

    @Override
    public void fire(final Rules rules, final Facts facts) {
        Objects.requireNonNull(rules, "Rules must not be null");
        Objects.requireNonNull(facts, "Facts must not be null");
        triggerListenersBeforeRules(rules, facts);
        doFire(rules, facts);
        triggerListenersAfterRules(rules, facts);
    }

    private void triggerListenersBeforeRules(final Rules rule, final Facts facts) {
        rulesEngineListeners.forEach(
            rulesEngineListener -> rulesEngineListener.beforeEvaluate(rule, facts)
        );
    }

    void doFire(final Rules rules, final Facts facts) {
        if (rules.isEmpty()) {
            log.warn("No rules registered! Nothing to apply");
            return;
        }
        logEngineParameters();
        log(rules);
        log(facts);
        log.debug("Rules evaluation started");
        for (final Rule rule : rules) {
            final String name = rule.getName();
            final int priority = rule.getPriority();
            if (priority > parameters.getPriorityThreshold()) {
                log.debug(
                    "Rule priority threshold ({}) exceeded at rule '{}' with priority={}, next rules will be skipped",
                    parameters.getPriorityThreshold(), name, priority
                );
                break;
            }
            if (!shouldBeEvaluated(rule, facts)) {
                log.debug("Rule '{}' has been skipped before being evaluated", name);
                continue;
            }
            boolean evaluationResult = false;
            try {
                evaluationResult = rule.evaluate(facts);
            } catch (final RuntimeException exception) {
                log.error("Rule '{}' evaluated with error", name, exception);
                triggerListenersOnEvaluationError(rule, facts, exception);
                // give the option to either skip next rules on evaluation error or continue by considering the evaluation error as false
                if (parameters.isSkipOnFirstNonTriggeredRule()) {
                    log.debug("Next rules will be skipped since parameter skipOnFirstNonTriggeredRule is set");
                    break;
                }
            }
            if (evaluationResult) {
                log.debug("Rule '{}' triggered", name);
                triggerListenersAfterEvaluate(rule, facts, true);
                try {
                    triggerListenersBeforeExecute(rule, facts);
                    rule.execute(facts);
                    log.debug("Rule '{}' performed successfully", name);
                    triggerListenersOnSuccess(rule, facts);
                    if (parameters.isSkipOnFirstAppliedRule()) {
                        log.debug("Next rules will be skipped since parameter skipOnFirstAppliedRule is set");
                        break;
                    }
                } catch (final Exception exception) {
                    log.error("Rule '{}' performed with error", name, exception);
                    triggerListenersOnFailure(rule, exception, facts);
                    if (parameters.isSkipOnFirstFailedRule()) {
                        log.debug("Next rules will be skipped since parameter skipOnFirstFailedRule is set");
                        break;
                    }
                }
            } else {
                log.debug("Rule '{}' has been evaluated to false, it has not been executed", name);
                triggerListenersAfterEvaluate(rule, facts, false);
                if (parameters.isSkipOnFirstNonTriggeredRule()) {
                    log.debug("Next rules will be skipped since parameter skipOnFirstNonTriggeredRule is set");
                    break;
                }
            }
        }
    }

    private void triggerListenersAfterRules(final Rules rule, final Facts facts) {
        rulesEngineListeners.forEach(rulesEngineListener -> rulesEngineListener.afterExecute(rule, facts));
    }

    private void logEngineParameters() {
        log.debug("{}", parameters);
    }

    private void log(final Rules rules) {
        log.debug("Registered rules:");
        for (final Rule rule : rules) {
            log.debug(
                "Rule { name = '{}', description = '{}', priority = '{}'}",
                rule.getName(), rule.getDescription(), rule.getPriority()
            );
        }
    }

    private void log(final Facts facts) {
        log.debug("Known facts:");
        for (final Fact<?> fact : facts) {
            log.debug("{}", fact);
        }
    }

    private boolean shouldBeEvaluated(final Rule rule, final Facts facts) {
        return triggerListenersBeforeEvaluate(rule, facts);
    }

    private void triggerListenersOnEvaluationError(final Rule rule, final Facts facts, final Exception exception) {
        ruleListeners.forEach(ruleListener -> ruleListener.onEvaluationError(rule, facts, exception));
    }

    private void triggerListenersAfterEvaluate(final Rule rule, final Facts facts, final boolean evaluationResult) {
        ruleListeners.forEach(ruleListener -> ruleListener.afterEvaluate(rule, facts, evaluationResult));
    }

    private void triggerListenersBeforeExecute(final Rule rule, final Facts facts) {
        ruleListeners.forEach(ruleListener -> ruleListener.beforeExecute(rule, facts));
    }

    private void triggerListenersOnSuccess(final Rule rule, final Facts facts) {
        ruleListeners.forEach(ruleListener -> ruleListener.onSuccess(rule, facts));
    }

    private void triggerListenersOnFailure(final Rule rule, final Exception exception, final Facts facts) {
        ruleListeners.forEach(ruleListener -> ruleListener.onFailure(rule, facts, exception));
    }

    private boolean triggerListenersBeforeEvaluate(final Rule rule, final Facts facts) {
        return ruleListeners.stream().allMatch(ruleListener -> ruleListener.beforeEvaluate(rule, facts));
    }

    @Override
    public Map<Rule, Boolean> check(final Rules rules, final Facts facts) {
        Objects.requireNonNull(rules, "Rules must not be null");
        Objects.requireNonNull(facts, "Facts must not be null");
        triggerListenersBeforeRules(rules, facts);
        final Map<Rule, Boolean> result = doCheck(rules, facts);
        triggerListenersAfterRules(rules, facts);
        return result;
    }

    private Map<Rule, Boolean> doCheck(final Rules rules, final Facts facts) {
        log.debug("Checking rules");
        final Map<Rule, Boolean> result = new HashMap<>();
        for (final Rule rule : rules) {
            if (shouldBeEvaluated(rule, facts)) {
                result.put(rule, rule.evaluate(facts));
            }
        }
        return result;
    }

}
