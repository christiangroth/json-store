package de.chrgroth.jsonstore.store;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.chrgroth.jsonstore.json.FlexjsonHelper;
import flexjson.JSONDeserializer;
import flexjson.JSONTokener;
import flexjson.JsonNumber;
import flexjson.ObjectBinder;

/**
 * Represents a JSON store for a concrete class. Access is provided using delegate methods to Java built in stream API. You may use flexjson
 * annotations to control conversion from/to JSON.
 * 
 * @author Christian Groth
 * @param <T>
 *            concrete type stored in this instance
 * @param
 * 				<P>
 *            concrete type structure used for storage of instances of type T
 */
public abstract class AbstractJsonStore<T, P> {
		private static final Logger LOG = LoggerFactory.getLogger(AbstractJsonStore.class);
		
		public static final String FILE_SEPARATOR = ".";
		public static final String FILE_PREFIX = "storage";
		public static final String FILE_SUFFIX = "json";
		
		private static final String JSON_FIELD_PAYLOAD = "payload";
		private static final String JSON_FIELD_PAYLOAD_TYPE_VERSION = "payloadTypeVersion";
		
		protected final FlexjsonHelper flexjsonHelper;
		protected JsonStoreMetadata<T, P> metadata;
		protected final File file;
		protected final Charset charset;
		protected final boolean prettyPrint;
		protected final boolean autoSave;
		protected final Map<Integer, VersionMigrationHandler> migrationHandlers;
		
		protected AbstractJsonStore(Class<T> payloadClass, Integer payloadTypeVersion, boolean singleton, String dateTimePattern, File storage, Charset charset, boolean prettyPrint, boolean autoSave,
				VersionMigrationHandler... migrationHandlers) {
			this(payloadClass, payloadTypeVersion, singleton, dateTimePattern, storage, charset, "", prettyPrint, autoSave, migrationHandlers);
		}
		
		protected AbstractJsonStore(Class<T> payloadClass, Integer payloadTypeVersion, boolean singleton, String dateTimePattern, File storage, Charset charset, String fileNameExtraPrefix, boolean prettyPrint, boolean autoSave,
				VersionMigrationHandler... migrationHandlers) {
			flexjsonHelper = new FlexjsonHelper(dateTimePattern);
			metadata = new JsonStoreMetadata<>();
			metadata.setPayloadType(payloadClass.getName());
			metadata.setPayloadTypeVersion(payloadTypeVersion);
			metadata.setSingleton(singleton);
			Date now = new Date();
			metadata.setCreated(now);
			metadata.setModified(now);
			this.file = storage != null ? new File(storage, FILE_PREFIX + FILE_SEPARATOR + fileNameExtraPrefix + payloadClass.getName() + FILE_SEPARATOR + FILE_SUFFIX) : null;
			this.charset = charset;
			this.prettyPrint = prettyPrint;
			this.autoSave = autoSave;
			this.migrationHandlers = new HashMap<>();
			if (migrationHandlers != null) {
				for (VersionMigrationHandler migrationHandler : migrationHandlers) {
						this.migrationHandlers.put(migrationHandler.sourceVersion(), migrationHandler);
				}
			}
		}
		
		/**
		 * Returns file used for storage.
		 * 
		 * @return file, may be null
		 */
		public final File getFile() {
			return file;
		}
		
		/**
		 * Checks if store is persistent.
		 * 
		 * @return true if store is persistent, false otherwise
		 */
		public final boolean isPersistent() {
			return file != null;
		}
		
		/**
		 * Saves all data contained in store to configured file. No action if store is not persistent.
		 */
		public final void save() {
			
			// abort on transient stores
			if (!isPersistent()) {
				return;
			}
			
			// create JSON
			String json = toJson();
			
			// write to file
			try {
				synchronized (file) {
						Files.write(file.toPath(), Arrays.asList(json), charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
				}
			} catch (IOException e) {
				LOG.error("Unable to write file content, skipping file during store: " + file.getAbsolutePath() + "!!", e);
			}
		}
		
		/**
		 * Returns store elements in JSON format.
		 * 
		 * @return JSON data
		 */
		public final String toJson() {
			return toJson(false);
		}
		
		/**
		 * Creates a copy of stored data in JSON format with given pretty-print mode.
		 * 
		 * @param prettyPrint
		 *            pretty-print mode
		 * @return JSON data
		 */
		public final String toJson(boolean prettyPrint) {
			
			// update metadata
			metadata.setModified(new Date());
			
			// create json data
			return flexjsonHelper.serializer(prettyPrint).serialize(metadata);
		}
		
		/**
		 * Loads store elements from configure file.
		 */
		public final void load() {
			
			// abort on transient stores
			if (!isPersistent()) {
				return;
			}
			
			// load JSON
			String json = null;
			try {
				synchronized (file) {
						json = Files.lines(file.toPath(), charset).parallel().filter(line -> line != null && !"".equals(line.trim())).map(String::trim).collect(Collectors.joining());
				}
			} catch (Exception e) {
				LOG.error("Unable to read file content, skipping file during restore: " + file.getAbsolutePath() + "!!", e);
			}
			
			// recreate data
			fromJson(json, false);
		}
		
		/**
		 * Creates store elements from given JSON data and replaces all store contents.Will invoke {@link #save()} if using auto-save mode.
		 * 
		 * @param json
		 *            JSON data
		 */
		public final void fromJson(String json) {
			fromJson(json, true);
		}
		
		@SuppressWarnings("unchecked")
		private void fromJson(String json, boolean explicitSave) {
			
			// null guard
			if (json == null || "".equals(json.trim())) {
				return;
			}
			
			// deserialize to generic structure
			Object genericStructureRaw = new JSONTokener(json).nextValue();
			if (genericStructureRaw instanceof Map) {
				
				// compare version information
				Map<String, Object> genericStructure = (Map<String, Object>) genericStructureRaw;
				Object topLevelTypeVersionRaw = genericStructure.get(JSON_FIELD_PAYLOAD_TYPE_VERSION);
				Integer topLevelTypeVersion = topLevelTypeVersionRaw != null ? ((JsonNumber) topLevelTypeVersionRaw).toInteger() : null;
				Integer payloadTypeVersion = metadata.getPayloadTypeVersion();
				if (topLevelTypeVersion != null & payloadTypeVersion != null) {
						
						// abort on newer version than available as code
						if (topLevelTypeVersion > payloadTypeVersion) {
							throw new IllegalStateException("loaded version is newer than specified version in code: " + topLevelTypeVersion + " > " + payloadTypeVersion + "!!");
						}
						
						// run all available version migrators
						if (topLevelTypeVersion < payloadTypeVersion) {
							
							// get payload
							Object genericStructurePayload = genericStructure.get(JSON_FIELD_PAYLOAD);
							if (genericStructurePayload != null) {
								
								// update per version
								for (int i = topLevelTypeVersion; i <= payloadTypeVersion; i++) {
										
										// check for migration handler
										VersionMigrationHandler migrationHandler = migrationHandlers.get(i);
										if (migrationHandler == null) {
											continue;
										}
										
										// invoke handler per instance, so you don't have to deal with wrapping list by yourself
										try {
											if (genericStructurePayload instanceof List<?>) {
												
												// might be a non singleton store
												for (Object genericStructurePayloadItem : (List<Object>) genericStructurePayload) {
														migrationHandler.migrate((Map<String, Object>) genericStructurePayloadItem);
												}
											} else {
												
												// might be a singleton store
												migrationHandler.migrate((Map<String, Object>) genericStructurePayload);
											}
										} catch (Exception e) {
											throw new IllegalStateException("faild to migrate " + metadata.getPayloadType() + "from version " + i + " to " + (i + 1) + ": " + e.getMessage() + "!!", e);
										}
								}
								
								// save migrated data, if auto save is enabled
								if (autoSave) {
										save();
								}
							}
						}
				}
				
				// proceed with deserialization to metadata using correct version
				try {
						// TODO this is a bad hack for the moment!!
						@SuppressWarnings("rawtypes")
						JSONDeserializer<JsonStoreMetadata> deserializer = flexjsonHelper.deserializer(JsonStoreMetadata.class);
						Method method = deserializer.getClass().getDeclaredMethod("createObjectBinder");
						method.setAccessible(true);
						ObjectBinder binder = (ObjectBinder) method.invoke(deserializer);
						
						// proceed deserialization
						metadata = (JsonStoreMetadata<T, P>) binder.bind(genericStructure);
				} catch (Exception e) {
						LOG.error("Unable to restore from JSON content, skipping file during restore: " + file.getAbsolutePath() + "!!", e);
				} finally {
						
						// avoid null metadata
						if (metadata == null) {
							metadata = new JsonStoreMetadata<>();
						}
				}
			} else {
				
				// update metadata to contain payload
				Date now = new Date();
				metadata.setPayload((P) genericStructureRaw);
				metadata.setCreated(now);
				metadata.setModified(now);
			}
			
			// metadata refresh callback
			metadataRefreshed();
			
			// save
			if (metadata != null && explicitSave && autoSave) {
				save();
			}
		}
		
		/**
		 * Gets called after metadata was refreshed on loading new JSON data.
		 */
		protected abstract void metadataRefreshed();
		
		/**
		 * Drops store file explicitly. Transient data in store remains unchanged.
		 */
		public final void drop() {
			if (isPersistent()) {
				try {
						synchronized (file) {
							Files.deleteIfExists(file.toPath());
						}
				} catch (IOException e) {
						LOG.error("Unable to delete persistent JSON store: " + file.getAbsolutePath() + "!!", e);
				}
			}
		}
}
