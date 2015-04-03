package de.groth.jsonstore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Central API class to create JSON stores. Stores are maintained per class
 * using {@link #resolve(Class)}, {@link #ensure(Class)} and
 * {@link #drop(Class)}.
 * 
 * Explicit saving to and loading from file system can be done using
 * {@link #save()} and {@link #load()}. Each JSON store will create a separate
 * file.
 * 
 * @author Christian Groth
 *
 */
public class JsonStores {

	private static final String STORE_FILENAME_REGEX = "storage\\.(.+)\\.json";
	private static final Pattern STORE_FILENAME_PATTERN = Pattern
			.compile(STORE_FILENAME_REGEX);

	private final Map<Class<?>, JsonStore<?>> stores;
	private final File storage;
	private final boolean prettyPrint;
	private final boolean autoSave;

	/**
	 * Creates transient JSON stores.
	 */
	public JsonStores() {
		this(null, false, false);
	}

	/**
	 * Creates persistent JSON stores with given base directory and using
	 * auto-save mode. Will also invoke {@link #load()}.
	 * 
	 * @param storage
	 *            base storage directory
	 */
	public JsonStores(File storage) {
		this(storage, true);
	}

	/**
	 * Creates persistent JSON stores with given base directory and auto-save
	 * mode. Will invoke {@link #load()} if auto-save mode is used.
	 * 
	 * @param storage
	 *            base storage directory
	 * @param autoSave
	 *            auto-save mode
	 */
	public JsonStores(File storage, boolean autoSave) {
		this(storage, false, autoSave);
	}

	/**
	 * Creates persistent JSON stores with given base directory, pretty-print
	 * mode and auto-save mode. Will invoke {@link #load()} if auto-save mode is
	 * used.
	 * 
	 * @param storage
	 *            base storage directory
	 * @param prettyPrint
	 *            pretty-print mode
	 * @param autoSave
	 *            auto-save mode
	 */
	public JsonStores(File storage, boolean prettyPrint, boolean autoSave) {

		// init state
		stores = new HashMap<>();
		this.storage = storage.getAbsoluteFile();
		this.prettyPrint = prettyPrint;
		this.autoSave = autoSave;

		// prepare storage
		if (isPersistent()) {

			// check if exists
			if (!Files.exists(storage.toPath())) {
				try {
					Files.createDirectories(storage.toPath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.err
							.println("Unable to initialize storage directory: "
									+ storage.getAbsolutePath() + "!!");
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
	 * @param clazz
	 *            class for JSON store
	 * @return existing or created JSON store
	 * @param <T>
	 *            concrete type of data
	 */
	public <T> JsonStore<T> ensure(Class<T> clazz) {
		if (!stores.containsKey(clazz)) {
			create(clazz);
		}

		return resolve(clazz);
	}

	private void create(Class<?> clazz) {
		stores.put(clazz,
				new JsonStore<>(clazz, storage, prettyPrint, autoSave));
	}

	/**
	 * Resolves JSON store for given class.
	 * 
	 * @param clazz
	 *            class for JSON store
	 * @return existing JSON store, may be null
	 * @param <T>
	 *            concrete type of data
	 */
	@SuppressWarnings("unchecked")
	public <T> JsonStore<T> resolve(Class<T> clazz) {
		return (JsonStore<T>) stores.get(clazz);
	}

	/**
	 * Drops JSON store for given class, is existent. Results in calling
	 * {@link JsonStore#drop()} if using auto-save mode and store exists.
	 * 
	 * @param clazz
	 *            class for JSON store
	 * @return dropped JSON store
	 * @param <T>
	 *            concrete type of data
	 */
	@SuppressWarnings("unchecked")
	public <T> JsonStore<T> drop(Class<T> clazz) {

		// drop in memory
		JsonStore<T> store = (JsonStore<T>) stores.remove(clazz);
		if (store != null && isPersistent()) {

			// remove file
			store.drop();
		}

		// done
		return store;
	}

	/**
	 * If stores are persistent {@link JsonStore#save()} will be invoked using
	 * parallel stream on all existing stores.
	 */
	public void save() {

		// abort on transient stores
		if (!isPersistent()) {
			return;
		}

		// delegate to all collections
		stores.values().parallelStream().forEach(store -> store.save());
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
					.filter(child -> Files.isReadable(child)
							&& Files.isRegularFile(child)
							&& child.getFileName().toFile().getName()
									.matches(STORE_FILENAME_REGEX))
					.forEach(
							storeFile -> {

								File file = storeFile.toFile();
								String filename = file.getName();
								Matcher matcher = STORE_FILENAME_PATTERN
										.matcher(filename);
								if (matcher.matches()) {
									Class<?> clazz = null;
									try {

										// load class
										clazz = Class.forName(matcher.group(1));

										// recreate store with data
										ensure(clazz).load();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										System.err.println("Unable to load class "
												+ clazz
												+ ", skipping file during restore: "
												+ file.getAbsolutePath() + "!!");
									}
								}
							});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Unable to scan storage path "
					+ storage.getAbsolutePath() + ", skipping data restore!!");
		}
	}

	private boolean isPersistent() {
		return storage != null;
	}
}
