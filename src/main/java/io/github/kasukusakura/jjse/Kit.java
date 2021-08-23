package io.github.kasukusakura.jjse;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Kit {
    private static final Pattern C_NAME_P =
            Pattern.compile("(class|interface|enum)[ \\r\\t\\n]+([A-Za-z_$][A-Za-z0-9_$]*)");

    static String readAll(Reader reader) throws IOException {
        if (reader == null) return null;
        StringBuilder sb = new StringBuilder(2048);
        char[] buf = new char[2048];
        while (true) {
            int len = reader.read(buf);
            if (len == -1) break;
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }

    public static String findClassName(String script) {
        Matcher matcher = C_NAME_P.matcher(script);
        if (matcher.find()) return matcher.group(2);
        return "";
    }

    public static String jrt() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return runtimeMXBean.getSpecName() + " (" + runtimeMXBean.getSpecVendor() + ", v" + runtimeMXBean.getSpecVersion() + "), " +
                runtimeMXBean.getVmName() + " (" + runtimeMXBean.getVmVendor() + ", build" + runtimeMXBean.getVmVersion() + ")";
    }

    public static <T, T2 extends T> void addAll(
            Collection<T> collection,
            Iterable<T2> iterable
    ) {
        if (iterable instanceof Collection) {
            collection.addAll((Collection<? extends T>) iterable);
        } else {
            for (T2 v : iterable) {
                collection.add(v);
            }
        }
    }

    public static <T> T throwExceptionRT(Throwable throwable) {
        if (throwable == null) throw new NullPointerException();
        if (throwable instanceof RuntimeException) throw (RuntimeException) throwable;
        if (throwable instanceof Error) throw (Error) throwable;
        throw new RuntimeException(throwable);
    }

    public static <T> Collection<T> nonNullC(Collection<T> collection) {
        if (collection == null) return Collections.emptyList();
        return collection;
    }

    public static void transfer(InputStream stream, OutputStream os) throws Throwable {
        byte[] tmp = new byte[20480];
        while (true) {
            int l = stream.read(tmp);
            if (l == -1) break;
            os.write(tmp, 0, l);
        }
    }

    public static void deleteRecurse(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteRecurse(f);
                }
            }
        }
        file.delete();
    }

    public static String toStringOrNull(Object value) {
        return value == null ? null : value.toString();
    }

    public static boolean parseBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return Boolean.parseBoolean(value.toString());
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        if (value instanceof Collection<?>) return !((Collection<?>) value).isEmpty();
        return true;
    }
}
