package com.github.javaxcel.util;

public final class TypeClassifier {

    private TypeClassifier() {}

    private static final Class<?>[] PRIMITIVE_TYPES = {byte.class, short.class, int.class, long.class, float.class, double.class, char.class, boolean.class};
    private static final Class<?>[] WRAPPER_TYPES = {Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Character.class, Boolean.class};

    /**
     *
     * @param clazz class
     * @return if the class is writable with excel or not.
     */
    public static boolean isWritableClass(Class<?> clazz) {
        return isStringClass(clazz) || isPrimitive(clazz) || isWrapperClass(clazz);
    }

    /**
     * 자료형이 문자열(java.lang.String)인지 확인한다.
     */
    public static boolean isStringClass(Class<?> clazz) {
        return clazz.equals(String.class);
    }

    /**
     * 자료형이 기초형인지 확인한다.
     */
    public static boolean isPrimitive(Class<?> clazz) {
        for (Class<?> type : PRIMITIVE_TYPES) {
            if (clazz.equals(type)) return true;
        }

        return false;
    }

    /**
     * 자료형이 래퍼클래스인지 확인한다.
     */
    public static boolean isWrapperClass(Class<?> clazz) {
        for (Class<?> type : WRAPPER_TYPES) {
            if (clazz.equals(type)) return true;
        }

        return false;
    }

}
