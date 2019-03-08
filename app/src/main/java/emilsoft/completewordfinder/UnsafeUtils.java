package emilsoft.completewordfinder;

import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import sun.misc.Unsafe;

public class UnsafeUtils {

    //private static final int NR_BITS = Integer.valueOf(System.getProperty("sun.arch.data.model"));
    private static final int NR_BITS = (Build.SUPPORTED_64_BIT_ABIS.length > 0) ? 64 : 32;
    private static final int BYTE = 8;
    private static final int WORD = NR_BITS/BYTE;
    private static final int MIN_SIZE = 16;

    public static Unsafe getUnsafe() {
        Unsafe unsafe;
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe)field.get(null);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        //return unsafe;
        return UtilUnsafe.UNSAFE;
    }

    public static long sizeOf(Object o) {
        Unsafe unsafe = getUnsafe();
        HashSet<Field> fields = new HashSet<Field>();
        Class c = o.getClass();
        while (c != Object.class) {
            for (Field f : c.getDeclaredFields())
                if((f.getModifiers() & Modifier.STATIC) == 0)
                    fields.add(f);
            c = c.getSuperclass();
        }

        //get offset
        long maxSize = 0;
        for(Field f : fields) {
            long offset = unsafe.objectFieldOffset(f);
            if(offset > maxSize)
                maxSize = offset;
        }

        long size = ((maxSize/WORD) + 1) * WORD;
        Log.v(MainActivity.TAG, "Object's Size: "+size);
        return size;
    }

    private static long roundUpTo8(final long number) {
        return ((number + 7) / 8) * 8;
    }

    public static long headerSize(Class clazz) {
        // TODO Should be calculated based on the platform
        // TODO maybe unsafe.addressSize() would help?
        long len = 12; // JVM_64 has a 12 byte header 8 + 4 (with compressed pointers on)
        if (clazz.isArray()) {
            len += 4;
        }
        return len;
    }

    public static long firstFieldOffset(Class clazz) {
        long minSize = roundUpTo8(headerSize(clazz));
        Unsafe unsafe = getUnsafe();
        // Find the min offset for all the classes, up the class hierarchy.
        while (clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                if ((f.getModifiers() & Modifier.STATIC) == 0) {
                    long offset = unsafe.objectFieldOffset(f);
                    if (offset < minSize) {
                        minSize = offset;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        return minSize;
    }

    public static byte[] toByteArray(Object obj) {
        Unsafe unsafe = getUnsafe();
        int len = (int) sizeOf(obj);
        byte[] bytes = new byte[len];
        unsafe.copyMemory(obj, 0, bytes, unsafe.arrayBaseOffset(byte[].class), bytes.length);
        return bytes;
    }

//    public static long sizeOfShort(Object o) {
//        Unsafe unsafe = getUnsafe();
//        return unsafe.getAddress(
//                normalize(unsafe.getInt(o, 4L)) + 12L);
//    }

    private static long normalize(int value) {
        if(value >= 0) return value;
        return (~0L >>> 32) & value;
    }

    public static Object shallowCopy(Object obj) {
        Unsafe unsafe = getUnsafe();
        int size = UnsafeUtils.sizeOf(obj.getClass());
        long start = toAddress(obj);
        long address = unsafe.allocateMemory(size);
        unsafe.copyMemory(start, address, size);
        return fromAddress(address);
    }

    public static long toAddress(Object obj) {
        Unsafe unsafe = getUnsafe();
        Object[] array = new Object[]{obj};
        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
        return normalize(unsafe.getInt(array, baseOffset));
    }

    public static Object fromAddress(long address) {
        Unsafe unsafe = getUnsafe();
        Object[] array = new Object[]{null};
        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
        unsafe.putLong(array, baseOffset, address);
        return array[0];
    }

    public static int sizeOf(Class src) {
        //Get the instance fields of src class
        List<Field> instanceFields = new LinkedList<Field>();
        do {
            if(src == Object.class)
                return MIN_SIZE;
            for(Field f : src.getDeclaredFields()) {
                if((f.getModifiers() & Modifier.STATIC) == 0)
                    instanceFields.add(f);
            }
            src = src.getSuperclass();
        }
        while (instanceFields.isEmpty());

        //Get the field with the maximum offset
        long maxOffset = 0;
        for(Field f : instanceFields) {
            long offset = UtilUnsafe.UNSAFE.objectFieldOffset(f);
            if(offset > maxOffset)
                maxOffset = offset;
        }
        Log.v(MainActivity.TAG,"NR_BITS: "+NR_BITS+" bit - SizeOf: "+(((int) maxOffset/WORD) + 1) * WORD);
        return (((int) maxOffset/WORD) + 1) * WORD;
    }

}


