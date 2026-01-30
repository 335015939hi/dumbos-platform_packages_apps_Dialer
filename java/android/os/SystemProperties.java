package android.os;

import java.lang.reflect.Method;

// Wrapper for hidden SystemProperties API using reflection
// TODO: This is a compatibility layer for AOSP-to-Gradle migration
public class SystemProperties {
    private static final String TAG = "SystemProperties";

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String def) {
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            Method getString = SystemProperties.getMethod("get", String.class, String.class);
            return (String) getString.invoke(null, key, def);
        } catch (Exception e) {
            return def;
        }
    }

    public static int getInt(String key, int def) {
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            Method getInt = SystemProperties.getMethod("getInt", String.class, int.class);
            return (Integer) getInt.invoke(null, key, def);
        } catch (Exception e) {
            return def;
        }
    }

    public static long getLong(String key, long def) {
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            Method getLong = SystemProperties.getMethod("getLong", String.class, long.class);
            return (Long) getLong.invoke(null, key, def);
        } catch (Exception e) {
            return def;
        }
    }

    public static boolean getBoolean(String key, boolean def) {
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            Method getBoolean = SystemProperties.getMethod("getBoolean", String.class, boolean.class);
            return (Boolean) getBoolean.invoke(null, key, def);
        } catch (Exception e) {
            return def;
        }
    }

    public static void set(String key, String val) {
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            Method set = SystemProperties.getMethod("set", String.class, String.class);
            set.invoke(null, key, val);
        } catch (Exception e) {
            // Silently fail
        }
    }
}
