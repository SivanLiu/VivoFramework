package com.vivo.services.rms.sdk;

import android.util.SparseArray;
import java.util.Stack;

public final class IntArrayFactory {
    private static final int MAX_SIZE = 16;
    private static final SparseArray<Stack<int[]>> sCaches = new SparseArray(16);

    public static int[] create(int length) {
        synchronized (sCaches) {
            if (length <= 0) {
                return null;
            }
            Stack<int[]> stack = (Stack) sCaches.get(length);
            if (stack == null) {
                stack = new Stack();
                sCaches.put(length, stack);
            }
            int[] iArr;
            if (stack.isEmpty()) {
                iArr = new int[length];
                return iArr;
            }
            iArr = (int[]) stack.pop();
            return iArr;
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0028, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void recycle(int[] array) {
        synchronized (sCaches) {
            if (array == null) {
                return;
            }
            int length = array.length;
            Stack<int[]> stack = (Stack) sCaches.get(length);
            if (stack == null) {
                stack = new Stack();
                sCaches.put(length, stack);
            }
            if (stack.size() < 16) {
                stack.push(array);
            }
        }
    }
}
