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


import io.github.bakhomious.rules.api.Fact;
import io.github.bakhomious.rules.api.Facts;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class FactsTest {

    private final Facts facts = new Facts();

    @Test
    public void factsMustHaveUniqueName() {
        facts.add(new Fact<>("foo", 1));
        facts.add(new Fact<>("foo", 2));

        assertThat(facts).hasSize(1);
        final Fact<?> fact = facts.getFact("foo");
        assertThat(fact.value()).isEqualTo(2);
    }

    @Test
    public void testAdd() {
        final Fact<Integer> fact1 = new Fact<>("foo", 1);
        final Fact<Integer> fact2 = new Fact<>("bar", 2);
        facts.add(fact1);
        facts.add(fact2);

        assertThat(facts).contains(fact1);
        assertThat(facts).contains(fact2);
    }

    @Test
    public void testPut() {
        facts.put("foo", 1);
        facts.put("bar", 2);

        assertThat(facts).contains(new Fact<>("foo", 1));
        assertThat(facts).contains(new Fact<>("bar", 2));
    }

    @Test
    public void testRemove() {
        final Fact<Integer> foo = new Fact<>("foo", 1);
        facts.add(foo);
        facts.remove(foo);

        assertThat(facts).isEmpty();
    }

    @Test
    public void testRemoveByName() {
        final Fact<Integer> foo = new Fact<>("foo", 1);
        facts.add(foo);
        facts.remove("foo");

        assertThat(facts).isEmpty();
    }

    @Test
    public void testGet() {
        final Fact<Integer> fact = new Fact<>("foo", 1);
        facts.add(fact);
        final Integer value = facts.get("foo");
        assertThat(value).isEqualTo(1);
    }

    @Test
    public void testGetFact() {
        final Fact<Integer> fact = new Fact<>("foo", 1);
        facts.add(fact);
        final Fact<?> retrievedFact = facts.getFact("foo");
        assertThat(retrievedFact).isEqualTo(fact);
    }

    @Test
    public void testAsMap() {
        final Fact<Integer> fact1 = new Fact<>("foo", 1);
        final Fact<Integer> fact2 = new Fact<>("bar", 2);
        facts.add(fact1);
        facts.add(fact2);
        final Map<String, Object> map = facts.asMap();
        assertThat(map).containsKeys("foo", "bar");
        assertThat(map).containsValues(1, 2);
    }

    @Test
    public void testClear() {
        final Facts facts = new Facts();
        facts.add(new Fact<>("foo", 1));
        facts.clear();
        assertThat(facts).isEmpty();
    }

}
