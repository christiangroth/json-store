package de.chrgroth.jsonstore.store;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.chrgroth.jsonstore.json.FlexjsonHelper;
import flexjson.JSONDeserializer;
import flexjson.JSONTokener;
import flexjson.ObjectBinder;

/**
 * Represents a JSON store for a concrete class. Access is provided using delegate methods to Java built in stream API. You may use flexjson
 * annotations to control conversion from/to JSON.
 * 
 * @author Christian Groth
 * @param <T>
 *          concrete type stored in this instance
 * @param concrete
 *          type structure used for storage of instances of type T
 */
// TODO test migration from non metadata to metadata
// TODO test migrating versions
// TODO update docs
// TODO update release notes
public abstract class AbstractJsonStore<T, P> {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractJsonStore.class);
	
	public static final String FILE_SEPARATOR = ".";
	public static final String FILE_PREFIX = "storage";
	public static final String FILE_SUFFIX = "json";
	
	protected final FlexjsonHelper flexjsonHelper;
	protected JsonStoreMetadata<T, P> metadata;
	protected final File file;
	protected final Charset charset;
	protected final boolean prettyPrint;
	protected final boolean autoSave;
	
	protected AbstractJsonStore(Class<T> payloadClass, Integer payloadTypeVersion, boolean singleton, String dateTimePattern, File storage, Charset charset, boolean prettyPrint, boolean autoSave) {
		this(payloadClass, payloadTypeVersion, singleton, dateTimePattern, storage, charset, "", prettyPrint, autoSave);
	}
	
	protected AbstractJsonStore(Class<T> payloadClass, Integer payloadTypeVersion, boolean singleton, String dateTimePattern, File storage, Charset charset, String fileNameExtraPrefix, boolean prettyPrint, boolean autoSave) {
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
	}
	
	/**
	 * Returns stores metadata.
	 * 
	 * @return JSON store metadata
	 */
	public JsonStoreMetadata<T, P> getMetadata() {
		return metadata;
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
	 *          pretty-print mode
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
	 *          JSON data
	 */
	public final void fromJson(String json) {
		fromJson(json, true);
	}
	
	// TODO define constants
	@SuppressWarnings("unchecked")
	private void fromJson(String json, boolean explicitSave) {
		
		// null guard
		if (json == null || "".equals(json.trim())) {
		return;
		}
		
		// deserialize to generic structure
		Map<String, Object> genericStructure = (Map<String, Object>) new JSONTokener(json).nextValue();
		Object topLevelType = genericStructure.get("class");
		if (!JsonStoreMetadata.class.getName().equals(topLevelType)) {
		
		// TODO load data the old way, and wrap to new metadata object
		throw new IllegalStateException("json data is not wrapped in json store metadata and thus can't be loaded!!");
		} else {
		
		// compare version information
		Integer topLevelTypeVersion = (Integer) genericStructure.get("payloadTypeVersion");
		Integer payloadTypeVersion = metadata.getPayloadTypeVersion();
		if (topLevelTypeVersion != null & payloadTypeVersion != null) {
			
			// abort on newer version than available as code
			if (topLevelTypeVersion > payloadTypeVersion) {
				// TODO crash somehow
				throw new IllegalStateException("loaded version is newer than specified version in code: " + topLevelTypeVersion + " > " + payloadTypeVersion + "!!");
			}
			
			// TODO 4. run all available version migrators and update metadata accordingly
			if (topLevelTypeVersion < payloadTypeVersion) {
				// rawData.put("class", TestDataParentNewVersion.class.getName()); // FAKED same cluss but different structure in production
				// environments
				// rawData.put("id", "CHANGED");
				// rawData.put("newProperty", "addedValue");
				// rawData.remove("bools");
			}
		}
		}
		
		// proceed with deserialization to metadata with correvt version
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
