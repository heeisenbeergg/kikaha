package kikaha.undertow.http;

/**
 * Defines how a deserializer should behave in order to proper transform
 * an array of bytes into a new object. Usually, this class is used to
 * transform the request body into a given object.
 */
public interface RequestDeserializer {

    <T> T deserialize( byte[] bytes, Class<T> type );

    String getContentType();
}
