package de.chrgroth.jsonstore.store;

import java.util.Date;

public class JsonStoreMetrics {
    private final String type;
    private final long itemCount;
    private final Date lastModified;
    private final long fileSize;

    public JsonStoreMetrics(String type, long itemCount, Date lastModified, long fileSize) {
        this.type = type;
        this.itemCount = itemCount;
        this.lastModified = lastModified != null ? new Date(lastModified.getTime()) : null;
        this.fileSize = fileSize;
    }

    public String getType() {
        return type;
    }

    public long getItemCount() {
        return itemCount;
    }

    public Date getLastModified() {
        return lastModified != null ? new Date(lastModified.getTime()) : null;
    }

    public long getFileSize() {
        return fileSize;
    }
}
