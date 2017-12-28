package de.chrgroth.jsonstore;

import com.google.common.base.Strings;

/**
 * Utilities for common store functions.
 *
 * @author chris
 */
public final class JsonStoreUtils {

    private JsonStoreUtils() {

    }

    /**
     * Computes the store uid based on given metadata.
     *
     * @param payloadClass
     *            payload class
     * @param optionalQualifier
     *            optional qualifier in case payload class is used for multiple stores
     * @return store uid
     */
    public static String buildStoreUid(Class<?> payloadClass, String optionalQualifier) {
        return payloadClass.getName() + (Strings.isNullOrEmpty(optionalQualifier) ? "" : "." + optionalQualifier);
    }
}
