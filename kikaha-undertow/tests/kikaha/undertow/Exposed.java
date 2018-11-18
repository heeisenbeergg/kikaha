package kikaha.undertow;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Machiavellian code created to be used only on unit-tests. It helps us to bypass
 * some odds from Undertow API through the Java Reflection API.
 * <p>
 * Don't use under production environments: not secure, not thread-safe and not that fast too.
 */
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class Exposed {

	final Object object;

	@SneakyThrows
	public <T> T getFieldValue( String name, Class<T> target ) {
		final Field field = object.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return (T)field.get(object);
	}

	@SneakyThrows
	public Exposed setFieldValue( String name, Object value ) {
		final Field field = object.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(object, value);
		return this;
	}

	public <T> T runMethodSilently(String name, Object...args ) {
		try {
			return runMethod(name, args);
		} catch ( Throwable e ){
			return null;
		}
	}

	public <T> T runMethod(String name, Object...args ) {
		try {
			final Method method = object.getClass().getDeclaredMethod(name, toClassArray(args));
			method.setAccessible(true);
			return (T)method.invoke(object, args);
		} catch ( Throwable t ) {
			throw new IllegalArgumentException( t );
		}
	}

	Class[] toClassArray( Object[] objs ) {
		final Class[] classes = new Class[ objs.length ];
		int i = 0;
		for ( Object obj : objs )
			classes[ i++ ] = obj.getClass();
		return classes;
	}

	public static Exposed expose( Object object ) {
		return new Exposed( object );
	}
}