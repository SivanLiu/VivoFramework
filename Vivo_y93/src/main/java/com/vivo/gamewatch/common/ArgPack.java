package com.vivo.gamewatch.common;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.ArrayList;

public class ArgPack implements Parcelable {
    public static final byte ARG = (byte) 15;
    public static final String ARG_NAME = "ArgPack";
    public static final byte BOOLEAN = (byte) 2;
    public static final String BOOLEAN_NAME = "boolean";
    public static final byte BYTE = (byte) 3;
    public static final byte BYTE_ARRAY = (byte) 8;
    public static final String BYTE_ARRAY_NAME = "byte[]";
    public static final String BYTE_NAME = "byte";
    public static final Creator<ArgPack> CREATOR = new Creator<ArgPack>() {
        public ArgPack createFromParcel(Parcel source) {
            return ArgPack.createFromParcel(source);
        }

        public ArgPack[] newArray(int size) {
            return new ArgPack[size];
        }
    };
    public static final byte FLOAT = (byte) 13;
    public static final byte FLOAT_ARRAY = (byte) 14;
    public static final String FLOAT_ARRAY_NAME = "float[]";
    public static final String FLOAT_NAME = "float";
    public static final byte IBINDER = (byte) 7;
    public static final String IBINDER_NAME = "IBinder";
    public static final byte INT = (byte) 4;
    public static final byte INT_ARRAY = (byte) 10;
    public static final String INT_ARRAY_NAME = "int[]";
    public static final String INT_NAME = "int";
    public static final byte LONG = (byte) 5;
    public static final byte LONG_ARRAY = (byte) 11;
    public static final String LONG_ARRAY_NAME = "long[]";
    public static final String LONG_NAME = "long";
    public static final byte NULL = (byte) 1;
    public static final String NULL_NAME = "null";
    private static final String PARAM = "param";
    public static final byte STRING = (byte) 6;
    public static final byte STRING_ARRAY = (byte) 9;
    public static final String STRING_ARRAY_NAME = "String[]";
    public static final byte STRING_LIST = (byte) 12;
    public static final String STRING_LIST_NAME = "ArrayList<String>";
    public static final String STRING_NAME = "String";
    private static final String TAG = "ArgPack";
    public static final byte VOID = (byte) 0;
    public static final String VOID_NAME = "void";
    private Object[] mObjectArgs;
    private byte[] mTypeArgs;

    public ArgPack(Object... args) {
        fill(args);
    }

    public void fill(Object... args) {
        if (args == null) {
            this.mObjectArgs = new Object[1];
            this.mObjectArgs[0] = null;
        } else {
            this.mObjectArgs = args;
        }
        generateTypes();
    }

    public void wrap(Object[] args) {
        if (args == null) {
            this.mObjectArgs = new Object[1];
            this.mObjectArgs[0] = null;
        } else {
            this.mObjectArgs = args;
        }
        generateTypes();
    }

    private void generateTypes() {
        if (this.mObjectArgs.length == 0) {
            this.mTypeArgs = new byte[]{(byte) 0};
            return;
        }
        if (this.mTypeArgs == null || this.mTypeArgs.length != this.mObjectArgs.length) {
            this.mTypeArgs = new byte[this.mObjectArgs.length];
        }
        for (int i = 0; i < size(); i++) {
            this.mTypeArgs[i] = objectType(this.mObjectArgs[i]);
        }
    }

    public Object get(int i) {
        return this.mObjectArgs[i];
    }

    public int size() {
        return this.mObjectArgs.length;
    }

    public Object[] getObjects() {
        return this.mObjectArgs;
    }

    public boolean isEmpty() {
        return this.mObjectArgs.length == 0;
    }

    public byte[] getTypes() {
        return this.mTypeArgs;
    }

    public boolean matches(byte[] pattern) {
        return matches(pattern, this.mTypeArgs);
    }

    public static boolean nullable(byte type) {
        if (type == (byte) 2 || type == (byte) 3 || type == (byte) 4 || type == (byte) 5 || type == (byte) 13) {
            return false;
        }
        return true;
    }

    public static boolean matches(byte pattern, byte input) {
        boolean z = true;
        if (input == (byte) 1 && nullable(pattern)) {
            return true;
        }
        if (input != pattern) {
            z = false;
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean matches(byte[] pattern, byte[] input) {
        if (pattern == null || input == null || pattern.length != input.length) {
            return false;
        }
        for (int i = 0; i < pattern.length; i++) {
            if (!matches(pattern[i], input[i])) {
                return false;
            }
        }
        return true;
    }

    public static String typeName(byte type) {
        switch (type) {
            case (byte) 0:
                return "void";
            case (byte) 1:
                return "null";
            case (byte) 2:
                return "boolean";
            case (byte) 3:
                return "byte";
            case (byte) 4:
                return "int";
            case (byte) 5:
                return "long";
            case (byte) 6:
                return "String";
            case (byte) 7:
                return "IBinder";
            case (byte) 8:
                return "byte[]";
            case (byte) 9:
                return "String[]";
            case (byte) 10:
                return "int[]";
            case (byte) 11:
                return "long[]";
            case (byte) 12:
                return "ArrayList<String>";
            case (byte) 13:
                return "float";
            case (byte) 14:
                return "float[]";
            case (byte) 15:
                return "ArgPack";
            default:
                return "unkown";
        }
    }

    public static Class<?> typeClass(byte type) {
        switch (type) {
            case (byte) 2:
                return Boolean.TYPE;
            case (byte) 3:
                return Byte.TYPE;
            case (byte) 4:
                return Integer.TYPE;
            case (byte) 5:
                return Long.TYPE;
            case (byte) 6:
                return String.class;
            case (byte) 7:
                return IBinder.class;
            case (byte) 8:
                return byte[].class;
            case (byte) 9:
                return String[].class;
            case (byte) 10:
                return int[].class;
            case (byte) 11:
                return long[].class;
            case (byte) 12:
                return ArrayList.class;
            case (byte) 13:
                return Float.TYPE;
            case (byte) 14:
                return float[].class;
            case (byte) 15:
                return ArgPack.class;
            default:
                return null;
        }
    }

    public static Class<?>[] classArray(byte[] type) {
        if (type == null || type.length == 0) {
            return new Class[0];
        }
        if (type.length == 1 && type[0] == (byte) 0) {
            return new Class[0];
        }
        Class<?>[] c = new Class[type.length];
        for (int i = 0; i < type.length; i++) {
            c[i] = typeClass(type[i]);
        }
        return c;
    }

    public static byte objectType(Object o) {
        if (o == null) {
            return (byte) 1;
        }
        if (o instanceof Boolean) {
            return (byte) 2;
        }
        if (o instanceof Byte) {
            return (byte) 3;
        }
        if (o instanceof Integer) {
            return (byte) 4;
        }
        if (o instanceof Long) {
            return (byte) 5;
        }
        if (o instanceof String) {
            return (byte) 6;
        }
        if (o instanceof IBinder) {
            return (byte) 7;
        }
        if (o instanceof byte[]) {
            return (byte) 8;
        }
        if (o instanceof int[]) {
            return (byte) 10;
        }
        if (o instanceof long[]) {
            return (byte) 11;
        }
        if (o instanceof String[]) {
            return (byte) 9;
        }
        if (o instanceof ArrayList) {
            return (byte) 12;
        }
        if (o instanceof Float) {
            return (byte) 13;
        }
        if (o instanceof float[]) {
            return (byte) 14;
        }
        if (o instanceof ArgPack) {
            return (byte) 15;
        }
        Log.e("ArgPack", "Unsupported type,please support it" + o);
        throw new IllegalArgumentException("Unsupported type ");
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.mTypeArgs);
        if (this.mTypeArgs[0] == (byte) 0) {
            this.mObjectArgs = new Object[0];
            return;
        }
        for (int i = 0; i < this.mTypeArgs.length; i++) {
            switch (this.mTypeArgs[i]) {
                case (byte) 2:
                    dest.writeInt(((Boolean) this.mObjectArgs[i]).booleanValue() ? 1 : 0);
                    break;
                case (byte) 3:
                    dest.writeByte(((Byte) this.mObjectArgs[i]).byteValue());
                    break;
                case (byte) 4:
                    dest.writeInt(((Integer) this.mObjectArgs[i]).intValue());
                    break;
                case (byte) 5:
                    dest.writeLong(((Long) this.mObjectArgs[i]).longValue());
                    break;
                case (byte) 6:
                    dest.writeString((String) this.mObjectArgs[i]);
                    break;
                case (byte) 7:
                    dest.writeStrongBinder((IBinder) this.mObjectArgs[i]);
                    break;
                case (byte) 8:
                    dest.writeByteArray((byte[]) this.mObjectArgs[i]);
                    break;
                case (byte) 9:
                    dest.writeStringArray((String[]) this.mObjectArgs[i]);
                    break;
                case (byte) 10:
                    dest.writeIntArray((int[]) this.mObjectArgs[i]);
                    break;
                case (byte) 11:
                    dest.writeLongArray((long[]) this.mObjectArgs[i]);
                    break;
                case (byte) 12:
                    dest.writeStringList((ArrayList) this.mObjectArgs[i]);
                    break;
                case (byte) 13:
                    dest.writeFloat(((Float) this.mObjectArgs[i]).floatValue());
                    break;
                case (byte) 14:
                    dest.writeFloatArray((float[]) this.mObjectArgs[i]);
                    break;
                case (byte) 15:
                    this.mObjectArgs[i].writeToParcel(dest, flags);
                    break;
                default:
                    break;
            }
        }
    }

    public void readFromParcel(Parcel source) {
        this.mTypeArgs = source.createByteArray();
        if (this.mTypeArgs[0] == (byte) 0) {
            this.mObjectArgs = new Object[0];
            return;
        }
        this.mObjectArgs = new Object[this.mTypeArgs.length];
        for (int i = 0; i < this.mTypeArgs.length; i++) {
            switch (this.mTypeArgs[i]) {
                case (byte) 1:
                    this.mObjectArgs[i] = null;
                    break;
                case (byte) 2:
                    this.mObjectArgs[i] = Boolean.valueOf(source.readInt() == 1);
                    break;
                case (byte) 3:
                    this.mObjectArgs[i] = Byte.valueOf(source.readByte());
                    break;
                case (byte) 4:
                    this.mObjectArgs[i] = Integer.valueOf(source.readInt());
                    break;
                case (byte) 5:
                    this.mObjectArgs[i] = Long.valueOf(source.readLong());
                    break;
                case (byte) 6:
                    this.mObjectArgs[i] = source.readString();
                    break;
                case (byte) 7:
                    this.mObjectArgs[i] = source.readStrongBinder();
                    break;
                case (byte) 8:
                    this.mObjectArgs[i] = source.createByteArray();
                    break;
                case (byte) 9:
                    this.mObjectArgs[i] = source.createStringArray();
                    break;
                case (byte) 10:
                    this.mObjectArgs[i] = source.createIntArray();
                    break;
                case (byte) 11:
                    this.mObjectArgs[i] = source.createLongArray();
                    break;
                case (byte) 12:
                    ArrayList<String> list = new ArrayList();
                    source.readStringList(list);
                    this.mObjectArgs[i] = list;
                    break;
                case (byte) 13:
                    this.mObjectArgs[i] = Float.valueOf(source.readFloat());
                    break;
                case (byte) 14:
                    this.mObjectArgs[i] = source.createFloatArray();
                    break;
                case (byte) 15:
                    ArgPack arg = new ArgPack(new Object[0]);
                    arg.readFromParcel(source);
                    this.mObjectArgs[i] = arg;
                    break;
                default:
                    break;
            }
        }
    }

    public static String toString(byte[] argsType) {
        StringBuilder builder = new StringBuilder(16);
        builder.append("(");
        for (int i = 0; i < argsType.length; i++) {
            if (i != 0) {
                builder.append(",");
            }
            builder.append(typeName(argsType[i]));
            builder.append(" ");
            builder.append(PARAM);
            builder.append(i);
        }
        builder.append(")");
        return builder.toString();
    }

    public String toString() {
        if (isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(16);
        builder.append("[");
        for (int i = 0; i < this.mObjectArgs.length; i++) {
            if (i != 0) {
                builder.append(",");
            }
            builder.append(this.mObjectArgs[i]);
        }
        builder.append("]");
        return builder.toString();
    }

    public int describeContents() {
        return 0;
    }

    public static ArgPack[] createArgArray(Parcel source) {
        int N = source.readInt();
        if (N < 0) {
            return null;
        }
        ArgPack[] val = new ArgPack[N];
        for (int i = 0; i < N; i++) {
            val[i] = createFromParcel(source);
            if (val[i].isEmpty()) {
                val[i] = null;
            }
        }
        return val;
    }

    public static void writeArgArray(ArgPack[] args, Parcel dest) {
        if (args != null) {
            int N = args.length;
            dest.writeInt(N);
            for (int i = 0; i < N; i++) {
                if (args[i] == null) {
                    args[i] = new ArgPack(new Object[0]);
                }
                args[i].writeToParcel(dest, 0);
            }
            return;
        }
        dest.writeInt(-1);
    }

    public static ArgPack createFromParcel(Parcel source) {
        try {
            ArgPack arg = new ArgPack(new Object[0]);
            arg.readFromParcel(source);
            return arg;
        } catch (Exception e) {
            Log.e("ArgPack", "Exception when create ArgPack from parcel : " + e.getMessage());
            return null;
        }
    }
}
