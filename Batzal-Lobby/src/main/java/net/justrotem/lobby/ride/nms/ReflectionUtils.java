package net.justrotem.lobby.ride.nms;

import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class ReflectionUtils {

    public static void setField(Class<?> targetClass, Object targetInstance, Object value) throws IllegalAccessException, NoSuchFieldException {
        Field field = getField(targetClass, value.getClass());
        setField(field, targetInstance, value);
    }

    public static void setField(String fieldName, Class<?> targetClass, Object targetInstance, Object value) throws IllegalAccessException, NoSuchFieldException {
        Field field = targetClass.getDeclaredField(fieldName);
        setField(field, targetInstance, value);
    }

    private static void setField(Field field, Object targetInstance, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(targetInstance, value);
    }

    public static Field getField(Class<?> targetClass, Class<?> fieldClass) throws NoSuchFieldException {
        List<Field> fields = Arrays.stream(targetClass.getDeclaredFields()).filter(
                f -> MethodType.methodType(f.getType()).wrap().returnType().equals(fieldClass)
        ).toList();
        if (fields.size() != 1) throw new NoSuchFieldException("Fields found: " + fields);
        fields.getFirst().setAccessible(true);
        return fields.getFirst();
    }

    public static Method getMethod(Class<?> targetClass, Class<?> returnType, Class<?>... paramTypes) throws NoSuchMethodException {
        List<Method> methods = Arrays.stream(targetClass.getDeclaredMethods())
                .filter(m -> m.getParameterTypes().length == paramTypes.length &&
                        m.getReturnType() == returnType &&
                        IntStream.range(0, paramTypes.length)
                                .allMatch(i -> m.getParameterTypes()[i].isAssignableFrom(paramTypes[i])))
                .toList();
        if (methods.size() != 1) throw new NoSuchMethodException("Methods found: " + methods);

        methods.getFirst().setAccessible(true);
        return methods.getFirst();
    }

    public static Method getMethod(String methodName, Class<?> targetClass, Class<?>... paramTypes) throws NoSuchMethodException {
        Method method = targetClass.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method;
    }
}
