package kikaha.undertow;

import lombok.SneakyThrows;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.*;
import java.security.cert.CertificateException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KikahaUndertowHttpsServerTest {

    UndertowServer server = UndertowServer.usingDefaults()
        .listenHttpsRequests( 8080, "tests-resources/server.keystore", "password" )
        .requestHandler( (e,n)-> {
            val scheme = e.getRequestScheme();
            e.setStatusCode( 200 ).getResponseSender().send( scheme );
            e.endExchange();
        });

    @Test
    @SneakyThrows
    void runServer(){
        val req = new Request.Builder().url( "https://localhost:8080" ).get().build();
        val resp = createClient().newCall( req ).execute();
        assertEquals( 200, resp.code() );
        assertEquals( "https", resp.body().string() );
    }

    @SneakyThrows
    OkHttpClient createClient(){
        val trustManager = new TrustEveryoneManager();
        val trustAll = new TrustManager[]{ trustManager };

        val sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAll, new java.security.SecureRandom());
        val sslSocketFactory = sslContext.getSocketFactory();

        return new OkHttpClient.Builder()
                .sslSocketFactory( sslSocketFactory, trustManager )
                .hostnameVerifier( new TrustEveryoneHostnameVerifier() )
                .build();
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

class TrustEveryoneManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException { }

    @Override
    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException { }

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new java.security.cert.X509Certificate[]{};
    }
}

class TrustEveryoneHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String s, SSLSession sslSession) {
        return true;
    }
}