package com.github.christiangroth.jsonstore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.christiangroth.jsonstore.store.JsonSingletonStore;
import com.github.christiangroth.jsonstore.store.JsonStore;

/**
 * Central API class to create JSON stores. Stores are maintained per class using {@link #resolve(Class)}, {@link #ensure(Class)} and
 * {@link #drop(Class)}. Explicit saving to and loading from file system can be done using {@link #save()} and {@link #load()}. Each JSON
 * store will create a separate file.
 * 
 * @author Christian Groth
 */
public class JsonStores {
	
	private static final Logger LOG = LoggerFactory.getLogger(JsonStores.class);
	
	private static final String STORE_FILENAME_REGEX = "storage\\.(.+)\\.json";
	private static final Pattern STORE_FILENAME_PATTERN = Pattern.compile(STORE_FILENAME_REGEX);
	
	private static final String SINGLETON_STORE_FILENAME_REGEX = "storage\\.singleton\\.(.+)\\.json";
	private static final Pattern SINGLETON_STORE_FILENAME_PATTERN = Pattern.compile(SINGLETON_STORE_FILENAME_REGEX);
	
	private final Map<Class<?>, JsonStore<?>> stores;
	private final Map<Class<?>, JsonSingletonStore<?>> singletonStores;
	private final File storage;
	private final Charset charset;
	private final boolean prettyPrint;
	private final boolean autoSave;
	
	/**
	 * Builder class to control creation of {@link JsonStores}.
	 * 
	 * @author Christian Groth
	 */
	public static class JsonStoresBuilder {
		
		private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
		
		private File storage;
		private Charset charset;
		private boolean prettyPrint;
		private boolean autoSave;
		
		/**
		 * Configures persistent JSON storage with given base directory, default UTF-8 charset, pretty-print mode disabled and auto-save
		 * mode enabled.
		 * 
		 * @param storage
		 *            base storage directory
		 * @return builder
		 */
		public JsonStoresBuilder storage(File storage) {
			this.storage = storage;
			this.charset = DEFAULT_CHARSET;
			this.prettyPrint = false;
			this.autoSave = true;
			return this;
		}
		
		/**
		 * Configures persistent JSON storage with given base directory, charset, pretty-print mode and auto-save mode.
		 * 
		 * @param storage
		 *            base storage directory
		 * @param charset
		 *            storage charset
		 * @param prettyPrint
		 *            pretty-print mode
		 * @param autoSave
		 *            auto-save mode
		 * @return builder
		 */
		public JsonStoresBuilder storage(File storage, Charset charset, boolean prettyPrint, boolean autoSave) {
			this.storage = storage;
			this.charset = charset;
			this.prettyPrint = prettyPrint;
			this.autoSave = autoSave;
			return this;
		}
		
		/**
		 * Creates the {@link JsonStores} instance.
		 * 
		 * @return stores instance
		 */
		public JsonStores build() {
			return new JsonStores(storage, charset, prettyPrint, autoSave);
		}
	}
	
	/**
	 * Creates a new builder instance.
	 * 
	 * @return stores builder
	 */
	public static JsonStoresBuilder builder() {
		return new JsonStoresBuilder();
	}
	
	private JsonStores(File storage, Charset charset, boolean prettyPrint, boolean autoSave) {
		
		// init state
		stores = new HashMap<>();
		singletonStores = new HashMap<>();
		this.storage = storage == null ? null : storage.getAbsoluteFile();
		this.charset = charset;
		this.prettyPrint = prettyPrint;
		this.autoSave = autoSave;
		
		// prepare storage
		if (isPersistent()) {
			
			// check if exists
			if (!Files.exists(storage.toPath())) {
				try {
					LOG.info("creating storage path " + storage.getAbsolutePath());
					Files.createDirectories(storage.toPath());
				} catch (IOException e) {
					LOG.error("Unable to initialize storage path: " + storage.getAbsolutePath() + "!!", e);
				}
			}
			
			// auto load
			if (autoSave) {
				load();
			}
		}
	}
	
	/**
	 * Ensures existence of JSON store for given class.
	 * 
	 * @param dataClass
	 *            class for JSON store
	 * @return existing or created JSON store
	 * @param <T>
	 *            concrete type of data
	 */
	public <T> JsonStore<T> ensure(Class<T> dataClass) {
		if (!stores.containsKey(dataClass)) {
			create(dataClass);
		}
		
		return resolve(dataClass);
	}
	
	private void create(Class<?> dataClass) {
		stores.put(dataClass, new JsonStore<>(dataClass, storage, charset, prettyPrint, autoSave));
	}
	
	/**
	 * Resolves JSON store for given class.
	 * 
	 * @param dataClass
	 *            class for JSON store
	 * @return existing JSON store, may be null
	 * @param <T>
	 *            concrete type of data
	 */
	@SuppressWarnings("unchecked")
	public <T> JsonStore<T> resolve(Class<T> dataClass) {
		return (JsonStore<T>) stores.get(dataClass);
	}
	
	/**
	 * Ensures existence of JSON singleton store for given class.
	 * 
	 * @param dataClass
	 *            class for JSON store
	 * @return existing or created JSON singleton store
	 * @param <T>
	 *            concrete type of data
	 */
	public <T> JsonSingletonStore<T> ensureSingleton(Class<T> dataClass) {
		if (!singletonStores.containsKey(dataClass)) {
			createSingleton(dataClass);
		}
		
		return resolveSingleton(dataClass);
	}
	
	private void createSingleton(Class<?> dataClass) {
		singletonStores.put(dataClass, new JsonSingletonStore<>(dataClass, storage, charset, prettyPrint, autoSave));
	}
	
	/**
	 * Resolves JSON singleton store for given class.
	 * 
	 * @param dataClass
	 *            class for JSON store
	 * @return existing JSON singleton store, may be null
	 * @param <T>
	 *            concrete type of data
	 */
	@SuppressWarnings("unchecked")
	public <T> JsonSingletonStore<T> resolveSingleton(Class<T> dataClass) {
		return (JsonSingletonStore<T>) singletonStores.get(dataClass);
	}
	
	/**
	 * Drops JSON store for given class, is existent. Results in calling {@link JsonStore#drop()} if using auto-save mode and store exists.
	 * 
	 * @param dataClass
	 *            class for JSON store
	 * @return dropped JSON store
	 * @param <T>
	 *            concrete type of data
	 */
	@SuppressWarnings("unchecked")
	public <T> JsonStore<T> drop(Class<T> dataClass) {
		
		// drop in memory
		JsonStore<T> store = (JsonStore<T>) stores.remove(dataClass);
		if (store != null && isPersistent()) {
			
			// remove file
			store.drop();
		}
		
		// done
		return store;
	}
	
	/**
	 * Drops JSON singleton store for given class, is existent. Results in calling {@link JsonSingletonStore#drop()} if using auto-save mode
	 * and store exists.
	 * 
	 * @param dataClass
	 *            class for JSON store
	 * @return dropped JSON singleton store
	 * @param <T>
	 *            concrete type of data
	 */
	@SuppressWarnings("unchecked")
	public <T> JsonSingletonStore<T> dropSingleton(Class<T> dataClass) {
		
		// drop in memory
		JsonSingletonStore<T> store = (JsonSingletonStore<T>) singletonStores.remove(dataClass);
		if (store != null && isPersistent()) {
			
			// remove file
			store.drop();
		}
		
		// done
		return store;
	}
	
	/**
	 * If stores are persistent {@link JsonStore#save()} will be invoked using parallel stream on all existing stores.
	 */
	public void save() {
		
		// abort on transient stores
		if (!isPersistent()) {
			return;
		}
		
		// delegate to all stores
		stores.values().parallelStream().forEach(store -> store.save());
		singletonStores.values().parallelStream().forEach(store -> store.save());
	}
	
	/**
	 * Creates JSON stores from configured storage directory.
	 */
	public void load() {
		
		// abort on transient stores
		if (!isPersistent()) {
			return;
		}
		
		// walk all files in path
		try {
			Files.walk(storage.toPath(), 1)
					.filter(child -> Files.isReadable(child) && Files.isRegularFile(child)
							&& (child.getFileName().toFile().getName().matches(STORE_FILENAME_REGEX) || child.getFileName().toFile().getName().matches(SINGLETON_STORE_FILENAME_REGEX))).forEach(storeFile -> loadStore(storeFile));
		} catch (IOException e) {
			LOG.error("Unable to scan storage path " + storage.getAbsolutePath() + ", skipping data restore!!", e);
		}
	}
	
	private void loadStore(Path storeFile) {
		
		// get file
		File file = storeFile.toFile();
		String filename = file.getName();
		
		// detect type (singleton first due to regex matching)
		Matcher singletonMatcher = SINGLETON_STORE_FILENAME_PATTERN.matcher(filename);
		Matcher regularMatcher = STORE_FILENAME_PATTERN.matcher(filename);
		Matcher matcher = null;
		boolean isSingleton = false;
		if (singletonMatcher.matches()) {
			matcher = singletonMatcher;
			isSingleton = true;
		} else if (regularMatcher.matches()) {
			matcher = regularMatcher;
			isSingleton = false;
		}
		
		// load class
		Class<?> dataClass = null;
		try {
			dataClass = Class.forName(matcher.group(1));
			
			// ensure store and load data
			if (!isSingleton) {
				ensure(dataClass).load();
			} else {
				ensureSingleton(dataClass).load();
			}
		} catch (Exception e) {
			LOG.error("Unable to load data class " + dataClass + ", skipping file during restore: " + file.getAbsolutePath() + "!!", e);
		}
	}
	
	public boolean isPersistent() {
		return storage != null;
	}
}
