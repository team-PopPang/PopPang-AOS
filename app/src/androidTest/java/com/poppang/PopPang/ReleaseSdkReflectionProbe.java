package com.poppang.PopPang;

/**
 * Standalone ART regression probe. Run with the minified release DEX on its classpath,
 * without AndroidJUnitRunner or a debug app replacing the release classes.
 * Uses no account data, app storage, or network requests.
 */
public final class ReleaseSdkReflectionProbe {
    public static void main(String[] args) throws Exception {
        String[] enums = {"ApiErrorCause", "AuthErrorCause", "AppsErrorCause", "ClientErrorCause"};
        int checked = 0;
        for (String name : enums) {
            Class<?> type = Class.forName("com.kakao.sdk.common.model." + name);
            Object[] values = type.getEnumConstants();
            if (values == null || values.length == 0) {
                throw new AssertionError("Missing SDK enum constants: " + name);
            }
            for (Object value : values) {
                // This is the same lookup used by Kakao's GenericEnumSerializer.
                String fieldName = ((Enum<?>) value).name();
                java.lang.reflect.Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                if (field.get(null) != value) {
                    throw new AssertionError("Wrong SDK enum field: " + name + "." + fieldName);
                }
                checked++;
            }
        }
        System.out.println("PASS: " + checked + " Kakao SDK enum fields preserve runtime names in release DEX");
    }
}
