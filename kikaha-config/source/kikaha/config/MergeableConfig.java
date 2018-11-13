package kikaha.config;

import static java.lang.String.format;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.*;
import org.yaml.snakeyaml.Yaml;

/**
 *
 */
@RequiredArgsConstructor
public class MergeableConfig implements Config {

	static final byte[] EMPTY_BYTES = new byte[]{};

	final Yaml yaml = new Yaml();
	Charset encoding = Charset.defaultCharset();

	final Map<String, Object> conf;
	final String rootPath;

	MergeableConfig(){
		this.conf = new HashMap<>();
		this.rootPath = "";
	}

	public static MergeableConfig create(){
		return new MergeableConfig();
	}

	public MergeableConfig load(File file ) throws IOException {
		try (final FileInputStream inputStream = new FileInputStream(file) ) {
			return load( inputStream );
		}
	}

	public MergeableConfig load(InputStream inputStream){
		Map<String, Object> newConf = (Map<String, Object>) yaml.load(inputStream);
		deepMerge( conf, newConf );
		return this;
	}

	@Override
	public String toString() {
		return yaml.dump( conf );
	}

	@Override
	public Map<String, Object> toMap(){
		return conf;
	}

	@Override
	public Set<String> getKeys() {
		return conf.keySet();
	}

	@Override
	public Config getConfig(String path) {
		final Map<String, Object> current = read(path, o->(Map<String,Object>)o);
		if ( current == null )
			return null;
		return new MergeableConfig(current, rootPath + path + ".");
	}

	@Override
	public Object getObject(String path) {
		return read( path, o->o );
	}

	public Class<?> getClass( String path, Class<?> defaultClass ) {
		final String clazzName = getString(path);
		if ( clazzName == null )
			return defaultClass;
		return instantiate( clazzName );
	}

	private Class<?> instantiate( String className ) {
		try {
			return Class.forName( className );
		} catch ( Throwable cause ) {
			throw new IllegalStateException( "Can't load " + className, cause);
		}
	}

	@Override
	public byte[] getBytes(String path, String defaultValue ){
		String value = getString(path);
		if ( value == null )
			value = defaultValue;
		if ( value == null )
			return EMPTY_BYTES;
		return value.getBytes( encoding );
	}

	@Override
	public String getString(String path, String defaultValue) {
		final String value = getString(path);
		return value != null ? value : defaultValue;
	}

	@Override
	public String getString(String path) {
		final String propertyValue = System.getProperty(rootPath + path);
		if ( propertyValue != null )
			return propertyValue;
		return read( path, o->(String)o );
	}

	@Override
	public boolean getBoolean(String path, boolean defaultValue) {
		final String propertyValue = System.getProperty(rootPath + path);
		if ( propertyValue != null )
			return Boolean.valueOf(propertyValue);
		return read( path, o->ifNull( (Boolean)o , defaultValue) );
	}

	@Override
	public int getInteger(String path, int defaultValue) {
		final String propertyValue = System.getProperty(rootPath + path);
		if ( propertyValue != null )
			return Integer.valueOf( propertyValue );
		return read( path, o->ifNull( (Integer)o, defaultValue ) );
	}

	@Override
	public long getLong(String path, long defaultValue) {
		final String propertyValue = System.getProperty(rootPath + path);
		if ( propertyValue != null )
			return Long.valueOf( propertyValue );
		return read( path, o->ifNull( asLong(o), defaultValue ) );
	}

	@Override
	public List<Config> getConfigList(String path){
		final List<Map<String, Object>> current = read(path, o->(List<Map<String,Object>>)o);
		if ( current == null )
			return Collections.emptyList();
		return current.stream().map( m -> new MergeableConfig(m, rootPath + path + ".") ).collect(Collectors.toList());
	}

	@Override
	public List<String> getStringList( String path, List<String> defaultValues ) {
		final List<String> current = read(path, o -> (List<String>) o);
		if ( current == null )
			return defaultValues;
		return current;
	}

	private <T> T read( String path, Function<Object,T> parser ) {
		final String[] strings = path.split("\\.");
		Map<String, Object> current = readPath(strings);
		if ( current == null )
			current = conf;
		final Object last = current.get( strings[strings.length-1] );
		return parser.apply( last );
	}

	private Map<String, Object> readPath( String[] strings ){
		Map<String, Object> current = conf;
		for ( int i=0; i<strings.length-1 && current != null; i++ )
			current = (Map<String, Object>) current.get( strings[i] );
		return current;
	}

	/**
	 * Apply a deep merge on two {@code Map}s. Thanks to
	 * {@code http://stackoverflow.com/a/36123154/548685}.
	 *
	 * @param original
	 * @param newMap
	 */
	private static void deepMerge( @NonNull Map original, @NonNull Map newMap) {
		for (Entry e : (Set<Entry>) newMap.entrySet()) {
			Object currentKey = e.getKey(), currentValue = e.getValue();
			if (shouldMergeAnyWay( original, currentKey, currentValue ))
				original.put(currentKey, currentValue);
		}
	}

	private static boolean shouldMergeAnyWay(Map original, Object currentKey, Object currentValue ){
		if (original.containsKey(currentKey)) {
			Object originalValue = original.get(currentKey);

			if (originalValue instanceof Collection) {
				checkArgument(currentValue instanceof Collection,
						"A non-collection collided with a collection: %s\t%s",currentValue, originalValue);
				((Collection) originalValue).addAll((Collection) currentValue);
				return false;
			}

			if (originalValue instanceof Map) {
				checkArgument(currentValue instanceof Map,
						"A non-map collided with a map: %s\t%s",
						currentValue, originalValue);
				deepMerge((Map) originalValue, (Map) currentValue);
				return false;
			}
		}

		return true;
	}

	private static void checkArgument( boolean isValid, String str, Object...args ){
		if ( !isValid )
			throw new IllegalArgumentException( format(str, args) );
	}

	private static <T> T ifNull( T value, T defaultValue ) {
		return value != null ? value : defaultValue;
	}

	private static long asLong( Object o ) {
		if ( o instanceof Integer )
			return (int)o;
		else if ( o instanceof String )
			return Long.valueOf( (String)o );
		return (Long)o;
	}
}
