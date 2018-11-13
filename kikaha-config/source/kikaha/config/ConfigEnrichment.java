package kikaha.config;

/**
 * Allow developers to enrich the current configuration with data loaded
 * from another sources.
 */
public interface ConfigEnrichment {

	/**
	 * Enrich the current configuration with data from another sources.
	 *
	 * @param mergeableConfig
	 * @return the enriched {@link Config}.
	 */
	Config enrich( Config mergeableConfig );
}
