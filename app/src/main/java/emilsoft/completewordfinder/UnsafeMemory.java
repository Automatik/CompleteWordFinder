package emilsoft.completewordfinder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import sun.misc.Unsafe;

public class UnsafeMemory {

    private static final Unsafe unsafe;
    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe)field.get(null);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final long byteArrayOffset = unsafe.arrayBaseOffset(byte[].class);
    private static final long longArrayOffset = unsafe.arrayBaseOffset(long[].class);
    private static final long doubleArrayOffset = unsafe.arrayBaseOffset(double[].class);
    private static final long trieNodeArrayOffset = unsafe.arrayBaseOffset(TrieNode[].class);

    private static final int SIZE_OF_BOOLEAN = 1;
    private static final int SIZE_OF_INT = 4;
    private static final int SIZE_OF_LONG = 8;
    private static final int SIZE_OF_CHAR = 2;

    private int pos = 0;
    private final byte[] buffer;

    public UnsafeMemory(final byte[] buffer) {
        if (null == buffer) {
            throw new NullPointerException("buffer cannot be null");
        }

        this.buffer = buffer;
    }

    public void reset() {
        this.pos = 0;
    }

    public void putBoolean(final boolean value) {
        unsafe.putBoolean(buffer, byteArrayOffset + pos, value);
        pos += SIZE_OF_BOOLEAN;
    }

    public boolean getBoolean() {
        boolean value = unsafe.getBoolean(buffer, byteArrayOffset + pos);
        pos += SIZE_OF_BOOLEAN;

        return value;
    }

    public void putInt(final int value) {
        unsafe.putInt(buffer, byteArrayOffset + pos, value);
        pos += SIZE_OF_INT;
    }

    public int getInt() {
        int value = unsafe.getInt(buffer, byteArrayOffset + pos);
        pos += SIZE_OF_INT;

        return value;
    }

    public void putLong(final long value) {
        unsafe.putLong(buffer, byteArrayOffset + pos, value);
        pos += SIZE_OF_LONG;
    }

    public long getLong() {
        long value = unsafe.getLong(buffer, byteArrayOffset + pos);
        pos += SIZE_OF_LONG;

        return value;
    }

    public void putChar(final char value) {
        unsafe.putChar(buffer, byteArrayOffset + pos, value);
        pos += SIZE_OF_CHAR;
    }

    public char getChar() {
        char value = unsafe.getChar(buffer, byteArrayOffset + pos);
        pos += SIZE_OF_CHAR;

        return value;
    }

//    public void putTrieNode(final TrieNode value) {
//        long offset = toAddress(value);
//        unsafe.putObject(buffer, offset + pos, value);
//        pos += UnsafeUtils.sizeOf(TrieNode.class);
//    }
//
//    public TrieNode getTrieNode() {
//        TrieNode value = (TrieNode) unsafe.getObject(buffer, byteArrayOffset + pos);
//        pos += UnsafeUtils.sizeOf(TrieNode.class);
//
//        return value;
//    }

    public void putObject(Object obj) {
        Class c = obj.getClass();
        do {
            for (Field f : c.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    long offset = unsafe.objectFieldOffset(f);
                    if (f.getType() == long.class) {
                        unsafe.putLong(pos + offset, unsafe.getLong(obj, offset));
                    } else if (f.getType() == int.class) {
                        unsafe.putInt(pos + offset, unsafe.getInt(obj, offset));
                    } else {
                        throw new UnsupportedOperationException();
                    }
                }
            }
        } while ((c = c.getSuperclass()) != null);
    }

    public void putTrieNodeArray(final TrieNode[] values) {
        putInt(values.length);

        long bytesToCopy = values.length << 3;
        long bytes = UnsafeUtils.sizeOf(values);
        unsafe.copyMemory(values, trieNodeArrayOffset,
                buffer, byteArrayOffset + pos,
                bytesToCopy);
    }

    public void putLongArray(final long[] values) {
        putInt(values.length);

        long bytesToCopy = values.length << 3;
        unsafe.copyMemory(values, longArrayOffset,
                buffer, byteArrayOffset + pos,
                bytesToCopy);
        pos += bytesToCopy;
    }

    public long[] getLongArray() {
        int arraySize = getInt();
        long[] values = new long[arraySize];

        long bytesToCopy = values.length << 3;
        unsafe.copyMemory(buffer, byteArrayOffset + pos,
                values, longArrayOffset,
                bytesToCopy);
        pos += bytesToCopy;

        return values;
    }

    public void putDoubleArray(final double[] values) {
        putInt(values.length);

        long bytesToCopy = values.length << 3;
        unsafe.copyMemory(values, doubleArrayOffset,
                buffer, byteArrayOffset + pos,
                bytesToCopy);
        pos += bytesToCopy;
    }

    public double[] getDoubleArray() {
        int arraySize = getInt();
        double[] values = new double[arraySize];

        long bytesToCopy = values.length << 3;
        unsafe.copyMemory(buffer, byteArrayOffset + pos,
                values, doubleArrayOffset,
                bytesToCopy);
        pos += bytesToCopy;

        return values;
    }

}
