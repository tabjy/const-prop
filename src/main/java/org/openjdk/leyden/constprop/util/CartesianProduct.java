package org.openjdk.leyden.constprop.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CartesianProduct {

    /**
     * Computes the cartesian product of the given lists. Each list must not be null and must only contain distinct
     * elements. One <code>null</code> element is allowed for each list.
     *
     * @param lists non-nullable lists of nullable elements
     * @return the cartesian products
     */
    @SafeVarargs
    public static <T> List<List<T>> of(List<T>... lists) {
        Objects.requireNonNull(lists);

        List<List<T>> result = new ArrayList<>();
        if (lists.length == 0) {
            return List.of();
        }

        List<T> currentList = lists[0];
        List<T>[] remainingLists = Arrays.copyOfRange(lists, 1, lists.length);
        List<List<T>> remainingProducts = of(remainingLists);

        if (remainingProducts.isEmpty()) {
            result.addAll(currentList.stream().map(List::of).toList());
        }

        for (List<T> remainingProduct : remainingProducts) {
            for (T head : currentList) {
                List<T> newProduct = new ArrayList<>();
                newProduct.add(head);
                newProduct.addAll(remainingProduct);
                result.add(newProduct);
            }
        }



        return result;
    }
}