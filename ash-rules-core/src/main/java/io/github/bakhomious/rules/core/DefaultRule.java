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


import io.github.bakhomious.rules.api.Action;
import io.github.bakhomious.rules.api.Condition;
import io.github.bakhomious.rules.api.Facts;

import java.util.List;


class DefaultRule extends BasicRule {

    private final Condition condition;
    private final List<Action> actions;

    DefaultRule(
        final String name,
        final String description,
        final int priority,
        final Condition condition,
        final List<Action> actions
    ) {
        super(name, description, priority);
        this.condition = condition;
        this.actions = actions;
    }

    @Override
    public boolean evaluate(final Facts facts) {
        return condition.evaluate(facts);
    }

    @Override
    public void execute(final Facts facts) throws Exception {
        for (final Action action : actions) {
            action.execute(facts);
        }
    }

}
