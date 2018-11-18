package kikaha.undertow.http;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * The action which should be able to handle an incoming request.
 */
public interface RequestHandler {

    void handleRequest(HttpServerExchange exchange, RequestHandler next);

    /**
     * Defines which RequestHandler should handle the request in cases where
     * this current object isn't capable to handle it by itself.
     *
     * @param next
     * @return
     * @throws IOException
     */
    default RequestHandler andThen( RequestHandler next ) throws IOException {
        return new AndNext( this, next );
    }

    /**
     * Converts the RequestHandle into a proper Undertow's {@link HttpHandler}.
     * @return
     */
    default HttpHandler asHttpHandler(){
        return new UndertowRequestHandler( this );
    }
}

@RequiredArgsConstructor
class AndNext implements RequestHandler {

    @NonNull final RequestHandler handler;
    @NonNull final RequestHandler next;

    @Override
    public void handleRequest( HttpServerExchange exchange, RequestHandler ignored ) {
        this.handler.handleRequest( exchange, this.next );
    }
}

@RequiredArgsConstructor
class UndertowRequestHandler implements HttpHandler {

    final RequestHandler wrapped;

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) {
        wrapped.handleRequest( httpServerExchange, null );
    }
}