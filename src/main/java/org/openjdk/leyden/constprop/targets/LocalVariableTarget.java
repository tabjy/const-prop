package org.openjdk.leyden.constprop.targets;

import org.objectweb.asm.tree.analysis.Frame;
import org.openjdk.leyden.constprop.values.ConstantizationValue;

import java.util.Objects;

public class LocalVariableTarget extends ConstantizationTarget {
    public final int instruction;
    public final int slot; // the index of the slot from the local variable table
    public final int line;
    public final String name;

    LocalVariableTarget(int instruction, int slot, String name, int line) {
        this.instruction = instruction;
        this.slot = slot;
        this.name = name;
        this.line = line;
    }

    @Override
    Scope getScope() {
        return Scope.METHOD;
    }

    @Override
    public String toString() {
        return String.format("LocalVariableTarget[instruction=%d, index=%d, name=%s, line=%d]",
                instruction, slot, name, line);
    }

    @Override
    public ConstantizationValue<?> extractValueFromFrames(Frame<ConstantizationValue<?>>[] frames) {
        // NOTE: the last frame could've been null if the last instruction being a pseudo-instruction (e.g., a label),
        //       in this case, we really want the last non-null frame
        Frame<ConstantizationValue<?>> frame = null;
        for (int i = instruction; i >= 0 && frame == null; i--) {
            frame = frames[i];
        }

        Objects.requireNonNull(frame);
        return frame.getLocal(slot);
    }
}