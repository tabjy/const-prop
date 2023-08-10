package org.openjdk.leyden.constprop.analysis;

import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.openjdk.leyden.constprop.operations.BinaryOperation;
import org.openjdk.leyden.constprop.operations.UnaryOperation;
import org.openjdk.leyden.constprop.util.Unknowable;
import org.openjdk.leyden.constprop.values.AbstractConstantizationValueFactory;
import org.openjdk.leyden.constprop.values.ConstantizationValue;
import org.openjdk.leyden.constprop.values.DefaultConstantizationValueFactory;
import org.openjdk.leyden.constprop.values.DoubleValue;
import org.openjdk.leyden.constprop.values.FloatValue;
import org.openjdk.leyden.constprop.values.IntegerValue;
import org.openjdk.leyden.constprop.values.LongValue;

import java.lang.constant.ClassDesc;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

class ConstantizationInterpreter extends Interpreter<ConstantizationValue<?>> {
    private final AbstractConstantizationValueFactory valueFactory;
    private final AbstractClassPool classPool;

    private InsnList instructions;
    private int[] basicBlockStarts;
    private int currentBasicBlock;
    private HashMap<Integer, ReferenceTracker> referenceTrackers;

    ConstantizationInterpreter() {
        this(new ClassLoaderClassPool(), new DefaultConstantizationValueFactory());
    }

    ConstantizationInterpreter(AbstractClassPool classPool, AbstractConstantizationValueFactory valueFactory) {
        super(Opcodes.ASM9);

        this.classPool = classPool;
        this.valueFactory = valueFactory;
    }

    private int getInstLineNumberByIndex(int index) {
        Objects.requireNonNull(this.instructions);

        while (true) {
            if (index < 0) {
                return -1;
            }

            AbstractInsnNode inst = this.instructions.get(index--);
            if (inst instanceof LineNumberNode) {
                return ((LineNumberNode) inst).line;
            }
        }
    }

    void setMethodNode(MethodNode method) {
        this.instructions = method.instructions;

        // DEBUG:
        System.out.println("==== instructions ====");
        for (int i = 0; i < this.instructions.size(); i++) {
            System.out.printf("inst #%d, line #%d: %s\n", i, getInstLineNumberByIndex(i), this.instructions.get(i));
        }

        // compute frames
        AbstractInsnNode[] instructions = method.instructions.toArray();
        basicBlockStarts = IntStream.range(0, instructions.length)
                .filter(i -> {
                    AbstractInsnNode ins = instructions[i];
                    return i == 0 // the first frame is implicit
                            || ins instanceof FrameNode
                            || (ins instanceof JumpInsnNode
                            && ins.getOpcode() != Opcodes.GOTO
                            && ins.getOpcode() != Opcodes.ATHROW);
                })
                .map(i -> {
                    // look for the first non-pseudo instruction nodes in a basic block
                    i += 1;
                    while (instructions[i].getOpcode() == -1) i++;
                    return i;
                })
                // It is possible to have two basic block indices enclosing only pseudo instruction nodes, in which case
                // the two frame must be merged. For example: even javac doesn't produce such case, it is valid bytecode
                // to have the non-branching path of a conditional jump instruction to be also a jump target of another
                // instruction.
                .distinct()
                .toArray();

        currentBasicBlock = 0;

        this.referenceTrackers = new HashMap<>();

        // DEBUG:
        System.out.println("==== basic blocks ====");
        for (int i = 0; i < basicBlockStarts.length; i++) {
            System.out.printf("blk #%d, line #%d, inst #%d: %s\n", i, getInstLineNumberByIndex(basicBlockStarts[i]), basicBlockStarts[i], this.instructions.get(basicBlockStarts[i]));
        }
    }

    private void trackBasicBlock(AbstractInsnNode insn) {
        if (instructions == null) {
            return;
        }

        int index = instructions.indexOf(insn);
        int block;
        for (block = 0; block < basicBlockStarts.length; block++) {
            if (basicBlockStarts[block] >= index) {
                break;
            }
        }

        referenceTrackers.putIfAbsent(block,
                block == 0 ? new ReferenceTracker() : new ReferenceTracker(referenceTrackers.get(currentBasicBlock)));
        currentBasicBlock = block;

        // DEBUG:
        System.out.println("==== tracking basic block ====");
        System.out.printf("inst #%d, line #%d, blk #%d\n", index, getInstLineNumberByIndex(index), block);
    }

    private ReferenceTracker getCurrentReferenceTracker() {
        return referenceTrackers.get(currentBasicBlock);
    }

    @Override
    public ConstantizationValue<?> newValue(Type type) {
        if (type == null) {
            return ConstantizationValue.UninitializedValue.INSTANCE;
        }

        if (type == Type.VOID_TYPE) {
            return null;
        }

        return valueFactory.createValue(type.getDescriptor());
    }

    @Override
    public ConstantizationValue<?> newOperation(AbstractInsnNode insn) throws AnalyzerException {
        trackBasicBlock(insn);

        // constants
        switch (insn.getOpcode()) {
            case Opcodes.ICONST_M1 -> {
                return new IntegerValue(-1);
            }
            case Opcodes.ICONST_0 -> {
                return new IntegerValue(0);
            }
            case Opcodes.ICONST_1 -> {
                return new IntegerValue(1);
            }
            case Opcodes.ICONST_2 -> {
                return new IntegerValue(2);
            }
            case Opcodes.ICONST_3 -> {
                return new IntegerValue(3);
            }
            case Opcodes.ICONST_4 -> {
                return new IntegerValue(4);
            }
            case Opcodes.ICONST_5 -> {
                return new IntegerValue(5);
            }
            case Opcodes.LCONST_0 -> {
                return new LongValue(0L);
            }
            case Opcodes.LCONST_1 -> {
                return new LongValue(1L);
            }
            case Opcodes.FCONST_0 -> {
                return new FloatValue(0f);
            }
            case Opcodes.FCONST_1 -> {
                return new FloatValue(1f);
            }
            case Opcodes.FCONST_2 -> {
                return new FloatValue(2f);
            }
            case Opcodes.DCONST_0 -> {
                return new DoubleValue(0d);
            }
            case Opcodes.DCONST_1 -> {
                return new DoubleValue(1d);
            }
            case Opcodes.BIPUSH, Opcodes.SIPUSH -> {
                return new IntegerValue(((IntInsnNode) insn).operand);
            }
        }

        if (insn.getOpcode() == Opcodes.LDC) {
            // JVMSPECS 6.5: "LDC [...] index either must be a run-time constant of type int or float, or a reference to
            // a string literal..."
            Object value = ((LdcInsnNode) insn).cst;
            if (value instanceof Integer i) {
                return valueFactory.createValue(Type.INT_TYPE.getDescriptor(), i);
            }

            if (value instanceof Float f) {
                return valueFactory.createValue(Type.FLOAT_TYPE.getDescriptor(), f);
            }

            if (value instanceof String s) {
                return valueFactory.createValue(Type.DOUBLE_TYPE.getDescriptor(), s);
            }

            // JVMSPECS 6.5: "LDC_2W [...] index must be a run-time constant of type long or double"
            if (value instanceof Long l) {
                return valueFactory.createValue(Type.LONG_TYPE.getDescriptor(), l);
            }

            if (value instanceof Double d) {
                return valueFactory.createValue(Type.DOUBLE_TYPE.getDescriptor(), d);
            }

            // JVMSPECS 6.5: "... or a symbolic reference to a class, method type, or method handle"
            if (value instanceof Type t) {
                if (t.getSort() == Type.OBJECT) {
                    return valueFactory.createValue("L/java/lang/Class;", ClassDesc.ofDescriptor(t.getDescriptor()));
                }

                // TODO: are other sorts possible?
                throw new IllegalStateException("Unexpected LDC sort: " + t.getSort());
            }

            if (value instanceof Handle || value instanceof ConstantDynamic) {
                throw new UnsupportedOperationException("Unsupported constant type: " + value.getClass().getName());
            }
        }

        if (insn.getOpcode() == Opcodes.ACONST_NULL) {
            return valueFactory.createValue("Ljava/lang/Object;", null);
        }

        if (insn.getOpcode() == Opcodes.GETSTATIC) {
            FieldInsnNode fin = ((FieldInsnNode) insn);
            try {
                // currently only supports static final fields
                Optional<Object> opt = classPool.staticFinalFieldValueForName(fin.owner, fin.name);
                if (opt.isPresent()) {
                    return valueFactory.createValue(fin.desc, opt.get());
                } else {
                    throw new UnsupportedOperationException("GETSTATIC not possible");
                }

            } catch (ClassNotFoundException | FieldNotFoundException e) {
                // TODO: result in unknown value
                throw new UnsupportedOperationException("GETSTATIC not possible");
            }
        }

        if (insn.getOpcode() == Opcodes.NEW) {
            // TODO: create new mutable objects/arrays
            // NOTE: TypeInsnNode#desc is actually the internal name instead of the descriptor
            return valueFactory.createValue("L" + ((TypeInsnNode) insn).desc + ";");
        }

        throw new AnalyzerException(insn, "Illegal opcode: " + insn.getOpcode());
    }

    @Override
    public ConstantizationValue<?> copyOperation(AbstractInsnNode insn, ConstantizationValue<?> value) throws AnalyzerException {
        Objects.requireNonNull(value);

        trackBasicBlock(insn);

        // ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, DUP, DUP_X1, DUP_X2, DUP2,
        // DUP2_X1, DUP2_X2, SWAP

        // NOTE: we create a new value which is performs an identify operation from the original instead of creating
        // copies of the original value object. This way we add a new node to the chain.
        return value.ident();
    }

    @Override
    public ConstantizationValue<?> unaryOperation(AbstractInsnNode insn, ConstantizationValue<?> value) throws AnalyzerException {
        Objects.requireNonNull(value);

        trackBasicBlock(insn);

        return switch (insn.getOpcode()) {
            // void results
            case Opcodes.IFEQ,
                    Opcodes.IFNE,
                    Opcodes.FRETURN,
                    Opcodes.IFLT,
                    Opcodes.IFGE,
                    Opcodes.IFGT,
                    Opcodes.IFLE,
                    Opcodes.TABLESWITCH,
                    Opcodes.LOOKUPSWITCH,
                    Opcodes.IRETURN,
                    Opcodes.LRETURN,
                    Opcodes.DRETURN,
                    Opcodes.ARETURN,
                    Opcodes.PUTSTATIC, // TODO: handle this?
                    Opcodes.ATHROW,
                    Opcodes.MONITORENTER,
                    Opcodes.MONITOREXIT,
                    Opcodes.IFNULL,
                    Opcodes.IFNONNULL -> null;

            // int results
            case Opcodes.INEG -> new IntegerValue(new UnaryOperation<Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer operand) {
                    return Unknowable.of(operand * -1);
                }
            }, value);
            case Opcodes.IINC -> new IntegerValue(new UnaryOperation<Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer operand) {
                    return Unknowable.of(operand + 1);
                }
            }, value);
            case Opcodes.L2I -> new IntegerValue(new UnaryOperation<Long, Integer>() {
                @Override
                public Unknowable<Integer> apply(Long operand) {
                    return Unknowable.of(operand.intValue());
                }
            }, value);
            case Opcodes.F2I -> new IntegerValue(new UnaryOperation<Float, Integer>() {
                @Override
                public Unknowable<Integer> apply(Float operand) {
                    return Unknowable.of(operand.intValue());
                }
            }, value);
            case Opcodes.D2I -> new IntegerValue(new UnaryOperation<Double, Integer>() {
                @Override
                public Unknowable<Integer> apply(Double operand) {
                    return Unknowable.of(operand.intValue());
                }
            }, value);
            case Opcodes.I2B -> new IntegerValue(new UnaryOperation<Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer operand) {
                    return Unknowable.of((int) operand.byteValue());
                }
            }, value);
            case Opcodes.I2C -> new IntegerValue(new UnaryOperation<Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer operand) {
                    return Unknowable.of((int) (char) operand.intValue());
                }
            }, value);
            case Opcodes.I2S -> new IntegerValue(new UnaryOperation<Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer operand) {
                    return Unknowable.of((int) operand.shortValue());
                }
            }, value);
            case Opcodes.ARRAYLENGTH -> null; // TODO: support arrays

            // long results
            case Opcodes.LNEG -> new LongValue(new UnaryOperation<Long, Long>() {
                @Override
                public Unknowable<Long> apply(Long operand) {
                    return Unknowable.of(operand * -1);
                }
            }, value);
            case Opcodes.I2L -> new LongValue(new UnaryOperation<Integer, Long>() {
                @Override
                public Unknowable<Long> apply(Integer operand) {
                    return Unknowable.of(operand.longValue());
                }
            }, value);
            case Opcodes.F2L -> new LongValue(new UnaryOperation<Float, Long>() {
                @Override
                public Unknowable<Long> apply(Float operand) {
                    return Unknowable.of(operand.longValue());
                }
            }, value);
            case Opcodes.D2L -> new LongValue(new UnaryOperation<Double, Long>() {
                @Override
                public Unknowable<Long> apply(Double operand) {
                    return Unknowable.of(operand.longValue());
                }
            }, value);

            // float results
            case Opcodes.FNEG -> new FloatValue(new UnaryOperation<Float, Float>() {
                @Override
                public Unknowable<Float> apply(Float operand) {
                    return Unknowable.of(operand * -1);
                }
            }, value);
            case Opcodes.I2F -> new FloatValue(new UnaryOperation<Integer, Float>() {
                @Override
                public Unknowable<Float> apply(Integer operand) {
                    return Unknowable.of(operand.floatValue());
                }
            }, value);
            case Opcodes.L2F -> new FloatValue(new UnaryOperation<Long, Float>() {
                @Override
                public Unknowable<Float> apply(Long operand) {
                    return Unknowable.of(operand.floatValue());
                }
            }, value);
            case Opcodes.D2F -> new FloatValue(new UnaryOperation<Double, Float>() {
                @Override
                public Unknowable<Float> apply(Double operand) {
                    return Unknowable.of(operand.floatValue());
                }
            }, value);

            // double results
            case Opcodes.DNEG -> new DoubleValue(new UnaryOperation<Double, Double>() {
                @Override
                public Unknowable<Double> apply(Double operand) {
                    return Unknowable.of(operand * -1);
                }
            }, value);
            case Opcodes.I2D -> new DoubleValue(new UnaryOperation<Integer, Double>() {
                @Override
                public Unknowable<Double> apply(Integer operand) {
                    return Unknowable.of(operand.doubleValue());
                }
            }, value);
            case Opcodes.L2D -> new DoubleValue(new UnaryOperation<Long, Double>() {
                @Override
                public Unknowable<Double> apply(Long operand) {
                    return Unknowable.of(operand.doubleValue());
                }
            }, value);
            case Opcodes.F2D -> new DoubleValue(new UnaryOperation<Float, Double>() {
                @Override
                public Unknowable<Double> apply(Float operand) {
                    return Unknowable.of(operand.doubleValue());
                }
            }, value);

            // get field
            // TODO: support GETFIELD
            case Opcodes.GETFIELD -> null;

            // array creation
            // TODO: support arrays
            case Opcodes.NEWARRAY, Opcodes.ANEWARRAY -> null;

            // reference type casting
            // TODO: determine actual typing at compile time if possible
            case Opcodes.CHECKCAST -> null;

            default -> throw new AnalyzerException(insn, "Illegal opcode: " + insn.getOpcode());
        };
    }

    @Override
    public ConstantizationValue<?> binaryOperation(AbstractInsnNode insn, ConstantizationValue<?> value1, ConstantizationValue<?> value2) throws AnalyzerException {
        Objects.requireNonNull(value1);
        Objects.requireNonNull(value2);

        trackBasicBlock(insn);

        return switch (insn.getOpcode()) {
            // void results
            case Opcodes.IF_ICMPEQ,
                    Opcodes.IF_ICMPNE,
                    Opcodes.IF_ICMPLT,
                    Opcodes.IF_ICMPGE,
                    Opcodes.IF_ICMPGT,
                    Opcodes.IF_ICMPLE,
                    Opcodes.IF_ACMPEQ,
                    Opcodes.IF_ACMPNE,
                    Opcodes.PUTFIELD -> null; // TODO: support mutable objects

            // array operations
            // TODO: support array access
            case Opcodes.AALOAD,
                    Opcodes.BALOAD,
                    Opcodes.CALOAD,
                    Opcodes.FALOAD,
                    Opcodes.IALOAD,
                    Opcodes.SALOAD -> null;
            case Opcodes.LALOAD,
                    Opcodes.DALOAD -> null;

            // integer arithmetics
            case Opcodes.IADD -> new IntegerValue(new BinaryOperation<Integer, Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer v1, Integer v2) {
                    return Unknowable.of(v1 + v2);
                }
            }, value1, value2);
            case Opcodes.ISUB -> new IntegerValue(new BinaryOperation<Integer, Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer v1, Integer v2) {
                    return Unknowable.of(v1 - v2);
                }
            }, value1, value2);
            case Opcodes.IMUL -> new IntegerValue(new BinaryOperation<Integer, Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer v1, Integer v2) {
                    return Unknowable.of(v1 * v2);
                }
            }, value1, value2);
            case Opcodes.IDIV -> new IntegerValue(new BinaryOperation<Integer, Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer v1, Integer v2) {
                    return Unknowable.of(v1 / v2);
                }
            }, value1, value2);
            case Opcodes.IREM -> new IntegerValue(new BinaryOperation<Integer, Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer v1, Integer v2) {
                    return Unknowable.of(v1 % v2);
                }
            }, value1, value2);
            case Opcodes.ISHL -> new IntegerValue(new BinaryOperation<Integer, Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer v1, Integer v2) {
                    return Unknowable.of(v1 << v2);
                }
            }, value1, value2);
            case Opcodes.ISHR -> new IntegerValue(new BinaryOperation<Integer, Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer v1, Integer v2) {
                    return Unknowable.of(v1 >> v2);
                }
            }, value1, value2);
            case Opcodes.IUSHR -> new IntegerValue(new BinaryOperation<Integer, Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer v1, Integer v2) {
                    return Unknowable.of(v1 >>> v2);
                }
            }, value1, value2);
            case Opcodes.IAND -> new IntegerValue(new BinaryOperation<Integer, Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer v1, Integer v2) {
                    return Unknowable.of(v1 & v2);
                }
            }, value1, value2);
            case Opcodes.IOR -> new IntegerValue(new BinaryOperation<Integer, Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer v1, Integer v2) {
                    return Unknowable.of(v1 | v2);
                }
            }, value1, value2);
            case Opcodes.IXOR -> new IntegerValue(new BinaryOperation<Integer, Integer, Integer>() {
                @Override
                public Unknowable<Integer> apply(Integer v1, Integer v2) {
                    return Unknowable.of(v1 ^ v2);
                }
            }, value1, value2);

            // float arithmetics
            case Opcodes.FADD -> new FloatValue(new BinaryOperation<Float, Float, Float>() {
                @Override
                public Unknowable<Float> apply(Float v1, Float v2) {
                    return Unknowable.of(v1 + v2);
                }
            }, value1, value2);
            case Opcodes.FSUB -> new FloatValue(new BinaryOperation<Float, Float, Float>() {
                @Override
                public Unknowable<Float> apply(Float v1, Float v2) {
                    return Unknowable.of(v1 - v2);
                }
            }, value1, value2);
            case Opcodes.FMUL -> new FloatValue(new BinaryOperation<Float, Float, Float>() {
                @Override
                public Unknowable<Float> apply(Float v1, Float v2) {
                    return Unknowable.of(v1 * v2);
                }
            }, value1, value2);
            case Opcodes.FDIV -> new FloatValue(new BinaryOperation<Float, Float, Float>() {
                @Override
                public Unknowable<Float> apply(Float v1, Float v2) {
                    return Unknowable.of(v1 / v2);
                }
            }, value1, value2);
            case Opcodes.FREM -> new FloatValue(new BinaryOperation<Float, Float, Float>() {
                @Override
                public Unknowable<Float> apply(Float v1, Float v2) {
                    return Unknowable.of(v1 % v2);
                }
            }, value1, value2);

            // long arithmetics
            case Opcodes.LADD -> new LongValue(new BinaryOperation<Long, Long, Long>() {
                @Override
                public Unknowable<Long> apply(Long v1, Long v2) {
                    return Unknowable.of(v1 + v2);
                }
            }, value1, value2);
            case Opcodes.LSUB -> new LongValue(new BinaryOperation<Long, Long, Long>() {
                @Override
                public Unknowable<Long> apply(Long v1, Long v2) {
                    return Unknowable.of(v1 - v2);
                }
            }, value1, value2);
            case Opcodes.LMUL -> new LongValue(new BinaryOperation<Long, Long, Long>() {
                @Override
                public Unknowable<Long> apply(Long v1, Long v2) {
                    return Unknowable.of(v1 * v2);
                }
            }, value1, value2);
            case Opcodes.LDIV -> new LongValue(new BinaryOperation<Long, Long, Long>() {
                @Override
                public Unknowable<Long> apply(Long v1, Long v2) {
                    return Unknowable.of(v1 / v2);
                }
            }, value1, value2);
            case Opcodes.LREM -> new LongValue(new BinaryOperation<Long, Long, Long>() {
                @Override
                public Unknowable<Long> apply(Long v1, Long v2) {
                    return Unknowable.of(v1 % v2);
                }
            }, value1, value2);
            case Opcodes.LSHL -> new LongValue(new BinaryOperation<Long, Long, Long>() {
                @Override
                public Unknowable<Long> apply(Long v1, Long v2) {
                    return Unknowable.of(v1 << v2);
                }
            }, value1, value2);
            case Opcodes.LSHR -> new LongValue(new BinaryOperation<Long, Long, Long>() {
                @Override
                public Unknowable<Long> apply(Long v1, Long v2) {
                    return Unknowable.of(v1 >> v2);
                }
            }, value1, value2);
            case Opcodes.LUSHR -> new LongValue(new BinaryOperation<Long, Long, Long>() {
                @Override
                public Unknowable<Long> apply(Long v1, Long v2) {
                    return Unknowable.of(v1 >>> v2);
                }
            }, value1, value2);
            case Opcodes.LAND -> new LongValue(new BinaryOperation<Long, Long, Long>() {
                @Override
                public Unknowable<Long> apply(Long v1, Long v2) {
                    return Unknowable.of(v1 & v2);
                }
            }, value1, value2);
            case Opcodes.LOR -> new LongValue(new BinaryOperation<Long, Long, Long>() {
                @Override
                public Unknowable<Long> apply(Long v1, Long v2) {
                    return Unknowable.of(v1 | v2);
                }
            }, value1, value2);
            case Opcodes.LXOR -> new LongValue(new BinaryOperation<Long, Long, Long>() {
                @Override
                public Unknowable<Long> apply(Long v1, Long v2) {
                    return Unknowable.of(v1 ^ v2);
                }
            }, value1, value2);

            // double arithmetics
            case Opcodes.DADD -> new DoubleValue(new BinaryOperation<Double, Double, Double>() {
                @Override
                public Unknowable<Double> apply(Double v1, Double v2) {
                    return Unknowable.of(v1 + v2);
                }
            }, value1, value2);
            case Opcodes.DSUB -> new DoubleValue(new BinaryOperation<Double, Double, Double>() {
                @Override
                public Unknowable<Double> apply(Double v1, Double v2) {
                    return Unknowable.of(v1 - v2);
                }
            }, value1, value2);
            case Opcodes.DMUL -> new DoubleValue(new BinaryOperation<Double, Double, Double>() {
                @Override
                public Unknowable<Double> apply(Double v1, Double v2) {
                    return Unknowable.of(v1 * v2);
                }
            }, value1, value2);
            case Opcodes.DDIV -> new DoubleValue(new BinaryOperation<Double, Double, Double>() {
                @Override
                public Unknowable<Double> apply(Double v1, Double v2) {
                    return Unknowable.of(v1 / v2);
                }
            }, value1, value2);
            case Opcodes.DREM -> new DoubleValue(new BinaryOperation<Double, Double, Double>() {
                @Override
                public Unknowable<Double> apply(Double v1, Double v2) {
                    return Unknowable.of(v1 % v2);
                }
            }, value1, value2);

            // comparisons
            case Opcodes.LCMP -> new IntegerValue(new BinaryOperation<Long, Long, Integer>() {
                @Override
                public Unknowable<Integer> apply(Long v1, Long v2) {
                    return Unknowable.of(Long.compare(v1, v2));
                }
            });
            case Opcodes.FCMPL -> new IntegerValue(new BinaryOperation<Float, Float, Integer>() {
                @Override
                public Unknowable<Integer> apply(Float v1, Float v2) {
                    return Unknowable.of(
                            Float.isNaN(v1) || Float.isNaN(v2)
                                    ? -1
                                    : Float.compare(v1, v2));
                }
            });
            case Opcodes.FCMPG -> new IntegerValue(new BinaryOperation<Float, Float, Integer>() {
                @Override
                public Unknowable<Integer> apply(Float v1, Float v2) {
                    return Unknowable.of(
                            Float.isNaN(v1) || Float.isNaN(v2)
                                    ? 1
                                    : Float.compare(v1, v2));
                }
            });
            case Opcodes.DCMPL -> new IntegerValue(new BinaryOperation<Double, Double, Integer>() {
                @Override
                public Unknowable<Integer> apply(Double v1, Double v2) {
                    return Unknowable.of(
                            Double.isNaN(v1) || Double.isNaN(v2)
                                    ? -1
                                    : Double.compare(v1, v2));
                }
            });
            case Opcodes.DCMPG -> new IntegerValue(new BinaryOperation<Double, Double, Integer>() {
                @Override
                public Unknowable<Integer> apply(Double v1, Double v2) {
                    return Unknowable.of(
                            Double.isNaN(v1) || Double.isNaN(v2)
                                    ? 1
                                    : Double.compare(v1, v2));
                }
            });

            default -> throw new AnalyzerException(insn, "Illegal opcode: " + insn.getOpcode());
        };
    }

    @Override
    public ConstantizationValue<?> ternaryOperation(AbstractInsnNode insn, ConstantizationValue<?> value1, ConstantizationValue<?> value2, ConstantizationValue<?> value3) throws AnalyzerException {
        Objects.requireNonNull(value1);
        Objects.requireNonNull(value2);
        Objects.requireNonNull(value3);

        trackBasicBlock(insn);

        // IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE
        // TODO: support arrays

        return null;
    }

    @Override
    public ConstantizationValue<?> naryOperation(AbstractInsnNode insn, List<? extends ConstantizationValue<?>> values) throws AnalyzerException {
        trackBasicBlock(insn);

        return null;
    }

    @Override
    public void returnOperation(AbstractInsnNode insn, ConstantizationValue<?> values, ConstantizationValue<?> expected) throws AnalyzerException {
        trackBasicBlock(insn);

        // noop
    }

    @Override
    public ConstantizationValue<?> merge(ConstantizationValue<?> value1, ConstantizationValue<?> value2) {
        // TODO: better way to handle unchecked casts?

        @SuppressWarnings("unchecked")
        ConstantizationValue<Object> v1 = (ConstantizationValue<Object>) value1;
        @SuppressWarnings("unchecked")
        ConstantizationValue<Object> v2 = (ConstantizationValue<Object>) value2;

        return v1.merge(v2);
    }
}
