package kikaha.rest.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps the body request data into an object. It will rely on the configured serialization
 * algorithm to convert the data into an object.<br>
 * <br>
 * @since 3.0
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Body {}
