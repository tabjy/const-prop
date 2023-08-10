package org.openjdk.leyden.constprop.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openjdk.leyden.constprop.util.CartesianProduct;

public class CartesianProductTest {

    @Test
    void testCartesianProductWithNoInput() {
        List<List<Object>> actual = CartesianProduct.of();
        assertTrue(actual.isEmpty());
    }

    @Test
    void testCartesianProductWithOneList() {
        List<Object> list = Arrays.asList(1, 2, 3);
        List<List<Object>> actual = CartesianProduct.of(list);
        assertEquals(list.size(), actual.size());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(Collections.singletonList(list.get(i)), actual.get(i));
        }
    }

    @Test
    void testCartesianProductOfTwoLists() {
        List<Object> list1 = Arrays.asList(1, 2);
        List<Object> list2 = Arrays.asList("A", "B");

        List<List<Object>> actual = CartesianProduct.of(list1, list2);

        assertEquals(4, actual.size());
        assertTrue(actual.contains(Arrays.asList(1, "A")));
        assertTrue(actual.contains(Arrays.asList(2, "A")));
        assertTrue(actual.contains(Arrays.asList(1, "B")));
        assertTrue(actual.contains(Arrays.asList(2, "B")));
    }

    @Test
    void testCartesianProductOfThreeLists() {
        List<Object> list1 = Arrays.asList(1, 2);
        List<Object> list2 = Arrays.asList("A", "B");
        List<Object> list3 = Arrays.asList('x', 'y');

        List<List<Object>> actual = CartesianProduct.of(list1, list2, list3);

        assertEquals(8, actual.size());
        assertTrue(actual.contains(Arrays.asList(1, "A", 'x')));
        assertTrue(actual.contains(Arrays.asList(1, "A", 'y')));
        assertTrue(actual.contains(Arrays.asList(1, "B", 'x')));
        assertTrue(actual.contains(Arrays.asList(1, "B", 'y')));
        assertTrue(actual.contains(Arrays.asList(2, "A", 'x')));
        assertTrue(actual.contains(Arrays.asList(2, "A", 'y')));
        assertTrue(actual.contains(Arrays.asList(2, "B", 'x')));
        assertTrue(actual.contains(Arrays.asList(2, "B", 'y')));
    }
}