package org.openjdk.leyden.constprop.analysis;

import org.openjdk.leyden.constprop.values.MutableReferenceTypeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ReferenceTracker {
    private final ReferenceTracker parent;
    private final Map<Integer, MutableReferenceTypeValue<?>> values = new HashMap<>();

    public ReferenceTracker() {
        this(null);
    }

    public ReferenceTracker(ReferenceTracker parent) {
        this.parent = parent;
    }

    public void put(int index, MutableReferenceTypeValue<?> value) {
        values.put(index, value);
    }

    public MutableReferenceTypeValue<?> get(int index, long id) {
        Optional<MutableReferenceTypeValue<?>> optional = values.entrySet().stream()
                .filter(e -> e.getValue().getId() == id && e.getKey() <= index)
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue);

        if (optional.isPresent()) {
            return optional.get();
        }

        if (parent != null) {
            return parent.get(index, id);
        }

        return null;
    }
}
