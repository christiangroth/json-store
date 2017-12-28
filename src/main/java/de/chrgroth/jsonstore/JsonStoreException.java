package de.chrgroth.jsonstore;

/**
 * Custom exception indicating errors during handling an instance of {@link AbstractJsonStore} or subtypes.
 *
 * @author Christian Groth
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
