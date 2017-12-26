package de.chrgroth.jsonstore.store;

import com.google.common.base.Strings;

public final class JsonStoreUtils {

    private JsonStoreUtils() {

    }

    public static String buildStoreUid(Class<?> payloadClass, String optionalQualifier) {
        return payloadClass.getName() + (Strings.isNullOrEmpty(optionalQualifier) ? "" : "." + optionalQualifier);
    }
}
