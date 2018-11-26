package kikaha.undertow;

import io.undertow.Undertow;
import kikaha.config.Config;
import lombok.NonNull;
import lombok.val;
import org.xnio.Option;

import java.io.IOException;

import static io.undertow.UndertowOptions.*;

/**
 * A simplified Undertow builder builder.
 */
public class UndertowServer {

    final Undertow.Builder builder;
    final SSLContextFactory sslContextFactory;

    private boolean started = false;
    private Undertow server;

    public static UndertowServer usingDefaults(){
        return new UndertowServer(
            Undertow.builder()
                .setServerOption(ENABLE_STATISTICS, false)
                .setServerOption(ALWAYS_SET_DATE, false)
                .setServerOption(ALWAYS_SET_KEEP_ALIVE, true)
                .setServerOption(RECORD_REQUEST_START_TIME, false),
            SSLContextFactory.usingDefaults()
        );
    }

    public UndertowServer( Undertow.Builder builder, SSLContextFactory sslContextFactory ) {
        this.builder = builder;
        this.sslContextFactory = sslContextFactory;
    }

    public UndertowServer listenHttpRequests( Config config ) {
        if ( config.getBoolean( "undertow.http.enabled" ) )
            listenHttpRequests(
                config.getInteger( "undertow.http.port", 9000 ),
                config.getString( "undertow.http.host", "0.0.0.0" )
            );
        return this;
    }

    public UndertowServer listenHttpRequests( int port ){
        return listenHttpRequests( port, "0.0.0.0" );
    }

    public UndertowServer listenHttpRequests( int port, String host ) {
        ensureIsNotStarted();
        builder.addListener( port, host);
        return this;
    }

    public UndertowServer listenHttpsRequests( Config config ) {
        if ( config.getBoolean( "undertow.https.enabled" ) )
            listenHttpsRequests(
                config.getInteger( "undertow.https.port", 9000 ),
                config.getString( "undertow.https.host", "0.0.0.0" ),
                config.getString( "undertow.https.keystore" ),
                config.getString( "undertow.https.keystore-password" ),
                config.getString( "undertow.https.truststore" ),
                config.getString( "undertow.https.truststore-password" )
            );
        return this;
    }

    public UndertowServer listenHttpsRequests( int port, String keyStore, String keystorePassword ) {
        return listenHttpsRequests( port, "0.0.0.0", keyStore, keystorePassword, null, null );
    }

    public UndertowServer listenHttpsRequests(
            int port,
            @NonNull String host,
            @NonNull String keyStore,
            @NonNull String keystorePassword,
            String trustStoreName,
            String trustStorePassword )
    {
        try {
            ensureIsNotStarted();
            val sslContext = sslContextFactory.createSSLContext(keyStore, keystorePassword, trustStoreName, trustStorePassword);
            builder.addHttpsListener(port, host, sslContext);
            return this;
        } catch ( IOException cause ) {
            throw new RuntimeException( cause );
        }
    }

    private void enableHttp2(Config config) {
        if ( config.getBoolean( "undertow.http2.enabled" ) )
            enableHttp2();
    }

    public UndertowServer enableHttp2(){
        ensureIsNotStarted();
        enable( ENABLE_HTTP2 );
        return this;
    }

    @SafeVarargs
    public final UndertowServer disable(Option<Boolean>...options){
        ensureIsNotStarted();
        for (val option : options) {
            configure( option, false );
        }
        return this;
    }

    @SafeVarargs
    public final UndertowServer enable(Option<Boolean>...options){
        ensureIsNotStarted();
        for (val option : options) {
            configure( option, true );
        }
        return this;
    }

    public <T> UndertowServer configure(Option<T> option, T value) {
        ensureIsNotStarted();
        builder.setServerOption( option, value );
        return this;
    }

    public UndertowServer requestHandler( RequestHandler handler ) {
        ensureIsNotStarted();
        builder.setHandler( handler.asHttpHandler() );
        return this;
    }

    public UndertowServer start(){
        ensureIsNotStarted();
        server = builder.build();
        Runtime.getRuntime().addShutdownHook( new Thread( this::stop ) );
        server.start();
        started = true;
        return this;
    }

    public UndertowServer stop(){
        if ( !started )
            throw new IllegalStateException("The server is not running.");
        server.stop();
        return this;
    }

    private void ensureIsNotStarted() {
        if ( started )
            throw new IllegalStateException("The server have been already started.");
    }

    public UndertowServer configure(Config config) {
        listenHttpRequests( config );
        listenHttpsRequests( config );
        enableHttp2( config );
        return this;
    }
}
