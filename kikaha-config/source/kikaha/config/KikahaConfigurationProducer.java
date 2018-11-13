package kikaha.config;

import injector.AllOf;
import injector.Producer;
import injector.Singleton;
import lombok.Getter;

/**
 * Make the default configuration widely available.
 */
@Singleton
public class KikahaConfigurationProducer {

	static final Config defaultConfiguration = ConfigLoader.loadDefaults();

	final Iterable<ConfigEnrichment> listOfEnrichment;

	@Getter( lazy = true )
	private final Config config = loadConfiguration();

	public KikahaConfigurationProducer(
		@AllOf( ConfigEnrichment.class ) Iterable<ConfigEnrichment> listOfEnrichment )
	{
		this.listOfEnrichment = listOfEnrichment;
	}

	private Config loadConfiguration() {
		Config config = defaultConfiguration;
		for ( final ConfigEnrichment enrichment : listOfEnrichment )
			config = enrichment.enrich( config );
		return config;
	}

	@Producer
	public Config produceAConfiguration(){
		return getConfig();
	}
}
