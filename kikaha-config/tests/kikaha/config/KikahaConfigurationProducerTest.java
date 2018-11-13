package kikaha.config;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;

/**
 * Unit tests for KikahaConfigurationProducer.
 */
public class KikahaConfigurationProducerTest {

	KikahaConfigurationProducer producer;
	@Mock ConfigEnrichment enrichment;
	@Spy MergeableConfig enrichedMergeableConfig;

	@BeforeEach
	public void setupMocks(){
		MockitoAnnotations.initMocks(this);
		producer = spy( new KikahaConfigurationProducer(singletonList(enrichment)) );
	}

	@Test
	public void ensureThatCanEnrichConfig(){
		doReturn( enrichedMergeableConfig ).when( enrichment ).enrich( any() );

		val producedConfig = producer.produceAConfiguration();
		assertEquals( enrichedMergeableConfig, producedConfig );
	}
}