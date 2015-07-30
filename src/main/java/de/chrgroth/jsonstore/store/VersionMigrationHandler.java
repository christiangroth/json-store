package de.chrgroth.jsonstore.store;

import java.util.Map;

// TODO comment
public interface VersionMigrationHandler {
	
	int sourceVersion();
	
	int targetVersion();
	
	void migrate(Map<String, Object> genericPayload);
}
