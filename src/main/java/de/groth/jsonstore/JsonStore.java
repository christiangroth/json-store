package de.groth.jsonstore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.transformer.DateTransformer;

/**
 * Represents a JSON store for a concrete class. Access is provided using
 * delegate methods to Java built in stream API.
 * 
 * You may use flexjson annotations to control conversion from/to JSON.
 * 
 * @author cgroth
 *
 * @param <T>
 *            concrete type stored in this instance
 */
public class JsonStore<T> {

	private static final String FILE_SEPARATOR = ".";
	private static final String FILE_PREFIX = "storage";
	private static final String FILE_SUFFIX = "json";

	private final Class<T> clazz;
	private final Set<T> data;
	private final File file;
	private final boolean prettyPrint;
	private final boolean autoSave;

	/**
	 * Creates a new JSON store.
	 * 
	 * @param clazz
	 *            type of objects to be stored
	 * @param storage
	 *            global storage path
	 * @param prettyPrint
	 *            pretty-print mode
	 * @param autoSave
	 *            auto-save mode
	 */
	public JsonStore(Class<T> clazz, File storage, boolean prettyPrint,
			boolean autoSave) {
		this.clazz = clazz;
		data = new HashSet<>();
		this.file = storage != null ? new File(storage, FILE_PREFIX
				+ FILE_SEPARATOR + clazz.getName() + FILE_SEPARATOR
				+ FILE_SUFFIX) : null;
		this.prettyPrint = prettyPrint;
		this.autoSave = autoSave;
	}

	/**
	 * Returns configured class.
	 * 
	 * @return type of stored objects
	 */
	public Class<T> getClazz() {
		return clazz;
	}

	/**
	 * Returns copy of data
	 * 
	 * @return copy of data
	 */
	public Set<T> copy() {
		return new HashSet<>(data);
	}

	/**
	 * Returns store size.
	 * 
	 * @return size
	 */
	public int size() {
		return data.size();
	}

	/**
	 * Checks if store is empty
	 * 
	 * @return true if empty, false otherwise
	 */
	public boolean isEmpty() {
		return data.isEmpty();
	}

	/**
	 * Checks if store contains given element.
	 * 
	 * @param o
	 *            object to be checked
	 * @return true if object is contained, false otherwise
	 */
	public boolean contains(Object o) {
		return data.contains(o);
	}

	/**
	 * Checks if store contains all objects in given collection.
	 * 
	 * @param c
	 *            collection to be checked
	 * @return true if all objects are contained, false otherwise
	 */
	public boolean containsAll(Collection<?> c) {
		return data.containsAll(c);
	}

	/**
	 * Adds given object to store. Will invoke {@link #save()} if using
	 * auto-save mode and store was changed.
	 * 
	 * @param e
	 *            object to add
	 * @return true if store was changed, false otherwise
	 */
	public boolean add(T e) {
		boolean add = data.add(e);
		if (autoSave && add) {
			save();
		}
		return add;
	}

	/**
	 * Adds all objects from given collection to store. Will invoke
	 * {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param c
	 *            objects to add
	 * @return true if store was changed, false otherwise
	 */
	public boolean addAll(Collection<? extends T> c) {
		boolean addAll = data.addAll(c);
		if (autoSave && addAll) {
			save();
		}
		return addAll;
	}

	/**
	 * Retains elements in given collection.Will invoke {@link #save()} if using
	 * auto-save mode and store was changed.
	 * 
	 * @param c
	 *            collection of elements to be retained
	 * @return true if store was changed, false otherwise
	 */
	public boolean retainAll(Collection<?> c) {
		boolean retainAll = data.retainAll(c);
		if (autoSave && retainAll) {
			save();
		}
		return retainAll;
	}

	/**
	 * Removed the given element from store. Will invoke {@link #save()} if
	 * using auto-save mode and store was changed.
	 * 
	 * @param t
	 *            element to be removed
	 * @return true if store was changed, false otherwise
	 */
	public boolean remove(T t) {
		boolean remove = data.remove(t);
		if (autoSave) {
			save();
		}
		return remove;
	}

	/**
	 * Removed all elements in given collection from store. Will invoke
	 * {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param c
	 *            collection with elements to be removed
	 * @return true if store was changed, false otherwise
	 */
	public boolean removeAll(Collection<T> c) {
		boolean removeAll = data.removeAll(c);
		if (autoSave) {
			save();
		}
		return removeAll;
	}

	/**
	 * Removes all elements satisfying given predicate from store. Will invoke
	 * {@link #save()} if using auto-save mode and store was changed.
	 * 
	 * @param filter
	 *            predicate to be used
	 * @return true if store was changed, false otherwise
	 */
	public boolean removeIf(Predicate<? super T> filter) {
		boolean removeIf = data.removeIf(filter);
		if (autoSave) {
			save();
		}
		return removeIf;
	}

	/**
	 * Clears all elements in store. Will invoke {@link #save()} if using
	 * auto-save mode.
	 */
	public void clear() {
		data.clear();
		if (autoSave) {
			save();
		}
	}

	/**
	 * Creates a stream over the elements in this store.
	 * 
	 * @return stream over elements in store
	 */
	public Stream<T> stream() {
		return data.stream();
	}

	/**
	 * Creates a parallel stream over the elements in this store.
	 * 
	 * @return parallel stream over elements in store
	 */
	public Stream<T> parallelStream() {
		return data.parallelStream();
	}

	/**
	 * Performs given action on all elements in store. <br>
	 * <br>
	 * <b>Attention: Even if using auto-save mode you have to call
	 * {@link #save()} yourself!!</b>
	 * 
	 * @param action
	 *            action to be performed on store elements
	 */
	public void forEach(Consumer<? super T> action) {
		data.forEach(action);
	}

	/**
	 * Returns file used for storage.
	 * 
	 * @return file, may be null
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Checks if store is persistent.
	 * 
	 * @return true if store is persistent, false otherwise
	 */
	public boolean isPersistent() {
		return file != null;
	}

	/**
	 * Saves all data contained in store to configured file. No action if store
	 * is not persistent.
	 */
	public void save() {

		// abort on transient stores
		if (!isPersistent()) {
			return;
		}

		// create json
		String json = toJson(prettyPrint);

		// write to file
		try {
			synchronized (file) {
				// TODO charset
				Files.write(file.toPath(), json.getBytes(),
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING,
						StandardOpenOption.WRITE);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err
					.println("Unable to write file content, skipping file during store: "
							+ file.getAbsolutePath() + "!!");
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Returns store elements in JSON format.
	 * 
	 * @return JSON data
	 */
	public String toJson() {
		return toJson(false);
	}

	/**
	 * Returns store elements in JSON format with given pretty-print mode.
	 * 
	 * @param prettyPrint
	 *            pretty-print mode
	 * @return JSON data
	 */
	public String toJson(boolean prettyPrint) {
		return transformer(prettyPrint).serialize(data);
	}

	/**
	 * Loads store elements from configure file.
	 */
	public void load() {

		// abort on transient stores
		if (!isPersistent()) {
			return;
		}

		// load json
		String json = null;
		try {
			synchronized (file) {
				json = Files
						.lines(file.toPath())
						.parallel()
						.filter(line -> line != null && !"".equals(line.trim()))
						.map(String::trim).collect(Collectors.joining());
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			System.err
					.println("Unable to read file content, skipping file during restore: "
							+ file.getAbsolutePath() + "!!");
		}

		// recreate data
		fromJson(json, false);
	}

	/**
	 * Creates store elements from given JSON data and replaces all store
	 * contents.Will invoke {@link #save()} if using auto-save mode.
	 * 
	 * @param json
	 *            JSON data
	 */
	public void fromJson(String json) {
		fromJson(json, true);
	}

	private void fromJson(String json, boolean executeSave) {
		if (json == null || "".equals(json.trim())) {
			return;
		}

		// deserialize
		List<T> deserialized = null;
		try {
			deserialized = new JSONDeserializer<List<T>>().use(Date.class,
					dateTransformer()).deserialize(json);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err
					.println("Unable to restore from json content, skipping file during restore: "
							+ file.getAbsolutePath() + "!!");
			System.err.println("Invalid JSON: " + json);
		}

		// add data
		if (deserialized != null) {
			data.clear();
			data.addAll(deserialized);

			// save
			if (executeSave && autoSave) {
				save();
			}
		}
	}

	private JSONSerializer transformer(boolean prettyPrint) {
		return new JSONSerializer().prettyPrint(prettyPrint).transform(
				dateTransformer(), Date.class);
	}

	private DateTransformer dateTransformer() {
		return new DateTransformer("HH:mm:ss dd.MM.yyyy");
	}

	/**
	 * Drops store file explicitly. Transient data in store remains unchanged.
	 */
	public void drop() {
		if (isPersistent()) {
			try {
				synchronized (file) {
					Files.deleteIfExists(file.toPath());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err
						.println("Unable to delete form persistent json storee: "
								+ file.getAbsolutePath() + "!!");
			}
		}
	}
}
