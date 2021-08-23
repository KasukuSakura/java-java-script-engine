package io.github.kasukusakura.jjse;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class JCI_JC_ClassSolve {
    Map<String, Iterable<JavaFileObject>> files = new HashMap<>();
    Map<String, Iterable<JavaFileObject>> filesRec = new HashMap<>();
    List<ZipFile> zips = new ArrayList<>();


    Iterable<JavaFileObject> findAll(String pkg, boolean recurse) {
        return (recurse ? filesRec : files)
                .computeIfAbsent(pkg, k -> calc(k, recurse));
    }

    private Iterable<JavaFileObject> calc(String s, boolean recurse) {
        List<JavaFileObject> rsp = new ArrayList<>();
        String zipPath = s.replace('.', '/') + '/';
        for (ZipFile zipFile : zips) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                if (entryName.isEmpty()) continue;
                if (entryName.charAt(entryName.length() - 1) == '/') continue;
                if (!entryName.startsWith(zipPath)) continue;
                if (!entryName.endsWith(".class")) continue;
                if (!recurse && entryName.indexOf('/', zipPath.length()) != -1) continue;
                rsp.add(new JFOinJar(zipFile, zipEntry));
            }
        }
        return rsp;
    }

    static class JFOinJar extends SimpleJavaFileObject {
        private final ZipFile zip;
        private final ZipEntry entry;
        final String binName;

        public JFOinJar(ZipFile zipFile, ZipEntry zipEntry) {
            super(URI.create("cinjar:/" + zipEntry.getName()), Kind.CLASS);
            this.zip = zipFile;
            this.entry = zipEntry;
            String cn = zipEntry.getName().substring(0, zipEntry.getName().length() - 6).replace('/', '.');
            while (!cn.isEmpty() && cn.charAt(0) == '.') {
                cn = cn.substring(1);
            }
            binName = cn;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return zip.getInputStream(entry);
        }
    }

    void open(String path) {
        try {
            zips.add(new ZipFile(path));
        } catch (IOException ignored) {
        }
    }

    void close() {
        for (ZipFile zf : zips) {
            try {
                zf.close();
            } catch (IOException ignored) {
            }
        }
    }
}
