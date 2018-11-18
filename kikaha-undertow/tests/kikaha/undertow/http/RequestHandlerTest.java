package kikaha.undertow.http;

import io.undertow.server.HttpServerExchange;
import kikaha.undertow.HttpServerExchangeStub;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName( "RequestHandler: Behavior Tests" )
class RequestHandlerTest {

    @DisplayName("Can handle a simple request")
    @Test void handleRequest1() throws Exception {
        val fallback = mock( RequestHandler.class );
        val chain = new RootHandler().andThen( fallback ).asHttpHandler();
        val exchange = HttpServerExchangeStub.createHttpExchange().setRequestPath("/");
        chain.handleRequest( exchange );

        assertEquals( 200, exchange.getResponseCode() );
        verify( fallback, never() ).handleRequest( any(), any() );
        verify( exchange.getResponseSender() ).send( eq("ROOT") );
    }

    @DisplayName( "Can execute a fallback implementation" )
    @Test void handleRequest2() throws Exception {
        val fallback = mock( RequestHandler.class );
        val chain = new RootHandler().andThen( fallback ).asHttpHandler();
        val exchange = HttpServerExchangeStub.createHttpExchange().setRequestPath("/notFound");
        chain.handleRequest( exchange );

        verify( fallback ).handleRequest( any(), any() );
        verify( exchange.getResponseSender(), never() ).send( anyString() );
    }
}

class RootHandler implements RequestHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange, RequestHandler next) {
        if ( exchange.getRequestPath().equals( "/" ) ){
            exchange.getResponseSender().send( "ROOT" );
            exchange.endExchange();
        } else
            next.handleRequest( exchange, null );
    }
}
