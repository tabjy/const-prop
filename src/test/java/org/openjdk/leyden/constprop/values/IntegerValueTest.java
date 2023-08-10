package org.openjdk.leyden.constprop.values;

import org.junit.jupiter.api.Test;
import org.openjdk.leyden.constprop.operations.BinaryOperation;
import org.openjdk.leyden.constprop.operations.NewOperation;
import org.openjdk.leyden.constprop.operations.UnaryOperation;
import org.openjdk.leyden.constprop.util.Unknowable;
import org.openjdk.leyden.constprop.values.ConstantizationValue;
import org.openjdk.leyden.constprop.values.IntegerValue;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegerValueTest {

    @SafeVarargs
    private <T> void assertAllPossibleValues(ConstantizationValue<T> value, T... values) {
        List<T> list = StreamSupport.stream(value.spliterator(), false).toList();
        assertEquals(values.length, list.size());
        for (T v : values) {
            assertTrue(list.contains(v));
        }
    }

    @Test
    void testCreatingSimpleIntegerValue() {
        IntegerValue integerValue = new IntegerValue(42);

        assertAllPossibleValues(integerValue, 42);
    }

    @Test
    void testCreatingSingleIntegerValue() {
        IntegerValue integerValue = new IntegerValue(new NewOperation<>() {
            @Override
            public Unknowable<Integer> apply() {
                return Unknowable.of(42);
            }
        });

        assertAllPossibleValues(integerValue, 42);
    }

    @Test
    void testCreatingTwoIntegerValue() {
        IntegerValue integerValue = new IntegerValue(new NewOperation<>() {
            @Override
            public Iterator<Integer> applyAll() {
                return Stream.of(99, 1).iterator();
            }
        });

        assertAllPossibleValues(integerValue, 1, 99);
    }

    @Test
    void testCreatingThreeIntegerValue() {
        IntegerValue integerValue = new IntegerValue(new NewOperation<>() {
            @Override
            public Iterator<Integer> applyAll() {
                return Stream.of(1, 99, 42).iterator();
            }
        });

        assertAllPossibleValues(integerValue, 1, 99);
    }

    @Test
    void testCreatingIdenticalValue() {
        IntegerValue src = new IntegerValue(42);
        IntegerValue integerValue = new IntegerValue(UnaryOperation.identity(), src);

        assertAllPossibleValues(integerValue, 42);
    }

    @Test
    void testCreatingSummedValue() {
        IntegerValue v1 = new IntegerValue(1);
        IntegerValue v2 = new IntegerValue(42);

        IntegerValue sum = new IntegerValue(new BinaryOperation<Integer, Integer, Integer>() {
            @Override
            public Unknowable<Integer> apply(Integer operand1, Integer operand2) {
                return Unknowable.of(operand1 + operand2);
            }
        }, v1, v2);

        assertAllPossibleValues(sum, 43);
    }

    @Test
    void testCreatingProductValue() {
        IntegerValue v1 = new IntegerValue(new NewOperation<>() {
            @Override
            public Iterator<Integer> applyAll() {
                return Stream.of(-1, 1).iterator();
            }
        });
        IntegerValue v2 = new IntegerValue(new NewOperation<>() {
            @Override
            public Iterator<Integer> applyAll() {
                return Stream.of(-99, 99).iterator();
            }
        });

        IntegerValue product = new IntegerValue(new BinaryOperation<Integer, Integer, Integer>() {
            @Override
            public Unknowable<Integer> apply(Integer operand1, Integer operand2) {
                return Unknowable.of(operand1 * operand2);
            }
        }, v1, v2);

        assertAllPossibleValues(product, -99, 99);
    }
}
