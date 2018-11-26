package kikaha.undertow;

import lombok.SneakyThrows;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KikahaUndertowHttpServerTest {

    UndertowServer server = UndertowServer.usingDefaults()
        .listenHttpRequests( 8080 )
        .requestHandler( (e,n)-> {
            e.setStatusCode( 200 ).getResponseSender().send( "OK" );
            e.endExchange();
        });

    @Test @SneakyThrows
    void runServer(){
        val req = new Request.Builder().url( "http://localhost:8080" ).get().build();
        val client = new OkHttpClient.Builder().build();
        val resp = client.newCall( req ).execute();
        assertEquals( 200, resp.code() );
        assertEquals( "OK", resp.body().string() );
    }

    @BeforeEach
    void startServerForTesting(){
        server.start();
    }

    @AfterEach
    void stopServer(){
        server.stop();
    }
}
