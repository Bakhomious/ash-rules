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
package io.github.bakhomious.rules.support.reader;

import io.github.bakhomious.rules.api.Rule;
import io.github.bakhomious.rules.support.RuleDefinition;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Base class for {@link RuleDefinitionReader}s.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public abstract class AbstractRuleDefinitionReader implements RuleDefinitionReader {

    public List<RuleDefinition> read(final Reader reader) throws Exception {
        final List<RuleDefinition> ruleDefinitions = new ArrayList<>();
        final Iterable<Map<String, Object>> rules = loadRules(reader);
        for (final Map<String, Object> rule : rules) {
            ruleDefinitions.add(createRuleDefinition(rule));
        }
        return ruleDefinitions;
    }

    /**
     * Load rules from the given reader as an iterable of Maps.
     *
     * @param reader to read rules from
     * @return an iterable of rule Maps
     * @throws Exception if unable to load rules
     */
    protected abstract Iterable<Map<String, Object>> loadRules(Reader reader) throws Exception;

    /**
     * Create a rule definition.
     *
     * @param map of rule properties
     * @return a rule definition
     */
    @SuppressWarnings("unchecked")
    protected RuleDefinition createRuleDefinition(final Map<String, Object> map) {
        final RuleDefinition ruleDefinition = new RuleDefinition();

        final String name = (String) map.get("name");
        ruleDefinition.setName(name != null ? name : Rule.DEFAULT_NAME);

        final String description = (String) map.get("description");
        ruleDefinition.setDescription(description != null ? description : Rule.DEFAULT_DESCRIPTION);

        final Integer priority = (Integer) map.get("priority");
        ruleDefinition.setPriority(priority != null ? priority : Rule.DEFAULT_PRIORITY);

        final String compositeRuleType = (String) map.get("compositeRuleType");

        final String condition = (String) map.get("condition");
        if (condition == null && compositeRuleType == null) {
            throw new IllegalArgumentException("The rule condition must be specified");
        }
        ruleDefinition.setCondition(condition);

        final List<String> actions = (List<String>) map.get("actions");
        if ((actions == null || actions.isEmpty()) && compositeRuleType == null) {
            throw new IllegalArgumentException("The rule action(s) must be specified");
        }
        ruleDefinition.setActions(actions);

        final List<Object> composingRules = (List<Object>) map.get("composingRules");
        if ((composingRules != null && !composingRules.isEmpty()) && compositeRuleType == null) {
            throw new IllegalArgumentException("Non-composite rules cannot have composing rules");
        } else if ((composingRules == null || composingRules.isEmpty()) && compositeRuleType != null) {
            throw new IllegalArgumentException("Composite rules must have composing rules specified");
        } else if (composingRules != null) {
            final List<RuleDefinition> composingRuleDefinitions = new ArrayList<>();
            for (final Object rule : composingRules) {
                final Map<String, Object> composingRuleMap = (Map<String, Object>) rule;
                composingRuleDefinitions.add(createRuleDefinition(composingRuleMap));
            }
            ruleDefinition.setComposingRules(composingRuleDefinitions);
            ruleDefinition.setCompositeRuleType(compositeRuleType);
        }

        return ruleDefinition;
    }

}
