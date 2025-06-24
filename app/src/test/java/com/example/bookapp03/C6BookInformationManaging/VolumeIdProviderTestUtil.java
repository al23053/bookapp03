package com.example.bookapp03.C6BookInformationManaging;

import java.lang.reflect.Field;

public final class VolumeIdProviderTestUtil {
    public static void overrideBaseUrl(String url) {
        try {
            Field f = VolumeIdProvider.class.getDeclaredField("BASE_URL");
            f.setAccessible(true);
            Field mod = Field.class.getDeclaredField("modifiers");
            mod.setAccessible(true);
            mod.setInt(f, f.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
            f.set(null, url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
