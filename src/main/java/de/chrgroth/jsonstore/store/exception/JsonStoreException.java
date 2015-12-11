package de.chrgroth.jsonstore.store.exception;

import de.chrgroth.jsonstore.store.AbstractJsonStore;

/**
 * Custom exception indicating datal errors during data handling in an instance of {@link AbstractJsonStore} or subtypes.
 *
 * @author chris
 */
public class JsonStoreException extends RuntimeException {
    private static final long serialVersionUID = -3791619692782895072L;

    public JsonStoreException(String msg) {
        super(msg);
    }

    public JsonStoreException(String msg, Exception e) {
        super(msg, e);
    }
}
