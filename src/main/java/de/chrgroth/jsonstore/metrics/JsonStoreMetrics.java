package de.chrgroth.jsonstore.metrics;

import java.util.Date;

/**
 * Simplified metrics for a single json store instance.
 *
 * @author Christian Groth
 */
public class JsonStoreMetrics {

    private final String uid;
    private final long itemCount;
    private final Date lastModified;
    private final long storageSize;

    /**
     * Creates new metrics with the given store values.
     *
     * @param uid
     *            store uid
     * @param itemCount
     *            store item count
     * @param lastModified
     *            store last modified timestamp
     * @param storageSize
     *            store storage size
     */
    public JsonStoreMetrics(String uid, long itemCount, Date lastModified, long storageSize) {
        this.uid = uid;
        this.itemCount = itemCount;
        this.lastModified = lastModified != null ? new Date(lastModified.getTime()) : null;
        this.storageSize = storageSize;
    }

    public String getUid() {
        return uid;
    }

    public long getItemCount() {
        return itemCount;
    }

    public Date getLastModified() {
        return lastModified != null ? new Date(lastModified.getTime()) : null;
    }

    public long getStorageSize() {
        return storageSize;
    }
}
