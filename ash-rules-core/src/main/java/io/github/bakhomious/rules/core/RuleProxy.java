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
import io.github.bakhomious.rules.api.Facts;
import io.github.bakhomious.rules.api.Rule;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.String.format;


/**
 * Main class to create rule proxies from annotated objects.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com) from Easy Rules
 */
@Slf4j
public class RuleProxy implements InvocationHandler {

    private static final RuleDefinitionValidator ruleDefinitionValidator = new RuleDefinitionValidator();

    @Getter
    private final Object target;

    private String name;

    private String description;

    private Integer priority;

    private Method[] methods;

    private Method conditionMethod;

    private Set<ActionMethodOrderBean> actionMethods;

    private Method compareToMethod;

    private Method toStringMethod;

    private io.github.bakhomious.rules.annotation.Rule annotation;

    private RuleProxy(final Object target) {
        this.target = target;
    }

    /**
     * Makes the rule object implement the {@link Rule} interface.
     *
     * @param rule the annotated rule object.
     * @return a proxy that implements the {@link Rule} interface.
     */
    public static Rule asRule(final Object rule) {
        final Rule result;
        if (rule instanceof Rule) {
            result = (Rule) rule;
        } else {
            ruleDefinitionValidator.validateRuleDefinition(rule);
            result = (Rule) Proxy.newProxyInstance(
                Rule.class.getClassLoader(),
                new Class[] {Rule.class, Comparable.class},
                new RuleProxy(rule)
            );
        }
        return result;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String methodName = method.getName();
        return switch (methodName) {
            case "getName" -> getRuleName();
            case "getDescription" -> getRuleDescription();
            case "getPriority" -> getRulePriority();
            case "compareTo" -> compareToMethod(args);
            case "evaluate" -> evaluateMethod(args);
            case "execute" -> executeMethod(args);
            case "equals" -> equalsMethod(args);
            case "hashCode" -> hashCodeMethod();
            case "toString" -> toStringMethod();
            default -> null;
        };
    }

    private Object evaluateMethod(final Object[] args) throws IllegalAccessException, InvocationTargetException {
        final Facts facts = (Facts) args[0];
        final Method conditionMethod = getConditionMethod();
        try {
            final List<Object> actualParameters = getActualParameters(conditionMethod, facts);
            return conditionMethod.invoke(target, actualParameters.toArray()); // validated upfront
        } catch (final NoSuchFactException e) {
            log.warn(
                "Rule '{}' has been evaluated to false due to a declared but missing fact '{}' in {}",
                getTargetClass().getName(), e.getMissingFact(), facts
            );
            return false;
        } catch (final IllegalArgumentException e) {
            log.warn(
                "Types of injected facts in method '{}' in rule '{}' do not match parameters types",
                conditionMethod.getName(), getTargetClass().getName(), e
            );
            return false;
        }
    }

    private Object executeMethod(final Object[] args) throws IllegalAccessException, InvocationTargetException {
        final Facts facts = (Facts) args[0];
        for (final ActionMethodOrderBean actionMethodBean : getActionMethodBeans()) {
            final Method actionMethod = actionMethodBean.method();
            final List<Object> actualParameters = getActualParameters(actionMethod, facts);
            actionMethod.invoke(target, actualParameters.toArray());
        }
        return null;
    }

    private Object compareToMethod(final Object[] args) throws Exception {
        final Method compareToMethod = getCompareToMethod();
        final Object otherRule = args[0]; // validated upfront
        if (compareToMethod != null && Proxy.isProxyClass(otherRule.getClass())) {
            if (compareToMethod.getParameters().length != 1) {
                throw new IllegalArgumentException("compareTo method must have a single argument");
            }
            final RuleProxy ruleProxy = (RuleProxy) Proxy.getInvocationHandler(otherRule);
            return compareToMethod.invoke(target, ruleProxy.getTarget());
        } else {
            return compareTo((Rule) otherRule);
        }
    }

    private List<Object> getActualParameters(final Method method, final Facts facts) {
        final List<Object> actualParameters = new ArrayList<>();
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (final Annotation[] annotations : parameterAnnotations) {
            if (annotations.length == 1) {
                final String factName = ((Fact) (annotations[0])).value(); //validated upfront.
                final Object fact = facts.get(factName);
                if (fact == null && !facts.asMap().containsKey(factName)) {
                    throw new NoSuchFactException(
                        format(
                            "No fact named '%s' found in known facts: %n%s",
                            factName,
                            facts
                        ), factName
                    );
                }
                actualParameters.add(fact);
            } else {
                actualParameters.add(facts); //validated upfront, there may be only one parameter not annotated and which is of type Facts.class
            }
        }
        return actualParameters;
    }

    private boolean equalsMethod(final Object[] args) throws Exception {
        if (!(args[0] instanceof final Rule otherRule)) {
            return false;
        }
        final int otherPriority = otherRule.getPriority();
        final int priority = getRulePriority();
        if (priority != otherPriority) {
            return false;
        }
        final String otherName = otherRule.getName();
        final String name = getRuleName();
        if (!name.equals(otherName)) {
            return false;
        }
        final String otherDescription = otherRule.getDescription();
        final String description = getRuleDescription();
        return Objects.equals(description, otherDescription);
    }

    private int hashCodeMethod() throws Exception {
        int result = getRuleName().hashCode();
        final int priority = getRulePriority();
        final String description = getRuleDescription();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + priority;
        return result;
    }

    private Method getToStringMethod() {
        if (this.toStringMethod == null) {
            final Method[] methods = getMethods();
            for (final Method method : methods) {
                if ("toString".equals(method.getName())) {
                    this.toStringMethod = method;
                    return this.toStringMethod;
                }
            }
        }
        return this.toStringMethod;
    }

    private String toStringMethod() throws Exception {
        final Method toStringMethod = getToStringMethod();
        if (toStringMethod != null) {
            return (String) toStringMethod.invoke(target);
        } else {
            return getRuleName();
        }
    }

    private int compareTo(final Rule otherRule) throws Exception {
        final int otherPriority = otherRule.getPriority();
        final int priority = getRulePriority();
        if (priority < otherPriority) {
            return -1;
        } else if (priority > otherPriority) {
            return 1;
        } else {
            final String otherName = otherRule.getName();
            final String name = getRuleName();
            return name.compareTo(otherName);
        }
    }

    private int getRulePriority() throws Exception {
        if (this.priority == null) {
            int priority = Rule.DEFAULT_PRIORITY;

            final io.github.bakhomious.rules.annotation.Rule rule = getRuleAnnotation();
            if (rule.priority() != Rule.DEFAULT_PRIORITY) {
                priority = rule.priority();
            }

            final Method[] methods = getMethods();
            for (final Method method : methods) {
                if (method.isAnnotationPresent(Priority.class)) {
                    priority = (int) method.invoke(target);
                    break;
                }
            }
            this.priority = priority;
        }
        return this.priority;
    }

    private Method getConditionMethod() {
        if (this.conditionMethod == null) {
            final Method[] methods = getMethods();
            for (final Method method : methods) {
                if (method.isAnnotationPresent(Condition.class)) {
                    this.conditionMethod = method;
                    return this.conditionMethod;
                }
            }
        }
        return this.conditionMethod;
    }

    private Set<ActionMethodOrderBean> getActionMethodBeans() {
        if (this.actionMethods == null) {
            this.actionMethods = new TreeSet<>();
            final Method[] methods = getMethods();
            for (final Method method : methods) {
                if (method.isAnnotationPresent(Action.class)) {
                    final Action actionAnnotation = method.getAnnotation(Action.class);
                    final int order = actionAnnotation.order();
                    this.actionMethods.add(new ActionMethodOrderBean(method, order));
                }
            }
        }
        return this.actionMethods;
    }

    private Method getCompareToMethod() {
        if (this.compareToMethod == null) {
            final Method[] methods = getMethods();
            for (final Method method : methods) {
                if (method.getName().equals("compareTo")) {
                    this.compareToMethod = method;
                    return this.compareToMethod;
                }
            }
        }
        return this.compareToMethod;
    }

    private Method[] getMethods() {
        if (this.methods == null) {
            this.methods = getTargetClass().getMethods();
        }
        return this.methods;
    }

    private io.github.bakhomious.rules.annotation.Rule getRuleAnnotation() {
        if (this.annotation == null) {
            this.annotation = Utils.findAnnotation(io.github.bakhomious.rules.annotation.Rule.class, getTargetClass());
        }
        return this.annotation;
    }

    private String getRuleName() {
        if (this.name == null) {
            final io.github.bakhomious.rules.annotation.Rule rule = getRuleAnnotation();
            this.name = rule.name().equals(Rule.DEFAULT_NAME) ? getTargetClass().getSimpleName() : rule.name();
        }
        return this.name;
    }

    private String getRuleDescription() {
        if (this.description == null) {
            // Default description = "when " + conditionMethodName + " then " + comma separated actionMethodsNames
            final StringBuilder description = new StringBuilder();
            appendConditionMethodName(description);
            appendActionMethodsNames(description);
            final io.github.bakhomious.rules.annotation.Rule rule = getRuleAnnotation();
            this.description = rule.description().equals(Rule.DEFAULT_DESCRIPTION)
                ? description.toString()
                : rule.description();
        }
        return this.description;
    }

    private void appendConditionMethodName(final StringBuilder description) {
        final Method method = getConditionMethod();
        if (method != null) {
            description.append("when ");
            description.append(method.getName());
            description.append(" then ");
        }
    }

    private void appendActionMethodsNames(final StringBuilder description) {
        final Iterator<ActionMethodOrderBean> iterator = getActionMethodBeans().iterator();
        while (iterator.hasNext()) {
            description.append(iterator.next().method().getName());
            if (iterator.hasNext()) {
                description.append(",");
            }
        }
    }

    private Class<?> getTargetClass() {
        return target.getClass();
    }

}
