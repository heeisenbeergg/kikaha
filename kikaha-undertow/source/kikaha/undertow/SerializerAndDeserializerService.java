package kikaha.undertow;

import injector.AllOf;
import injector.Singleton;
import io.undertow.server.HttpServerExchange;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Singleton
public class SerializerAndDeserializerService {

    static final NotFound<RequestDeserializer> noDeserializerFound = new NotFound<>();
    static final NotFound<ResponseSerializer> noSerializerFound = new NotFound<>();

    final Map<String, RequestDeserializer> deserializers;
    final Map<String, ResponseSerializer> serializers;

    public SerializerAndDeserializerService(
        @AllOf(RequestDeserializer.class) Iterable<RequestDeserializer> requestDeserializers,
        @AllOf(ResponseSerializer.class) Iterable<ResponseSerializer> responseSerializers
    ){
        deserializers = new HashMap<>();
        serializers = new HashMap<>();
        requestDeserializers.forEach( d -> deserializers.put( d.getContentType(), d ) );
        responseSerializers.forEach( s -> serializers.put( s.getContentType(), s ) );
    }

    public <T> T deserialize( String contentType, byte[] bytes, Class<T> type ){
        return deserializers.computeIfAbsent( contentType, noDeserializerFound )
            .deserialize( bytes, type );
    }

    public void serialize( String contentType, HttpServerExchange exchange, Object object ){
        serializers.computeIfAbsent( contentType, noSerializerFound )
                .serialize( exchange, object );
    }

    private static class NotFound<T> implements Function<String, T> {

        @Override
        public T apply(String s) {
            throw new KikahaException.UnsupportedMediaTypeException( s );
        }
    }
}
