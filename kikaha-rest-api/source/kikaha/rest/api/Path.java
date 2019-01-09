package kikaha.rest.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the REST endpoint path that is expected to be mapped.<br>
 * <br>
 * When placed on a top-level class, it assumes that all mapped methods
 * will use this path as prefix for their URLs.<br>
 * <br>
 * When placed on a method, this path is appended to the top-level defined
 * path. When no top-level class path is defined, then the path defined on
 * the method will be used as the full path to be mapped.<br>
 * <br>
 * @since 3.0
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Path {

    String value();
}
