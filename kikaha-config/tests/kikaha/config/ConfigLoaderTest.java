package kikaha.config;


import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
public class ConfigLoaderTest {

	@Test
	public void ensureThatCanReadRootPathConfigurationFromYaml() throws IOException {
		final MergeableConfig defaultConfig = MergeableConfig.create().load(new File("tests-resources/conf/snippet1.yml"));
		final String name = defaultConfig.getString("name");
		assertEquals( "Kikaha", name );
	}

	@Test
	public void ensureThatCanReadMultiLevelPathConfigurationFromYaml() throws IOException {
		final MergeableConfig defaultConfig = MergeableConfig.create().load(new File("tests-resources/conf/snippet1.yml"));
		final String host = defaultConfig.getString("server.host");
		assertEquals( "0.0.0.0", host );
	}

	@Test
	public void ensureThatCanReadConfigurationListFromYaml() throws IOException {
		final MergeableConfig defaultConfig = MergeableConfig.create().load(new File("tests-resources/conf/snippet1.yml"));
		final List<Config> configList = defaultConfig.getConfigList("server.routes");
		assertNotNull( configList );
		assertEquals( 2l, configList.size() );
		assertEquals( "/", configList.get(0).getString("home.path")  );
	}

	@Test
	public void ensureThatCanReadStringListFromYaml() throws IOException {
		final MergeableConfig defaultConfig = MergeableConfig.create().load(new File("tests-resources/conf/snippet1.yml"));
		final List<String> configList = defaultConfig.getStringList("former-authors");
		assertNotNull( configList );
		assertEquals( 3l, configList.size() );
		assertEquals( "Miere", configList.get(0) );
		assertEquals( "Ricardo", configList.get(1) );
		assertEquals( "Cesar", configList.get(2) );
	}

	@Test
	public void ensureThatCanReadTheKeysOfYamlFile() throws IOException {
		final MergeableConfig defaultConfig = MergeableConfig.create().load(new File("tests-resources/conf/snippet1.yml"));
		final Set<String> keys = defaultConfig.getKeys();
		assertNotNull(keys);
		assertEquals(4, keys.size());
	}

	@Test
	public void ensureThatCanMergeYamlFiles() throws IOException {
		final MergeableConfig defaultConfig = MergeableConfig.create();
		defaultConfig.load(new File("tests-resources/conf/snippet1.yml"));
		assertFalse( defaultConfig.getBoolean("server.user-ssl") );
		assertEquals( 9000, defaultConfig.getInteger("server.port") );
		assertEquals( "0.0.0.0", defaultConfig.getString("server.host") );

		defaultConfig.load(new File("tests-resources/conf/snippet2.yml"));
		assertTrue(defaultConfig.getBoolean("server.user-ssl") );
		assertEquals( 9001, defaultConfig.getInteger("server.port") );
		assertEquals( "0.0.0.0", defaultConfig.getString("server.host") );

		final List<String> authors = defaultConfig.getStringList("former-authors");
		assertEquals( 4, authors.size() );
		assertEquals( "Included at last", authors.get( authors.size() - 1 ) );
	}

	@Test
	public void ensureThatCanReadAllConfigurationFilesFromConfigLoader() throws IOException {
		final Config config = ConfigLoader.loadDefaults();
		assertEquals( "Test", config.getString("server.name") );
		assertTrue( config.getBoolean( "server.defaults" ) );
		assertTrue( config.getBoolean( "server.application" ) );
		assertTrue( config.getBoolean( "server.application-test" ) );
	}

	@Test
	public void ensureThatCanReadConfigStructure() throws IOException {
		final MergeableConfig config = MergeableConfig.create();
		config.load(new File("tests-resources/conf/snippet1.yml"));

		Config structureConfig = config.getConfig("structure");
		assertNotNull( structureConfig );
		assertTrue( structureConfig.getBoolean("enabled") );
		assertFalse( structureConfig.getBoolean( "disabled" ) );
	}

	@Test
	public void ensureThatCanMergeMultipleConfFilesIntoOneSingleYamlFile() throws IOException {
		final MergeableConfig config = MergeableConfig.create();
		config.load(new File("tests-resources/conf/snippet1.yml"));
		config.load(new File("tests-resources/conf/snippet2.yml"));

        final byte[] bytes = Files.readAllBytes(Paths.get("tests-resources/conf/expected-merged-snippet.yml"));
        final String expectedFile = new String(bytes);
        assertEquals( expectedFile, config.toString() );
    }

    @Test
    public void ensureCanReadAMergedYamlFile() throws IOException {
        final MergeableConfig config = MergeableConfig.create();
        config.load(new File("tests-resources/conf/expected-merged-snippet.yml"));
        final Map<String, Object> stringObjectMap = config.toMap();
        assertNotNull( stringObjectMap );
        assertEquals( 4, stringObjectMap.size() );
    }
}
