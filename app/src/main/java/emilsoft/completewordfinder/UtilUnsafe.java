package emilsoft.completewordfinder;

import java.lang.reflect.Field;

public class UtilUnsafe {

    public static final sun.misc.Unsafe UNSAFE;

    static {
        Object theUnsafe = null;
        Exception exception = null;
        try {
            Class<?> uc = Class.forName("sun.misc.Unsafe");
            Field f = uc.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            theUnsafe = f.get(uc);
        } catch (Exception e) {
            exception = e;
        }
        UNSAFE = (sun.misc.Unsafe) theUnsafe;
        if(UNSAFE == null)
            throw new Error("Could not obtain access to sun.misc.Unsafe", exception);
    }

    private UtilUnsafe() {}

}
