package com.github.christiangroth.jsonstore.store;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.christiangroth.jsonstore.json.FlexjsonHelper;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Represents a JSON store for a concrete class. Access is provided using delegate methods to Java built in stream API. You may use flexjson
 * annotations to control conversion from/to JSON.
 * 
 * @author Christian Groth
 * @param <T>
 *            concrete type stored in this instance
 */
public abstract class AbstractJsonStore<T> {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractJsonStore.class);
	
	public static final String FILE_SEPARATOR = ".";
	public static final String FILE_PREFIX = "storage";
	public static final String FILE_SUFFIX = "json";
	
	protected final FlexjsonHelper flexjsonHelper;
	protected final Class<T> dataClass;
	protected final File file;
	protected final Charset charset;
	protected final boolean prettyPrint;
	protected final boolean autoSave;
	
	protected AbstractJsonStore(Class<T> dataClass, String dateTimePattern, File storage, Charset charset, boolean prettyPrint, boolean autoSave) {
		this(dataClass, dateTimePattern, storage, charset, "", prettyPrint, autoSave);
	}
	
	protected AbstractJsonStore(Class<T> dataClass, String dateTimePattern, File storage, Charset charset, String fileNameExtraPrefix, boolean prettyPrint, boolean autoSave) {
		this.dataClass = dataClass;
		flexjsonHelper = new FlexjsonHelper(dateTimePattern);
		this.file = storage != null ? new File(storage, FILE_PREFIX + FILE_SEPARATOR + fileNameExtraPrefix + dataClass.getName() + FILE_SEPARATOR + FILE_SUFFIX) : null;
		this.charset = charset;
		this.prettyPrint = prettyPrint;
		this.autoSave = autoSave;
	}
	
	/**
	 * Returns configured class.
	 * 
	 * @return type of stored objects
	 */
	public final Class<T> getDataClass() {
		return dataClass;
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
		String json = toJson(prettyPrint);
		
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
		return serialize(flexjsonHelper.serializer(prettyPrint));
	}
	
	/**
	 * Serializes all stored data to JSON using given serializer.
	 * 
	 * @param serializer
	 *            serializer to be used
	 * @return JSON data
	 */
	protected abstract String serialize(JSONSerializer serializer);
	
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
	
	private void fromJson(String json, boolean executeSave) {
		if (json == null || "".equals(json.trim())) {
			return;
		}
		
		// deserialize
		Object deserialized = null;
		try {
			deserialized = deserialize(flexjsonHelper.deserializer(getDataClass()), json);
		} catch (Exception e) {
			LOG.error("Unable to restore from JSON content, skipping file during restore: " + file.getAbsolutePath() + "!!", e);
		}
		
		// save
		if (deserialized != null && executeSave && autoSave) {
			save();
		}
	}
	
	/**
	 * Deserializes given JSON data to store data. Do not catch any exceptions to due generic handling in this base class.
	 * 
	 * @param deserializer
	 *            deserializer to be used
	 * @param json
	 *            JSON data
	 * @return deserialized data or null if nothing was deserialized. If return value is not null and auto save mode is used, then
	 *         {@link #save()} will be invoked automatically.
	 */
	protected abstract Object deserialize(JSONDeserializer<?> deserializer, String json);
	
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
