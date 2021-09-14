package com.tw;

import com.tw.exception.ContainerStartupException;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tw.exception.BusinessExceptionCode.AMBIGUOUS_IMPLEMENTATION_CLASS;
import static com.tw.exception.BusinessExceptionCode.CIRCULAR_REFERENCE;

public class CdiContainer {

    private final Set<Class<?>> clazzSet;
    private final Map<Class<?>, Object> container = new HashMap<>();

    public CdiContainer(Set<Class<?>> clazzSet) {
        this.clazzSet = clazzSet;
    }

    public static CdiContainer initContainer(Class<?> rootClass) {

        String packageName = rootClass.getPackage().getName();

        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));

        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);

        return new CdiContainer(classes);
    }

    public <T> List<T> getBean(Class<T> clazz) {
        return getComponent(clazz, new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getComponent(Class<T> clazz, List<Class<?>> registerClasses) {
        if (container.containsKey(clazz) && Objects.nonNull(clazz.getAnnotation(Singleton.class))) {
            return (List<T>) Collections.singletonList(container.get(clazz));
        }
        if (clazzSet.contains(clazz)) {
            if (clazz.isInterface()) {
                List<Class<T>> implementationClass = new ArrayList<>();
                for (Class<?> classSet : clazzSet) {
                    if (clazz.isAssignableFrom(classSet) && !Objects.equals(classSet, clazz)) {
                        implementationClass.add((Class<T>) classSet);
                    }
                }
                if (CollectionUtils.isEmpty(implementationClass)) {
                    throw new ContainerStartupException("Not found class with " + clazz.getName());
                }
                return implementationClass.stream().map(c -> {
                    T instance = getInstance(c, registerClasses);
                    container.put(instance.getClass(), instance);
                    return instance;
                }).collect(Collectors.toList());
            }
            T instance = getInstance(clazz, registerClasses);
            container.put(instance.getClass(), instance);
            return Collections.singletonList(instance);
        }
        throw new ContainerStartupException("Not found class with " + clazz.getName());
    }

    @SuppressWarnings("unchecked")
    private <T> T getInstance(Class<T> clazz, List<Class<?>> registerClasses) {
        if (registerClasses.contains(clazz)) {
            throw new ContainerStartupException(CIRCULAR_REFERENCE);
        }
        T instance = createInstance(clazz, registerClasses);
        if (Objects.nonNull(instance)) {
            return instance;
        }
        throw new RuntimeException("Class should specify no args constructor or mark inject annotation");
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> clazz, List<Class<?>> registerClasses) {
        registerClasses.add(clazz);
        Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
        return Arrays.stream(constructors)
                .filter(c -> Objects.nonNull(c.getAnnotation(Inject.class)) || Objects.equals(c.getParameterCount(), 0))
                .map(constructor -> {
                    try {
                        if (Objects.nonNull(constructor.getAnnotation(Inject.class))) {
                            return getInstanceByInject(constructor, registerClasses);
                        }
                        return constructor.newInstance();
                    } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).findFirst().orElse(null);
    }

    private <T> T getInstanceByInject(Constructor<T> constructor, List<Class<?>> registerClasses)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (Objects.equals(constructor.getParameterCount(), 0)) {
            return constructor.newInstance();
        }
        return constructor.newInstance(Arrays.stream(constructor.getParameterTypes())
                .map(type -> {
                    List<?> components = getComponent(type, registerClasses);
                    if (components.size() == 1) {
                        return components.get(0);
                    }
                    if (CollectionUtils.isEmpty(components)) {
                        throw new ContainerStartupException("Not found class with " + type.getName());
                    }
                    Optional<Named> annotation = Optional.ofNullable(constructor.getAnnotation(Named.class));
                    annotation.ifPresent(named -> {
                        components.stream().filter(component -> {
                            Named componentNamed = component.getClass().getAnnotation(Named.class);
                            return Objects.nonNull(componentNamed) && Objects.equals(componentNamed.value(), named.value());
                        }).findFirst().orElseThrow(() -> new ContainerStartupException(AMBIGUOUS_IMPLEMENTATION_CLASS));
                    });
                    throw new ContainerStartupException(AMBIGUOUS_IMPLEMENTATION_CLASS);
                }).collect(Collectors.toList()));
    }

}
