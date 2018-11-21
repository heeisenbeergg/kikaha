package kikaha.undertow.http;

import kikaha.undertow.HttpServerExchangeStub;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class BodyConsumerCallbackTest {

    @Mock RequestHandler next;
    @Mock BodyConsumerWebRouteHandler handler;

    @BeforeEach void setupMockito(){
        MockitoAnnotations.initMocks(this);
    }

    @DisplayName( "Should be able to notify the BodyConsumerWebRouteHandler" )
    @Test void handle()
    {
        val msg = "Hello".getBytes();
        val exchange = HttpServerExchangeStub.createHttpExchange();

        val callback = new BodyConsumerCallback( handler, next );
        callback.handle( exchange, msg );

        verify( handler ).handleRequest( eq(exchange), eq(msg), eq(next) );
    }
}
