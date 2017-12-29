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
    public static String buildUid(Class<?> payloadClass, String optionalQualifier) {

        // null guard
        if (payloadClass == null) {
            throw new JsonStoreException("payload class must not be null!!");
        }

        // compute qualifierAppendix
        String appendix = "";
        if (optionalQualifier != null && !Strings.isNullOrEmpty(optionalQualifier.trim())) {
            appendix = "." + optionalQualifier.replaceAll("\\s", "");
        }

        // done
        return payloadClass.getName() + appendix;
    }
}
