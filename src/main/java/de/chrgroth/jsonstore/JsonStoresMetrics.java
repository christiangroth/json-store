package de.chrgroth.jsonstore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.chrgroth.jsonstore.store.JsonStoreMetrics;

public class JsonStoresMetrics {
    private final long overallItemCount;
    private final long overallFileSize;
    private final Map<String, JsonStoreMetrics> metrics = new HashMap<>();

    public JsonStoresMetrics(List<JsonStoreMetrics> metrics) {
        long items = 0;
        long size = 0;
        if (metrics != null) {
            for (JsonStoreMetrics metric : metrics) {
                this.metrics.put(metric.getUid(), metric);
                items += metric.getItemCount();
                size += metric.getFileSize();
            }
        }
        overallItemCount = items;
        overallFileSize = size;
    }

    public long getOverallItemCount() {
        return overallItemCount;
    }

    public long getOverallFileSize() {
        return overallFileSize;
    }

    public Map<String, JsonStoreMetrics> getMetrics() {
        return new HashMap<>(metrics);
    }
}
