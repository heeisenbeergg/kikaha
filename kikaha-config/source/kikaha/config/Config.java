package kikaha.config;

import java.util.*;

/**
 * Represents a configuration read from a file.
 * Based on the well known Typesafe's Config Library.
 */
public interface Config {

	/**
	 * Retrieve a configuration String from a file.
	 *
	 * @param path which configuration should be read
	 * @return a configuration String.
	 */
	String getString(String path);

	String getString(String path, String defaultValue);

	default byte[] getBytes(String path) {
		return getBytes( path, null );
	}

	byte[] getBytes(String path, String defaultValue);

	/**
	 * Retrieve a configuration Integer from a file.
	 *
	 * @param path which configuration should be read
	 * @return a configuration Integer
	 */
	default int getInteger(String path) {
		return getInteger( path, 0 );
	}

	int getInteger(String path, int defaultValue);

	/**
	 * Retrieve a configuration Long from a file.
	 *
	 * @param path which configuration should be read
	 * @return a configuration Long
	 */
	default long getLong(String path) {
		return getLong( path, 0 );
	}

	long getLong(String path, long defaultValue);

	/**
	 * Retrieve a Boolean parameter from configuration.
	 *
	 * @param path which configuration should be read
	 * @return a Boolean parameter from configuration
	 */
	default boolean getBoolean(String path) {
		return getBoolean( path, false );
	}

	boolean getBoolean(String path, boolean defaultValue);

	/**
	 * Retrieve a list of configurations from a file. It is useful to deal with
	 * nested structures.
	 *
	 * @param path which configuration should be read
	 * @return a list of configurations from a file
	 */
	List<Config> getConfigList(String path);


	/**
	 * Retrieve a list of strings from a file.
	 *
	 * @param path which configuration should be read
	 * @return a list of strings from a file
	 */
	default List<String> getStringList(String path) {
		return getStringList( path, Collections.emptyList() );
	}

	List<String> getStringList(String path, List<String> defaultValues);

	/**
	 * Retrieve the key list available on a configuration.
	 * @return a key list available on a configuration
	 */
	Set<String> getKeys();

	Config getConfig(String path);

	Object getObject(String path);

	default Class<?> getClass( String path ) {
		return getClass( path, null );
	}

	Class<?> getClass( String path, Class<?> defaultClass );

	Map<String, Object> toMap();
}
