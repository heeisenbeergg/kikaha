package kikaha.undertow.http;

import io.undertow.Undertow;
import kikaha.config.ConfigLoader;
import kikaha.undertow.Exposed;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xnio.OptionMap;

import java.io.IOException;
import java.util.List;

import static io.undertow.UndertowOptions.ENABLE_HTTP2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName( "UndertowServer: Behaviour Tests" )
class UndertowServerTest {

    final Undertow.Builder undertow = Undertow.builder();
    final SSLContextFactory sslContextFactory = SSLContextFactory.usingDefaults();
    final UndertowServer undertowServer = new UndertowServer( undertow, sslContextFactory );

    @DisplayName("Can auto-configure http")
    @Test void configure() throws IOException {
        val config = ConfigLoader.load( "tests-resources/config-http.yml" );
        undertowServer.configure( config );

        val listeners = Exposed.expose( undertow ).getFieldValue( "listeners", List.class );
        assertEquals( 1, listeners.size() );

        val listener = Exposed.expose( listeners.get(0) );
        val type = listener.getFieldValue( "type", Undertow.ListenerType.class );
        assertEquals( type, Undertow.ListenerType.HTTP );
        assertEquals( 9999, (int)listener.getFieldValue( "port", Integer.class ) );
        assertEquals( "10.0.0.1", listener.getFieldValue( "host", String.class ) );
    }

    @DisplayName("Can auto-configure https")
    @Test void configure2() throws IOException {
        val config = ConfigLoader.load( "tests-resources/config-https.yml" );
        undertowServer.configure( config );

        val listeners = Exposed.expose( undertow ).getFieldValue( "listeners", List.class );
        assertEquals( 1, listeners.size() );

        val listener = Exposed.expose( listeners.get(0) );
        val type = listener.getFieldValue( "type", Undertow.ListenerType.class );
        assertEquals( type, Undertow.ListenerType.HTTPS );
        assertEquals( 10000, (int)listener.getFieldValue( "port", Integer.class ) );
        assertEquals( "10.0.0.2", listener.getFieldValue( "host", String.class ) );
    }

    @DisplayName("Can auto-configure http2")
    @Test void configure3() throws IOException {
        val config = ConfigLoader.load( "tests-resources/config-http2.yml" );
        undertowServer.configure( config );

        val options = Exposed.expose( undertow )
                .getFieldValue( "serverOptions", OptionMap.Builder.class )
                .getMap();

        assertEquals( 1, options.size() );
        assertTrue( options.get( ENABLE_HTTP2 ) );
    }
}