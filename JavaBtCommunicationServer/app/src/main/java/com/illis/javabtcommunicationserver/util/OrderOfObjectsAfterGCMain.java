package com.illis.javabtcommunicationserver.util;

import com.ironz.unsafe.UnsafeAndroid;

import java.util.Arrays;
import java.util.Collections;

public class OrderOfObjectsAfterGCMain {
    /* raw memory에 access하고 포인터 수준의 연산을 지원하는 sun.misc.Unsafe 클래스를 안드로이드에서 사용할 수 있도록 구현한 클래스 */
    static final UnsafeAndroid unsafe = new UnsafeAndroid();
    static final boolean is64bit = true; // auto detect if possible.

    public static void main(String... args) {
        Double[] ascending = new Double[16];
        for(int i=0;i<ascending.length;i++)
            ascending[i] = (double) i;

        Double[] descending = new Double[16];
        for(int i=descending.length-1; i>=0; i--)
            descending[i] = (double) i;

        Double[] shuffled = new Double[16];
        for(int i=0;i<shuffled.length;i++)
            shuffled[i] = (double) i;
        Collections.shuffle(Arrays.asList(shuffled));

        System.out.println("Before GC");
        printAddresses("ascending", ascending);
        printAddresses("descending", descending);
        printAddresses("shuffled", shuffled);

        System.gc();
        System.out.println("\nAfter GC");
        printAddresses("ascending", ascending);
        printAddresses("descending", descending);
        printAddresses("shuffled", shuffled);

        System.gc();
        System.out.println("\nAfter GC 2");
        printAddresses("ascending", ascending);
        printAddresses("descending", descending);
        printAddresses("shuffled", shuffled);
    }

    public static void printAddresses(String label, Object... objects) {
        System.out.print(label + ": 0x");
        long last = 0;
        int offset = unsafe.arrayBaseOffset(objects.getClass());
        int scale = unsafe.arrayIndexScale(objects.getClass());
        switch (scale) {
            case 4:
                long factor = is64bit ? 8 : 1;
                final long i1 = (unsafe.getInt(objects, offset) & 0xFFFFFFFFL) * factor;
                System.out.print(Long.toHexString(i1));
                last = i1;
                for (int i = 1; i < objects.length; i++) {
                    final long i2 = (unsafe.getInt(objects, offset + i * 4) & 0xFFFFFFFFL) * factor;
                    if (i2 > last)
                        System.out.print(", +" + Long.toHexString(i2 - last));
                    else
                        System.out.print(", -" + Long.toHexString( last - i2));
                    last = i2;
                }
                break;
            case 8:
                throw new AssertionError("Not supported");
        }
        System.out.println();
    }
}