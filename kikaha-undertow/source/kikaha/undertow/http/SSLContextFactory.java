package kikaha.undertow.http;

import lombok.RequiredArgsConstructor;
import org.xnio.IoUtils;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashSet;

/**
 * Creates SSLContext for SSL environments.
 *
 * @author Miere Teixeira
 */
@RequiredArgsConstructor
public class SSLContextFactory {

	final String certificateSecurityProvider;
	final String keystoreSecurityProvider;

	public static SSLContextFactory usingDefaults(){
		return new SSLContextFactory( "TLS", "JKS" );
	}

	/**
	 * Create a SSLContext for a given {@code keystore} file.
	 *
	 * @param keyStoreName
	 * @param keystorePassword
	 * @return
	 * @throws IOException
	 */
	public SSLContext createSSLContext( final String keyStoreName, final String keystorePassword ) throws IOException {
		return createSSLContext( keyStoreName, keystorePassword, null, null );
	}

	/**
	 * Create a SSLContext for a given {@code truststore} and {@code keystore} files.
	 * 
	 * @param keyStoreName
	 * @param trustStoreName
	 * @param keystorePassword
	 * @return
	 * @throws IOException
	 */
	public SSLContext createSSLContext(
		final String keyStoreName, final String keystorePassword, final String trustStoreName, final String trustStorePassword )
			throws IOException {
		final KeyStore keystore = loadKeyStore( keyStoreName, keystorePassword );
		final KeyStore truststore = loadKeyStore( trustStoreName, trustStorePassword );
		return createSSLContext( keystore, truststore, keystorePassword );
	}

	private KeyStore loadKeyStore( final String name, final String password ) throws IOException {
		if ( name == null || name.isEmpty() )
			return null;

		final InputStream stream = openFile( name );
		if ( stream == null ){
			final String msg = "Could not open " + name + " certificate.";
			throw new IOException( msg );
		}

		return loadKeyStore( stream, password );
	}

	private InputStream openFile( final String name ) {
		try {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream( name );

			if ( inputStream == null ) {
				inputStream = new FileInputStream(name);
			}

			return inputStream;
		} catch ( FileNotFoundException e ) {
			return null;
		}
	}

	private KeyStore loadKeyStore( final InputStream stream, final String password ) throws IOException {
		try {
			final KeyStore loadedKeystore = KeyStore.getInstance( keystoreSecurityProvider );
			loadedKeystore.load( stream, password.toCharArray() );
			return loadedKeystore;
		} catch ( KeyStoreException | NoSuchAlgorithmException | CertificateException e ) {
			showAvailableSecurityProviders();
			throw new IOException("Unable to load KeyStore", e);
		} catch ( IOException e ){
			showAvailableSecurityProviders();
			throw e;
		} finally {
			IoUtils.safeClose( stream );
		}
	}

	private void showAvailableSecurityProviders(){
		HashSet<String> strings = new HashSet<>();
		for ( Provider provider : Security.getProviders() ) {
			for ( Provider.Service serviceProvider : provider.getServices() )
				strings.add( serviceProvider.getAlgorithm() );
		}
	}

	public SSLContext createSSLContext( 
		final KeyStore keyStore, final KeyStore trustStore, final String keystorePassword )
			throws IOException
	{
		final KeyManager[] keyManagers = createKeyManagers( keyStore, keystorePassword );
		final TrustManager[] trustManagers = createTrustManagers( trustStore );
		return createSSLContext( keyManagers, trustManagers );
	}

	private SSLContext createSSLContext( KeyManager[] keyManagers, TrustManager[] trustManagers ) throws IOException {
		try {
			SSLContext sslContext = SSLContext.getInstance( certificateSecurityProvider );
			sslContext.init( keyManagers, trustManagers, null );
			return sslContext;
		} catch ( NoSuchAlgorithmException | KeyManagementException e ) {
			throw new IOException( "Unable to create and initialise the SSLContext", e );
		}
	}

	private TrustManager[] createTrustManagers( final KeyStore trustStore ) throws IOException {
		if ( trustStore == null )
			return null;
		try {
			final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
			trustManagerFactory.init( trustStore );
			return trustManagerFactory.getTrustManagers();
		} catch ( NoSuchAlgorithmException | KeyStoreException e ) {
			throw new IOException( "Unable to initialise TrustManager[]", e );
		}
	}

	private KeyManager[] createKeyManagers( final KeyStore keyStore, final String keystorePassword ) throws IOException {
		try {
			final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
			keyManagerFactory.init( keyStore, keystorePassword.toCharArray() );
			return keyManagerFactory.getKeyManagers();
		} catch ( NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e ) {
			throw new IOException( "Unable to initialise KeyManager[]", e );
		}
	}
}
