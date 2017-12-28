package de.chrgroth.jsonstore.metrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simplified metrics for multiple json store instances.
 *
 * @author Christian Groth
 */
public class JsonStoresMetrics {
    private final long overallItemCount;
    private final long overallStorageSize;
    private final Map<String, JsonStoreMetrics> metrics = new HashMap<>();

    /**
     * Creates new metrics containing all given store metrics.
     *
     * @param metrics
     *            all store metrics to be contained
     */
    public JsonStoresMetrics(List<JsonStoreMetrics> metrics) {
        long items = 0;
        long size = 0;
        if (metrics != null) {
            for (JsonStoreMetrics metric : metrics) {
                this.metrics.put(metric.getUid(), metric);
                items += metric.getItemCount();
                size += metric.getStorageSize();
            }
        }
        overallItemCount = items;
        overallStorageSize = size;
    }

    public long getOverallItemCount() {
        return overallItemCount;
    }

    public long getOverallStorageSize() {
        return overallStorageSize;
    }

    public Map<String, JsonStoreMetrics> getMetrics() {
        return new HashMap<>(metrics);
    }
}
