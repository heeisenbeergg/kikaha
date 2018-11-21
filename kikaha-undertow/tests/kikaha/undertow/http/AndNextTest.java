package kikaha.undertow.http;

import kikaha.undertow.HttpServerExchangeStub;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;

class AndNextTest {

    @Mock RequestHandler first;
    @Mock RequestHandler second;
    @Mock RequestHandler third;

    @BeforeEach
    void setupMockito(){
        MockitoAnnotations.initMocks(this);
    }

    @DisplayName( "Will wrap the next value before call the next in the chain" )
    @Test void handleRequest(){
        val exchange = HttpServerExchangeStub.createHttpExchange();
        val andNext = new AndNext( first, second );
        andNext.handleRequest( exchange, third );

        verify( first ).handleRequest( eq(exchange), isA( AndNext.class ) );
    }

    @DisplayName( "Will ignore the next value when it is null" )
    @Test void handleRequest2(){
        val exchange = HttpServerExchangeStub.createHttpExchange();
        val andNext = new AndNext( first, second );
        andNext.handleRequest( exchange, null );

        verify( first ).handleRequest( eq(exchange), eq(second) );
    }
}