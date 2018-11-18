package kikaha.undertow;

import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.server.protocol.http.HttpServerConnection;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Protocols;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.xnio.OptionMap;
import org.xnio.StreamConnection;
import org.xnio.XnioIoThread;
import org.xnio.conduits.ConduitStreamSinkChannel;
import org.xnio.conduits.ConduitStreamSourceChannel;
import org.xnio.conduits.StreamSinkConduit;
import org.xnio.conduits.StreamSourceConduit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import static org.mockito.Mockito.mock;

@UtilityClass
public class HttpServerExchangeStub {

	public static HttpServerExchange createHttpExchange() {
		final ServerConnection serverConnection = createServerConnection();
		final HttpServerExchange httpExchange = newHttpExchange( serverConnection );
		final Sender sender = mock( Sender.class );
		final Exposed exposed = new Exposed( httpExchange );
		exposed.setFieldValue( "sender", sender );
		return httpExchange;
	}

	private static HttpServerExchange newHttpExchange( final ServerConnection serverConnection ) {
		final HttpServerExchange httpServerExchange = new HttpServerExchange( serverConnection, new HeaderMap(), new HeaderMap(), 200 );
		httpServerExchange.setRequestMethod( new HttpString( "GET" ) );
		httpServerExchange.setProtocol( Protocols.HTTP_1_1 );
		httpServerExchange.setRelativePath("/test");
		return httpServerExchange;
	}

	private static ServerConnection createServerConnection(){
		final StreamConnection streamConnection = createStreamConnection();
		final OptionMap options = OptionMap.EMPTY;
		return new HttpServerConnection( streamConnection, null, null, options, 0, null );
	}

	@SneakyThrows
	private static StreamConnection createStreamConnection() {
		final StreamConnection streamConnection = Mockito.mock( StreamConnection.class );
		final ConduitStreamSinkChannel sinkChannel = createSinkChannel();
		Mockito.when( streamConnection.getSinkChannel() ).thenReturn( sinkChannel );
		final ConduitStreamSourceChannel sourceChannel = createSourceChannel();
		Mockito.when( streamConnection.getSourceChannel() ).thenReturn( sourceChannel );
		final XnioIoThread ioThread = Mockito.mock( XnioIoThread.class );
		Mockito.when( streamConnection.getIoThread() ).thenReturn( ioThread );
		return streamConnection;
	}

	private static ConduitStreamSinkChannel createSinkChannel() throws IOException {
		final StreamSinkConduit sinkConduit = Mockito.mock( StreamSinkConduit.class );
		Mockito.when( sinkConduit.write( Matchers.any( ByteBuffer.class ) ) ).thenReturn( 1 );
		final ConduitStreamSinkChannel sinkChannel = new ConduitStreamSinkChannel( null, sinkConduit );
		return sinkChannel;
	}

	private static ConduitStreamSourceChannel createSourceChannel() {
		final StreamSourceConduit sourceConduit = Mockito.mock( StreamSourceConduit.class );
		final ConduitStreamSourceChannel sourceChannel = new ConduitStreamSourceChannel( null, sourceConduit );
		return sourceChannel;
	}

	public static void setRequestHost( HttpServerExchange exchange, String host, int port ) {
		exchange.getRequestHeaders().put( Headers.HOST, host );
		exchange.setDestinationAddress( new InetSocketAddress( host, port ) );
	}
}

