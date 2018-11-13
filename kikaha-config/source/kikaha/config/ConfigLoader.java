package kikaha.config;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 *
 */
@UtilityClass
public class ConfigLoader {

	public MergeableConfig loadDefaults() {
		try {
			final MergeableConfig config = MergeableConfig.create();
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
			loadFiles(config, loader.getResources("META-INF/defaults.yml"));
			loadFiles(config, loader.getResources("conf/application.yml"));
			loadFiles(config, loader.getResources("conf/application-test.yml"));
			return config;
		} catch ( IOException cause ) {
			throw new IllegalStateException(cause);
		}
	}

	private void loadFiles( MergeableConfig config, Enumeration<URL> resources ) throws IOException {
		while( resources.hasMoreElements() ){
			final URL url = resources.nextElement();
			try (final InputStream stream = url.openStream() ) {
				config.load(stream);
			}
		}
	}
}
