package io.github.kasukusakura.jjse;

import java.security.ProtectionDomain;
import java.util.Map;

class JJSClassLoader extends ClassLoader {
    private final Map<String, byte[]> codes;
    private final ProtectionDomain domain;

    JJSClassLoader(ClassLoader parent, Map<String, byte[]> codes, ProtectionDomain domain) {
        super(parent);
        this.codes = codes;
        this.domain = domain;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = codes.get(name);
        if (bytes != null)
            return defineClass(name, bytes, 0, bytes.length, domain);
        throw new ClassNotFoundException(name);
    }
}
