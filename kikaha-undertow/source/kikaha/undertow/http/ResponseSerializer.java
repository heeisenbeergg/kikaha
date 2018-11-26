package kikaha.undertow.http;

import io.undertow.server.HttpServerExchange;

/**
 * Defines how a serializer should behave in order to send
 * an object as response to a request. It is expected that the
 * serialize proper take care of the response ensuring that
 * the response will not block the thread.
 */
public interface ResponseSerializer {

    void serialize(HttpServerExchange exchange, Object object);

    String getContentType();
}
