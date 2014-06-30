package io.skullabs.undertow.standalone.mojo;

import io.skullabs.undertow.standalone.Main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;

/**
 * @goal run
 * @requiresDependencyResolution test
 */
public class UndertowRunnerMojo extends AbstractMojo {

	final static String SEPARATOR = System.getProperty( "path.separator" );

	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	MavenProject project;

	/**
	 * The profile configuration to load when running the server.
	 * 
	 * @parameter default-value=""
	 */
	String profile;

	/**
	 * @parameter default-value="${plugin}"
	 */
	PluginDescriptor plugin;

	/** @parameter default-value="${localRepository}" */
	ArtifactRepository localRepository;

	/**
	 * Used to construct artifacts for deletion/resolution...
	 * 
	 * @component
	 */
	ArtifactFactory factory;

	/**
	 * @component
	 */
	ArtifactResolver resolver;

	/**
	 * Name of the generated JAR.
	 * 
	 * @parameter alias="jarName" expression="${jar.finalName}"
	 *            default-value="${project.build.finalName}"
	 * @required
	 */
	String finalName;

	/**
	 * Directory containing the build files.
	 * 
	 * @parameter expression="${project.build.directory}"
	 */
	File buildDirectory;

	StringBuilder classPath = new StringBuilder();
	String standaloneJar;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			memorizeClassPathWithRunnableJar();
			String commandLineString = getCommandLineString();
			System.out.println( "CML: " + commandLineString );
			run( commandLineString );
		} catch ( Exception e ) {
			throw new MojoExecutionException( "Can't initialize Undertow Server.", e );
		}
	}

	@SuppressWarnings( "unchecked" )
	void memorizeClassPathWithRunnableJar()
			throws DependencyResolutionRequiredException, ArtifactResolutionException, ArtifactNotFoundException {
		for ( Resource element : (List<Resource>)this.project.getResources() )
			this.classPath.append( element.getDirectory() ).append( SEPARATOR );
		for ( String element : (List<String>)this.project.getCompileClasspathElements() )
			this.classPath.append( element ).append( SEPARATOR );
		for ( Artifact artifact : (List<Artifact>)this.project.getRuntimeArtifacts() )
			this.classPath.append(getArtifactAbsolutePath(artifact)).append( SEPARATOR );
		this.classPath
				.append( getFinalArtifactName() ).append( SEPARATOR )
				.append( resolveUndertowStadalone() );
	}

	String resolveUndertowStadalone() throws ArtifactResolutionException, ArtifactNotFoundException {
		Artifact undertowStandalone = getUndertowStandalone();
		return getArtifactAbsolutePath(undertowStandalone);
	}

	String getArtifactAbsolutePath(Artifact artifact)
			throws ArtifactResolutionException, ArtifactNotFoundException {
		this.resolver.resolve( artifact, Collections.EMPTY_LIST, this.localRepository );
		return artifact.getFile().getAbsolutePath();
	}

	Artifact getUndertowStandalone() {
		return this.factory.createDependencyArtifact(
				this.plugin.getGroupId(), "undertow-standalone",
				VersionRange.createFromVersion( this.plugin.getVersion() ), "jar", "", Artifact.SCOPE_RUNTIME );
	}

	String getFinalArtifactName() {
		String fileName = String.format( "%s.%s", this.finalName, this.project.getPackaging() );
		return new File( this.buildDirectory, fileName ).getAbsolutePath();
	}

	String getCommandLineString() {
		return String.format(
				"java -cp %s %s %s",
				this.classPath.toString(),
				Main.class.getCanonicalName(),
				this.profile != null ? this.profile : "" );
	}

	void run( String commandLineString ) throws IOException, InterruptedException {
		Process exec = Runtime.getRuntime().exec( commandLineString );
		printAsynchronously( exec.getInputStream() );
		printAsynchronously( exec.getErrorStream() );
		if ( exec.waitFor() > 0 )
			throw new RuntimeException( "The Undertow Server has failed to run." );
	}

	void printAsynchronously( InputStream stream ) {
		new Thread( new ProcessOutputPrinter( stream ) ).start();
	}

}