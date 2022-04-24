package ru.ckateptb.commons.ioc;

import com.google.common.reflect.ClassPath;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.ckateptb.commons.ioc.annotations.*;
import ru.ckateptb.commons.ioc.core.*;
import ru.ckateptb.commons.ioc.exceptions.IoCBeanNotFound;
import ru.ckateptb.commons.ioc.exceptions.IoCCircularDepException;
import ru.ckateptb.commons.ioc.exceptions.IoCException;
import ru.ckateptb.commons.ioc.utils.FinderUtils;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class IoC<T extends IoCHolder> {
    private static final List<BeanRegisterHandler> registerHandlers = new ArrayList<>();
    private static final Map<IoCHolder, IoC<IoCHolder>> instances = new HashMap<>();
    private static final BeanContainer beanContainer = new BeanContainer();
    private static final ImplementationContainer implementationContainer = new ImplementationContainer();
    private static final CircularDetector circularDetector = new CircularDetector();
    private final T holder;
    private final ClassLoader classLoader;
    private final Predicate<String> filter;

    @SuppressWarnings("unchecked")
    public static <T extends IoCHolder> IoC<T> getInstance(T holder) {
        return (IoC<T>) instances.get(holder);
    }

    public static <T> T get(Class<T> clazz) {
        for (IoC<?> ioC : instances.values()) {
            T bean = ioC.getBean(clazz);
            if (bean == null) continue;
            return bean;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, String qualifier) {
        for (IoC<?> ioC : instances.values()) {
            Object bean = null;
            try {
                bean = ioC._getBean(clazz, null, qualifier, false);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException |
                     IoCBeanNotFound | IoCCircularDepException e) {
                e.printStackTrace();
            }
            if (bean == null) continue;
            return (T) bean;
        }
        return null;
    }

    public static <T extends IoCHolder> IoC<T> init(T holder, Object... predefinedBeans) {
        return init(holder, packageName -> true, predefinedBeans);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IoCHolder> IoC<T> init(T holder, Predicate<String> filter, Object... predefinedBeans) {
        try {
            Class<? extends IoCHolder> mainClass = holder.getClass();
            IoC<T> instance = new IoC<>(holder, mainClass.getClassLoader(), filter);
            instance.initWrapper(mainClass, predefinedBeans);
            instances.put(holder, (IoC<IoCHolder>) instance);
            return instance;
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException | IoCBeanNotFound | IoCCircularDepException |
                 URISyntaxException e) {
            throw new IoCException(e);
        }
    }

    public <H> H getBean(Class<H> clazz) {
        try {
            return _getBean(clazz);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException | IoCBeanNotFound | IoCCircularDepException e) {
            throw new IoCException(e);
        }
    }

    private void initWrapper(Class<?> mainClass, Object[] predefinedBeans) throws IOException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException, IoCBeanNotFound, IoCCircularDepException, URISyntaxException {
        if (predefinedBeans != null) {
            Set<Object> beans = Arrays.stream(predefinedBeans).collect(Collectors.toSet());
            beans.add(holder);
            register(beans);
        }
        ComponentScan scan = mainClass.getAnnotation(ComponentScan.class);
        if (scan == null) {
            this.scan(mainClass.getPackage().getName());
        } else {
            this.scan(scan.value());
        }
    }

    @SneakyThrows
    public void scan(String... packages) {
        for (String packageName : packages) {
            init(packageName);
        }
    }

    public void register(Object... beans) {
        for (Object bean : beans) {
            Class<?>[] interfaces = bean.getClass().getInterfaces();
            if (interfaces.length == 0) {
                implementationContainer.putImplementationClass(bean.getClass(), bean.getClass());
            } else {
                for (Class<?> interfaceClass : interfaces) {
                    implementationContainer.putImplementationClass(bean.getClass(), interfaceClass);
                }
            }
            beanContainer.putBean(bean.getClass(), bean);
        }
    }

    private void init(String packageName) throws IOException, InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, IoCBeanNotFound, IoCCircularDepException {
        beanContainer.putBean(IoC.class, this);
        implementationContainer.putImplementationClass(IoC.class, IoC.class);
        Set<Class<?>> classes = ClassPath.from(classLoader).getTopLevelClassesRecursive(packageName).stream()
                .filter(classInfo -> filter.test(classInfo.getName()))
                .map(classInfo -> {
                    try {
                        return classInfo.load();
                    } catch (Exception ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(cl -> cl.isAnnotationPresent(Component.class) || cl.isAnnotationPresent(Configuration.class))
                .collect(Collectors.toSet());
        scanImplementations(classes);
        scanConfigurationClass(classes);
        scanComponentClasses(classes);
    }

    private void scanImplementations(Set<Class<?>> classes) {
        Set<Class<?>> componentClasses = classes.stream().filter(cl -> cl.isAnnotationPresent(Component.class)).collect(Collectors.toSet());
        for (Class<?> implementationClass : componentClasses) {
            Class<?>[] interfaces = implementationClass.getInterfaces();
            if (interfaces.length == 0) {
                implementationContainer.putImplementationClass(implementationClass, implementationClass);
            } else {
                for (Class<?> interfaceClass : interfaces) {
                    implementationContainer.putImplementationClass(implementationClass, interfaceClass);
                }
            }
        }
        Set<Class<?>> configurationClasses = classes.stream().filter(cl -> cl.isAnnotationPresent(Configuration.class)).collect(Collectors.toSet());
        for (Class<?> configurationClass : configurationClasses) {
            Set<Method> methods = FinderUtils.findMethods(configurationClass, Bean.class);
            for (Method method : methods) {
                Class<?> returnType = method.getReturnType();
                implementationContainer.putImplementationClass(returnType, returnType);
            }
        }
    }

    private void scanConfigurationClass(Set<Class<?>> classes) throws IoCCircularDepException, InvocationTargetException,
            IllegalAccessException, InstantiationException, NoSuchMethodException {
        Deque<Class<?>> configurationClassesQ = new ArrayDeque<>(5);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Configuration.class)) {
                configurationClassesQ.add(clazz);
            }
        }
        while (!configurationClassesQ.isEmpty()) {
            Class<?> configurationClass = configurationClassesQ.removeFirst();
            try {
                Object instance = configurationClass.getConstructor().newInstance();
                circularDetector.detect(configurationClass);
                scanConfigurationBeans(configurationClass, instance);
            } catch (IoCBeanNotFound e) {
                configurationClassesQ.addLast(configurationClass);
            }
        }
    }

    private void scanComponentClasses(Set<Class<?>> classes) throws IoCCircularDepException, InvocationTargetException,
            IllegalAccessException, InstantiationException, NoSuchMethodException, IoCBeanNotFound {
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Component.class)) {
                newInstanceWrapper(clazz);
            }
        }
    }

    private void scanConfigurationBeans(Class<?> clazz, Object classInstance) throws InvocationTargetException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, IoCBeanNotFound, IoCCircularDepException {
        Set<Method> methods = FinderUtils.findMethods(clazz, Bean.class);
        Set<Field> fields = FinderUtils.findFields(clazz, Autowired.class);

        for (Field field : fields) {
            String qualifier = field.isAnnotationPresent(Qualifier.class) ? field.getAnnotation(Qualifier.class).value() : null;
            Object fieldInstance = _getBean(field.getType(), field.getName(), qualifier, false);
            field.set(classInstance, fieldInstance);
        }

        for (Method method : methods) {
            Class<?> beanType = method.getReturnType();
            Object beanInstance = method.invoke(classInstance);
            String name = method.getAnnotation(Bean.class).value();
            beanContainer.putBean(beanType, beanInstance, name);
        }
    }

    private Object newInstanceWrapper(Class<?> clazz) throws InvocationTargetException,
            IllegalAccessException, InstantiationException, NoSuchMethodException, IoCBeanNotFound, IoCCircularDepException {
        if (beanContainer.containsBean(clazz)) {
            return beanContainer.getBean(clazz);
        }

        circularDetector.detect(clazz);

        Object instance = newInstance(clazz);
        beanContainer.putBean(clazz, instance);
        fieldInject(clazz, instance);
        setterInject(clazz, instance);
        registerHandlers.forEach(registerHandlers -> registerHandlers.on(instance));
        return instance;
    }

    private Object newInstance(Class<?> clazz) throws IllegalAccessException,
            InstantiationException, InvocationTargetException, NoSuchMethodException,
            IoCBeanNotFound, IoCCircularDepException {
        Constructor<?> defaultConstructor = FinderUtils.findAnnotatedConstructor(clazz, Autowired.class);
        if (defaultConstructor == null) {
            try {
                defaultConstructor = clazz.getConstructors()[0];
            } catch (Throwable throwable) {
                try {
                    defaultConstructor = clazz.getConstructor();
                } catch (NoSuchMethodException e) {
                    throw new IoCException("There is no default constructor in class " + clazz.getName());
                }
            }
            defaultConstructor.setAccessible(true);
        }
        Object[] parameters = getParameters(defaultConstructor.getParameterCount(), defaultConstructor.getParameters(), defaultConstructor.getParameterTypes());
        Object instance = defaultConstructor.newInstance(parameters);
        FinderUtils.findMethods(clazz, PostConstruct.class).forEach(post -> {
            if (post.getParameterCount() > 0) {
                new IoCException("Method " + post + " must not take parameters").printStackTrace();
                return;
            }
            try {
                post.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        return instance;
    }

    private Object[] getParameters(int parameterCount, Parameter[] params, Class<?>[] types) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, IoCBeanNotFound, IoCCircularDepException {
        Object[] parameters = new Object[parameterCount];
        for (int i = 0; i < parameters.length; i++) {
            String qualifier = params[i].isAnnotationPresent(Qualifier.class) ?
                    params[i].getAnnotation(Qualifier.class).value() : null;
            Object depInstance = _getBean(types[i],
                    types[i].getName(), qualifier, true);
            parameters[i] = depInstance;
        }
        return parameters;
    }

    private void setterInject(Class<?> clazz, Object classInstance) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, InstantiationException, IoCBeanNotFound, IoCCircularDepException {
        Set<Method> methods = FinderUtils.findMethods(clazz, Autowired.class);
        for (Method method : methods) {
            Object[] parameters = getParameters(method.getParameterCount(), method.getParameters(), method.getParameterTypes());
            method.invoke(classInstance, parameters);
        }
    }

    private void fieldInject(Class<?> clazz, Object classInstance) throws IllegalAccessException,
            InstantiationException, InvocationTargetException, NoSuchMethodException, IoCBeanNotFound, IoCCircularDepException {
        Set<Field> fields = FinderUtils.findFields(clazz, Autowired.class);
        for (Field field : fields) {
            String qualifier = field.isAnnotationPresent(Qualifier.class) ? field.getAnnotation(Qualifier.class).value() : null;
            Object fieldInstance = _getBean(field.getType(), field.getName(), qualifier, true);
            field.set(classInstance, fieldInstance);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T _getBean(Class<T> interfaceClass) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, IoCBeanNotFound, IoCCircularDepException {
        return (T) _getBean(interfaceClass, null, null, false);
    }

    private <T> Object _getBean(Class<T> interfaceClass, String fieldName, String qualifier, boolean createIfNotFound) throws
            InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            IoCBeanNotFound, IoCCircularDepException {
        Class<?> implementationClass = interfaceClass.isInterface() ?
                implementationContainer.getImplementationClass(interfaceClass, fieldName, qualifier) : interfaceClass;
        if (beanContainer.containsBean(implementationClass)) {
            if (qualifier != null) {
                return beanContainer.getBean(implementationClass, qualifier);
            }
            return beanContainer.getBean(implementationClass);
        }
        if (createIfNotFound) {
            synchronized (beanContainer) {
                return newInstanceWrapper(implementationClass);
            }
        } else {
            throw new IoCBeanNotFound("Cannot found bean for " + interfaceClass.getName());
        }
    }
}
