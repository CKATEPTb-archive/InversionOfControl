package ru.ckateptb.commons.ioc.guava.reflect;


import ru.ckateptb.commons.ioc.guava.base.Preconditions;
import ru.ckateptb.commons.ioc.guava.base.Splitter;
import ru.ckateptb.commons.ioc.guava.collect.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static ru.ckateptb.commons.ioc.guava.base.StandardSystemProperty.JAVA_CLASS_PATH;
import static ru.ckateptb.commons.ioc.guava.base.StandardSystemProperty.PATH_SEPARATOR;

public final class ClassPath {
    private static final Splitter CLASS_PATH_ATTRIBUTE_SEPARATOR = Splitter.on(" ").omitEmptyStrings();
    private final ImmutableSet<ResourceInfo> resources;

    private ClassPath(ImmutableSet<ResourceInfo> resources) {
        this.resources = resources;
    }

    public static ClassPath from(ClassLoader classloader) throws IOException {
        ImmutableSet<LocationInfo> locations = locationsFrom(classloader);
        Set<File> scanned = new HashSet();
        UnmodifiableIterator var3 = locations.iterator();

        while(var3.hasNext()) {
            LocationInfo location = (LocationInfo)var3.next();
            scanned.add(location.file());
        }

        ImmutableSet.Builder<ResourceInfo> builder = ImmutableSet.builder();
        UnmodifiableIterator var7 = locations.iterator();

        while(var7.hasNext()) {
            LocationInfo location = (LocationInfo)var7.next();
            builder.addAll(location.scanResources(scanned));
        }

        return new ClassPath(builder.build());
    }

    public ImmutableSet<ClassInfo> getTopLevelClasses() {
        return FluentIterable.from(this.resources).filter(ClassInfo.class).filter(ClassInfo::isTopLevel).toSet();
    }

    public ImmutableSet<ClassInfo> getTopLevelClassesRecursive(String packageName) {
        Preconditions.checkNotNull(packageName);
        String packagePrefix = (new StringBuilder(1 + String.valueOf(packageName).length())).append(packageName).append('.').toString();
        ImmutableSet.Builder<ClassInfo> builder = ImmutableSet.builder();
        UnmodifiableIterator var4 = this.getTopLevelClasses().iterator();

        while(var4.hasNext()) {
            ClassInfo classInfo = (ClassInfo)var4.next();
            if (classInfo.getName().startsWith(packagePrefix)) {
                builder.add(classInfo);
            }
        }

        return builder.build();
    }

    static ImmutableSet<ClassPath.LocationInfo> locationsFrom(ClassLoader classloader) {
        ImmutableSet.Builder<ClassPath.LocationInfo> builder = ImmutableSet.builder();
        for (Map.Entry<File, ClassLoader> entry : getClassPathEntries(classloader).entrySet()) {
            builder.add(new ClassPath.LocationInfo(entry.getKey(), entry.getValue()));
        }
        return builder.build();
    }

    static ImmutableSet<File> getClassPathFromManifest(
            File jarFile, Manifest manifest) {
        if (manifest == null) {
            return ImmutableSet.of();
        }
        ImmutableSet.Builder<File> builder = ImmutableSet.builder();
        String classpathAttribute =
                manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH.toString());
        if (classpathAttribute != null) {
            for (String path : CLASS_PATH_ATTRIBUTE_SEPARATOR.split(classpathAttribute)) {
                URL url;
                try {
                    url = getClassPathEntry(jarFile, path);
                } catch (MalformedURLException e) {
                    // Ignore bad entry
                    continue;
                }
                if (url.getProtocol().equals("file")) {
                    builder.add(toFile(url));
                }
            }
        }
        return builder.build();
    }

    static ImmutableMap<File, ClassLoader> getClassPathEntries(ClassLoader classloader) {
        LinkedHashMap<File, ClassLoader> entries = Maps.newLinkedHashMap();
        ClassLoader parent = classloader.getParent();
        if (parent != null) {
            entries.putAll(getClassPathEntries(parent));
        }

        UnmodifiableIterator var3 = getClassLoaderUrls(classloader).iterator();

        while(var3.hasNext()) {
            URL url = (URL)var3.next();
            if (url.getProtocol().equals("file")) {
                File file = toFile(url);
                if (!entries.containsKey(file)) {
                    entries.put(file, classloader);
                }
            }
        }

        return ImmutableMap.copyOf(entries);
    }

    private static ImmutableList<URL> getClassLoaderUrls(ClassLoader classloader) {
        if (classloader instanceof URLClassLoader) {
            return ImmutableList.copyOf(((URLClassLoader)classloader).getURLs());
        } else {
            return classloader.equals(ClassLoader.getSystemClassLoader()) ? parseJavaClassPath() : ImmutableList.of();
        }
    }

    static ImmutableList<URL> parseJavaClassPath() {
        ImmutableList.Builder<URL> urls = ImmutableList.builder();
        for (String entry : Splitter.on(PATH_SEPARATOR.value()).split(JAVA_CLASS_PATH.value())) {
            try {
                try {
                    urls.add(new File(entry).toURI().toURL());
                } catch (SecurityException e) { // File.toURI checks to see if the file is a directory
                    urls.add(new URL("file", null, new File(entry).getAbsolutePath()));
                }
            } catch (MalformedURLException e) {
            }
        }
        return urls.build();
    }

    static URL getClassPathEntry(File jarFile, String path) throws MalformedURLException {
        return new URL(jarFile.toURI().toURL(), path);
    }

    static String getClassName(String filename) {
        int classNameEnd = filename.length() - ".class".length();
        return filename.substring(0, classNameEnd).replace('/', '.');
    }

    static File toFile(URL url) {
        Preconditions.checkArgument(url.getProtocol().equals("file"));

        try {
            return new File(url.toURI());
        } catch (URISyntaxException var2) {
            return new File(url.getPath());
        }
    }

    static final class LocationInfo {
        final File home;
        private final ClassLoader classloader;

        LocationInfo(File home, ClassLoader classloader) {
            this.home = (File)Preconditions.checkNotNull(home);
            this.classloader = (ClassLoader)Preconditions.checkNotNull(classloader);
        }

        public final File file() {
            return this.home;
        }

        public ImmutableSet<ResourceInfo> scanResources(Set<File> scannedFiles) throws IOException {
            ImmutableSet.Builder<ResourceInfo> builder = ImmutableSet.builder();
            scannedFiles.add(this.home);
            this.scan(this.home, scannedFiles, builder);
            return builder.build();
        }

        private void scan(File file, Set<File> scannedUris, ImmutableSet.Builder<ResourceInfo> builder) throws IOException {
            try {
                if (!file.exists()) {
                    return;
                }
            } catch (SecurityException var7) {
                return;
            }

            if (file.isDirectory()) {
                this.scanDirectory(file, builder);
            } else {
                this.scanJar(file, scannedUris, builder);
            }

        }

        private void scanJar(File file, Set<File> scannedUris, ImmutableSet.Builder<ResourceInfo> builder) throws IOException {
            JarFile jarFile;
            try {
                jarFile = new JarFile(file);
            } catch (IOException var14) {
                return;
            }

            try {
                UnmodifiableIterator var5 = ClassPath.getClassPathFromManifest(file, jarFile.getManifest()).iterator();

                while(var5.hasNext()) {
                    File path = (File)var5.next();
                    if (scannedUris.add(path.getCanonicalFile())) {
                        this.scan(path, scannedUris, builder);
                    }
                }

                this.scanJarFile(jarFile, builder);
            } finally {
                try {
                    jarFile.close();
                } catch (IOException var13) {
                }

            }

        }

        private void scanJarFile(JarFile file, ImmutableSet.Builder<ResourceInfo> builder) {
            Enumeration<JarEntry> entries = file.entries();

            while(entries.hasMoreElements()) {
                JarEntry entry = (JarEntry)entries.nextElement();
                if (!entry.isDirectory() && !entry.getName().equals("META-INF/MANIFEST.MF")) {
                    builder.add(ClassPath.ResourceInfo.of(new File(file.getName()), entry.getName(), this.classloader));
                }
            }

        }

        private void scanDirectory(File directory, ImmutableSet.Builder<ResourceInfo> builder) throws IOException {
            Set<File> currentPath = new HashSet();
            currentPath.add(directory.getCanonicalFile());
            this.scanDirectory(directory, "", currentPath, builder);
        }

        private void scanDirectory(
                File directory,
                String packagePrefix,
                Set<File> currentPath,
                ImmutableSet.Builder<ClassPath.ResourceInfo> builder)
                throws IOException {
            File[] files = directory.listFiles();
            if (files == null) {
                // IO error, just skip the directory
                return;
            }
            for (File f : files) {
                String name = f.getName();
                if (f.isDirectory()) {
                    File deref = f.getCanonicalFile();
                    if (currentPath.add(deref)) {
                        scanDirectory(deref, packagePrefix + name + "/", currentPath, builder);
                        currentPath.remove(deref);
                    }
                } else {
                    String resourceName = packagePrefix + name;
                    if (!resourceName.equals(JarFile.MANIFEST_NAME)) {
                        builder.add(ClassPath.ResourceInfo.of(f, resourceName, classloader));
                    }
                }
            }
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof LocationInfo)) {
                return false;
            } else {
                LocationInfo that = (LocationInfo)obj;
                return this.home.equals(that.home) && this.classloader.equals(that.classloader);
            }
        }

        public int hashCode() {
            return this.home.hashCode();
        }

        public String toString() {
            return this.home.toString();
        }
    }

    public static final class ClassInfo extends ResourceInfo {
        private final String className;

        ClassInfo(File file, String resourceName, ClassLoader loader) {
            super(file, resourceName, loader);
            this.className = ClassPath.getClassName(resourceName);
        }

        public String getName() {
            return this.className;
        }

        public boolean isTopLevel() {
            return this.className.indexOf(36) == -1;
        }

        public Class<?> load() {
            try {
                return this.loader.loadClass(this.className);
            } catch (ClassNotFoundException var2) {
                throw new IllegalStateException(var2);
            }
        }

        public String toString() {
            return this.className;
        }
    }

    public static class ResourceInfo {
        private final File file;
        private final String resourceName;
        final ClassLoader loader;

        static ResourceInfo of(File file, String resourceName, ClassLoader loader) {
            return (ResourceInfo)(resourceName.endsWith(".class") ? new ClassInfo(file, resourceName, loader) : new ResourceInfo(file, resourceName, loader));
        }

        ResourceInfo(File file, String resourceName, ClassLoader loader) {
            this.file = (File) Preconditions.checkNotNull(file);
            this.resourceName = (String)Preconditions.checkNotNull(resourceName);
            this.loader = (ClassLoader)Preconditions.checkNotNull(loader);
        }

        public int hashCode() {
            return this.resourceName.hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ResourceInfo)) {
                return false;
            } else {
                ResourceInfo that = (ResourceInfo)obj;
                return this.resourceName.equals(that.resourceName) && this.loader == that.loader;
            }
        }

        public String toString() {
            return this.resourceName;
        }
    }
}

