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


import io.github.bakhomious.rules.api.Rule;
import io.github.bakhomious.rules.api.Rules;
import io.github.bakhomious.rules.support.composite.UnitRuleGroup;
import io.github.bakhomious.rules.support.reader.JsonRuleDefinitionReader;
import io.github.bakhomious.rules.support.reader.YamlRuleDefinitionReader;

import org.assertj.core.api.Assertions;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;


public class SpELRuleFactoryTest {

    static java.util.stream.Stream<Arguments> parameters() {
        return java.util.stream.Stream.of(
            Arguments.of(new SpELRuleFactory(new YamlRuleDefinitionReader()), "yml"),
            Arguments.of(new SpELRuleFactory(new JsonRuleDefinitionReader()), "json")
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRulesCreation(final SpELRuleFactory factory, final String fileExtension) throws Exception {
        // given
        final File rulesDescriptor = new File("src/test/resources/rules." + fileExtension);

        // when
        final Rules rules = factory.createRules(new FileReader(rulesDescriptor));

        // then
        assertThat(rules).hasSize(2);
        final Iterator<Rule> iterator = rules.iterator();

        Rule rule = iterator.next();
        assertThat(rule).isNotNull();
        assertThat(rule.getName()).isEqualTo("adult rule");
        assertThat(rule.getDescription()).isEqualTo("when age is greater than 18, then mark as adult");
        assertThat(rule.getPriority()).isEqualTo(1);

        rule = iterator.next();
        assertThat(rule).isNotNull();
        assertThat(rule.getName()).isEqualTo("weather rule");
        assertThat(rule.getDescription()).isEqualTo("when it rains, then take an umbrella");
        assertThat(rule.getPriority()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRuleCreationFromFileReader(final SpELRuleFactory factory, final String fileExtension)
        throws Exception {
        // given
        final Reader adultRuleDescriptorAsReader = new FileReader("src/test/resources/adult-rule." + fileExtension);

        // when
        final Rule adultRule = factory.createRule(adultRuleDescriptorAsReader);

        // then
        assertThat(adultRule.getName()).isEqualTo("adult rule");
        assertThat(adultRule.getDescription()).isEqualTo("when age is greater than 18, then mark as adult");
        assertThat(adultRule.getPriority()).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRuleCreationFromStringReader(final SpELRuleFactory factory, final String fileExtension)
        throws Exception {
        // given
        final Path ruleDescriptor = Paths.get("src/test/resources/adult-rule." + fileExtension);
        final Reader adultRuleDescriptorAsReader = new StringReader(new String(Files.readAllBytes(ruleDescriptor)));

        // when
        final Rule adultRule = factory.createRule(adultRuleDescriptorAsReader);

        // then
        assertThat(adultRule.getName()).isEqualTo("adult rule");
        assertThat(adultRule.getDescription()).isEqualTo("when age is greater than 18, then mark as adult");
        assertThat(adultRule.getPriority()).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRuleCreationFromFileReader_withCompositeRules(
        final SpELRuleFactory factory,
        final String fileExtension
    )
        throws Exception {
        // given
        final File rulesDescriptor = new File("src/test/resources/composite-rules." + fileExtension);

        // when
        final Rules rules = factory.createRules(new FileReader(rulesDescriptor));

        // then
        assertThat(rules).hasSize(2);
        final Iterator<Rule> iterator = rules.iterator();

        Rule rule = iterator.next();
        assertThat(rule).isNotNull();
        assertThat(rule.getName()).isEqualTo("Movie id rule");
        assertThat(rule.getDescription()).isEqualTo("description");
        assertThat(rule.getPriority()).isEqualTo(1);
        assertThat(rule).isInstanceOf(UnitRuleGroup.class);

        rule = iterator.next();
        assertThat(rule).isNotNull();
        assertThat(rule.getName()).isEqualTo("weather rule");
        assertThat(rule.getDescription()).isEqualTo("when it rains, then take an umbrella");
        assertThat(rule.getPriority()).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRuleCreationFromFileReader_withInvalidCompositeRuleType(
        final SpELRuleFactory factory,
        final String fileExtension
    ) {
        // given
        final File rulesDescriptor = new File("src/test/resources/composite-rule-invalid-composite-rule-type." + fileExtension);

        // when
        Assertions.assertThatThrownBy(() -> factory.createRule(new FileReader(rulesDescriptor)))
            // then
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "Invalid composite rule type, must be one of [UnitRuleGroup, ConditionalRuleGroup, ActivationRuleGroup]");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRuleCreationFromFileReader_withEmptyComposingRules(
        final SpELRuleFactory factory,
        final String fileExtension
    ) {
        // given
        final File rulesDescriptor = new File("src/test/resources/composite-rule-invalid-empty-composing-rules." + fileExtension);

        // when
        Assertions.assertThatThrownBy(() -> factory.createRule(new FileReader(rulesDescriptor)))
            // then
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Composite rules must have composing rules specified");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testRuleCreationFromFileReader_withNonCompositeRuleDeclaresComposingRules(
        final SpELRuleFactory factory,
        final String fileExtension
    ) {
        // given
        final File rulesDescriptor = new File("src/test/resources/non-composite-rule-with-composing-rules." + fileExtension);

        // when
        Assertions.assertThatThrownBy(() -> factory.createRule(new FileReader(rulesDescriptor)))
            // then
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Non-composite rules cannot have composing rules");
    }

}
