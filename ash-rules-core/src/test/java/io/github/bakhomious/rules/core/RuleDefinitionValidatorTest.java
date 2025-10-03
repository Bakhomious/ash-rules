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


import io.github.bakhomious.rules.annotation.AnnotatedRuleWithActionMethodHavingMoreThanOneArgumentOfTypeFacts;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithActionMethodHavingOneArgumentNotOfTypeFacts;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithActionMethodHavingOneArgumentOfTypeFacts;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithActionMethodThatReturnsNonVoidType;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithConditionMethodHavingNonBooleanReturnType;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithConditionMethodHavingOneArgumentNotOfTypeFacts;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithMetaRuleAnnotation;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithMoreThanOnePriorityMethod;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithMultipleAnnotatedParametersAndOneParameterOfSubTypeFacts;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithMultipleAnnotatedParametersAndOneParameterOfTypeFacts;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithNonPublicActionMethod;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithNonPublicConditionMethod;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithNonPublicPriorityMethod;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithOneParameterNotAnnotatedWithFactAndNotOfTypeFacts;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithPriorityMethodHavingArguments;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithPriorityMethodHavingNonIntegerReturnType;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithoutActionMethod;
import io.github.bakhomious.rules.annotation.AnnotatedRuleWithoutConditionMethod;

import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


public class RuleDefinitionValidatorTest {

    private RuleDefinitionValidator ruleDefinitionValidator;

    @BeforeEach
    public void setup() {
        ruleDefinitionValidator = new RuleDefinitionValidator();
    }

    /*
     * Rule annotation test
     */
    @Test
    public void notAnnotatedRuleMustNotBeAccepted() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new Object()));
    }

    @Test
    public void withCustomAnnotationThatIsItselfAnnotatedWithTheRuleAnnotation() {
        ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithMetaRuleAnnotation());
    }

    /*
     * Conditions methods tests
     */
    @Test
    public void conditionMethodMustBeDefined() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithoutConditionMethod()));
    }

    @Test
    public void conditionMethodMustBePublic() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithNonPublicConditionMethod()));
    }

    @Test
    public void whenConditionMethodHasOneNonAnnotatedParameter_thenThisParameterMustBeOfTypeFacts() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithConditionMethodHavingOneArgumentNotOfTypeFacts()));
    }

    @Test
    public void conditionMethodMustReturnBooleanType() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithConditionMethodHavingNonBooleanReturnType()));
    }

    @Test
    public void conditionMethodParametersShouldAllBeAnnotatedWithFactUnlessExactlyOneOfThemIsOfTypeFacts() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithOneParameterNotAnnotatedWithFactAndNotOfTypeFacts()));
    }

    /*
     * Action method tests
     */
    @Test
    public void actionMethodMustBeDefined() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithoutActionMethod()));
    }

    @Test
    public void actionMethodMustBePublic() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithNonPublicActionMethod()));
    }

    @Test
    public void actionMethodMustHaveAtMostOneArgumentOfTypeFacts() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithActionMethodHavingOneArgumentNotOfTypeFacts()));
    }

    @Test
    public void actionMethodMustHaveExactlyOneArgumentOfTypeFactsIfAny() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithActionMethodHavingMoreThanOneArgumentOfTypeFacts()));
    }

    @Test
    public void actionMethodMustReturnVoid() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithActionMethodThatReturnsNonVoidType()));
    }

    @Test
    public void actionMethodParametersShouldAllBeAnnotatedWithFactUnlessExactlyOneOfThemIsOfTypeFacts() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithOneParameterNotAnnotatedWithFactAndNotOfTypeFacts()));
    }

    /*
     * Priority method tests
     */

    @Test
    public void priorityMethodMustBeUnique() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithMoreThanOnePriorityMethod()));
    }

    @Test
    public void priorityMethodMustBePublic() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithNonPublicPriorityMethod()));
    }

    @Test
    public void priorityMethodMustHaveNoArguments() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithPriorityMethodHavingArguments()));
    }

    @Test
    public void priorityMethodReturnTypeMustBeInteger() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithPriorityMethodHavingNonIntegerReturnType()));
    }

    /*
     * Valid definition tests
     */
    @Test
    public void validAnnotationsShouldBeAccepted() {
        try {
            ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithMultipleAnnotatedParametersAndOneParameterOfTypeFacts());
            ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithMultipleAnnotatedParametersAndOneParameterOfSubTypeFacts());
            ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithActionMethodHavingOneArgumentOfTypeFacts());
        } catch (final Throwable throwable) {
            Assertions.fail("Should not throw exception for valid rule definitions");
        }
    }

}
