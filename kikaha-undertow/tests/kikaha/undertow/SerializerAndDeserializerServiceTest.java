package kikaha.undertow;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

class SerializerAndDeserializerServiceTest {

    final static String HELLO = "Hello";
    final static byte[] HELLO_BYTES = HELLO.getBytes();
    final static String TEXT_PLAIN = "text/plain";

    @Mock RequestDeserializer deserializer;
    @Mock ResponseSerializer serializer;

    SerializerAndDeserializerService service;

    @BeforeEach void setupMocks(){
        MockitoAnnotations.initMocks(this);

        doReturn( TEXT_PLAIN ).when( serializer ).getContentType();
        doReturn( TEXT_PLAIN ).when( deserializer ).getContentType();

        service = new SerializerAndDeserializerService(
            singletonList( deserializer ),
            singletonList( serializer )
        );
    }

    @DisplayName( "Should be able to deserialize a known content-type" )
    @Test void deserialize() {
        doReturn( HELLO ).when( deserializer ).deserialize( eq(HELLO_BYTES), eq(String.class) );

        val deserialized = service.deserialize( TEXT_PLAIN, HELLO_BYTES, String.class );

        verify( deserializer ).deserialize( eq(HELLO_BYTES), eq(String.class) );
        assertEquals( HELLO, deserialized );
    }

    @DisplayName( "Should throw exception while deserializing an unknown content-type" )
    @Test void deserialize2(){
        assertThrows(KikahaException.UnsupportedMediaTypeException.class,
            () -> service.deserialize( "application/json", HELLO_BYTES, String.class ),
                "application/json");
    }

    @DisplayName( "Should be able to serialize a known content-type" )
    @Test void serialize(){
        val exchange = HttpServerExchangeStub.createHttpExchange();

        service.serialize( TEXT_PLAIN, exchange, HELLO_BYTES );

        verify( serializer ).serialize( eq(exchange), eq(HELLO_BYTES) );
    }

    @DisplayName( "Should throw exception while serializing an unknown content-type" )
    @Test void serialize2(){
        val exchange = HttpServerExchangeStub.createHttpExchange();
        assertThrows(KikahaException.UnsupportedMediaTypeException.class,
            () -> service.serialize( "application/json", exchange, HELLO_BYTES ),
                "application/json");
    }
}