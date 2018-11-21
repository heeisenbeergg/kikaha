package kikaha.undertow.http;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import kikaha.undertow.HttpServerExchangeStub;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CustomRoutesTest {

    final HttpServerExchange exchange = HttpServerExchangeStub.createHttpExchange();

    @Mock
    RequestHandler next;

    @BeforeEach
    void setupMockito(){
        MockitoAnnotations.initMocks(this);
        exchange
            .setRequestMethod( Methods.GET )
            .setRequestPath( "/hello/world/1" )
        ;
    }

    @DisplayName( "Can match requests added through the constructor" )
    @Test void handleRequest() {
        val routes = new CustomRoutes( singletonList( new HelloWorldRoute() ) );
        routes.handleRequest( exchange, next );

        verify( next, never() ).handleRequest( any(), any() );
        verify( exchange.getResponseSender() ).send( eq( "HELLO WORLD" ) );
    }

    @DisplayName( "Can match requests added through the API" )
    @Test void handleRequest1() {
        val routes = new CustomRoutes();
        routes.add( new HelloWorldRoute() );
        routes.handleRequest( exchange, next );

        verify( next, never() ).handleRequest( any(), any() );
        verify( exchange.getResponseSender() ).send( eq( "HELLO WORLD" ) );
    }

    @DisplayName( "Can match requests added through the API, defining a custom method" )
    @Test void handleRequest2() {
        exchange.setRequestMethod( Methods.POST );

        val routes = new CustomRoutes();
        routes.add( Methods.POST, "/hello/world/1", new HelloWorldRoute() );
        routes.handleRequest( exchange, next );

        verify( next, never() ).handleRequest( any(), any() );
        verify( exchange.getResponseSender() ).send( eq( "HELLO WORLD" ) );
    }

    @DisplayName( "Calls the next handler in the chain when it doesn't match the method" )
    @Test void handleRequest3() {
        exchange.setRequestMethod( Methods.POST );

        val routes = new CustomRoutes().add( new HelloWorldRoute() );
        routes.handleRequest( exchange, next );

        verify( next ).handleRequest( eq( exchange ), eq(null) );
        verifyZeroInteractions( exchange.getResponseSender() );
    }

    @DisplayName( "Calls the next handler in the chain when it doesn't match the url" )
    @Test void handleRequest4() {
        exchange.setRequestPath( "/hello" );

        val routes = new CustomRoutes().add( new HelloWorldRoute() );
        routes.handleRequest( exchange, next );

        verify( next ).handleRequest( eq( exchange ), eq(null) );
        verifyZeroInteractions( exchange.getResponseSender() );
    }

    @DisplayName( "Can memorize path parameters" )
    @Test void handleRequest6() {
        new CustomRoutes()
            .add( new HelloWorldRoute() )
            .handleRequest( exchange, next );

        val pathParams = CustomRoutes.extractRequestParameters( exchange );
        assertEquals( "1",  pathParams.get( "id" ) );
    }
}

@Getter
class HelloWorldRoute implements WebRouteHandler {

    String httpMethod = Methods.GET_STRING;
    String relativePath = "/hello/world/{id}";

    @Override
    public void handleRequest(HttpServerExchange exchange, RequestHandler next) {
        exchange.getResponseSender().send( "HELLO WORLD" );
    }
}