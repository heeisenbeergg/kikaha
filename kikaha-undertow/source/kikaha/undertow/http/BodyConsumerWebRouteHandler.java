package kikaha.undertow.http;

import io.undertow.io.Receiver;
import io.undertow.server.HttpServerExchange;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * A {@link RequestHandler} designed to buffer the entire request body in a non-blocking way.
 * It is mostly used by POST, PUT and PATCH requests.
 */
public interface BodyConsumerWebRouteHandler extends WebRouteHandler, Receiver.ErrorCallback {

    @Override
    default void handleRequest(HttpServerExchange exchange, RequestHandler next){
        val successCallback = new BodyConsumerCallback( this, next );
        exchange.getRequestReceiver().receiveFullBytes( successCallback, this );
    }

    void handleRequest(HttpServerExchange exchange, byte[] message, RequestHandler next);
}

@RequiredArgsConstructor
class BodyConsumerCallback implements Receiver.FullBytesCallback {

    final BodyConsumerWebRouteHandler handler;
    final RequestHandler next;

    @Override
    public void handle(HttpServerExchange exchange, byte[] message) {
        handler.handleRequest( exchange, message, next );
    }
}