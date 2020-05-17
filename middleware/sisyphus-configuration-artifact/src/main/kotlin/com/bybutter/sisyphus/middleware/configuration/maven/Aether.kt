package com.bybutter.sisyphus.middleware.configuration.maven

import java.io.File
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResult
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.resolution.DependencyResult
import org.eclipse.aether.resolution.VersionRangeRequest
import org.eclipse.aether.resolution.VersionRangeResult
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.wagon.WagonProvider
import org.eclipse.aether.transport.wagon.WagonTransporterFactory
import org.eclipse.aether.util.repository.AuthenticationBuilder
import org.eclipse.aether.util.version.GenericVersionScheme

class Aether {
    private val system: RepositorySystem
    private val session: RepositorySystemSession
    private val repositories: MutableList<RemoteRepository> = mutableListOf()
    private val localRepository: RemoteRepository

    init {
        val userHome = System.getProperty("user.home")
        val localRepositoryDir = File("$userHome/.m2/repository")

        localRepository = RemoteRepository.Builder("local", "dafult", localRepositoryDir.toURI().toURL().toExternalForm()).build()

        val locator = MavenRepositorySystemUtils.newServiceLocator()
        locator.addService(RepositoryConnectorFactory::class.java, BasicRepositoryConnectorFactory::class.java)
        locator.addService(TransporterFactory::class.java, WagonTransporterFactory::class.java)
        locator.addService(WagonProvider::class.java, SimpleWagonProvider::class.java)

        system = locator.getService(RepositorySystem::class.java)

        val localRepo = LocalRepository(localRepositoryDir)
        session = MavenRepositorySystemUtils.newSession()
        session.localRepositoryManager = system.newLocalRepositoryManager(session, localRepo)
    }

    fun registerLocal(): RemoteRepository {
        repositories.add(localRepository)
        return localRepository
    }

    fun registerMavenCentral(): RemoteRepository {
        return registerRepository("https://repo.maven.apache.org/maven2/")
    }

    fun registerJCenter(): RemoteRepository {
        return registerRepository("https://jcenter.bintray.com/")
    }

    fun registerRepository(url: String, user: String? = null, password: String? = null): RemoteRepository {
        val authentication = if (user != null && password != null) {
            AuthenticationBuilder().addUsername(user).addPassword(password).build()
        } else null

        val result = RemoteRepository.Builder("", "default", url).apply {
            if (authentication != null) {
                setAuthentication(authentication)
            }
        }.build()

        repositories.add(result)
        return result
    }

    fun resolveArtifact(name: String): ArtifactResult {
        var artifact: Artifact = DefaultArtifact(name)
        val versionConstraint = versionScheme.parseVersionConstraint(artifact.version)
        if (versionConstraint.range != null) {
            val result = resolveVersionRange(name)
            artifact = artifact.setVersion(result.highestVersion.toString())
        }

        val artifactRequest = ArtifactRequest().apply {
            setArtifact(artifact)
            for (repository in this@Aether.repositories) {
                addRepository(repository)
            }
        }

        return system.resolveArtifact(session, artifactRequest)
    }

    fun resolveVersionRange(name: String): VersionRangeResult {
        val artifact: Artifact = DefaultArtifact(name)
        val versionRangeRequest = VersionRangeRequest().apply {
            setArtifact(artifact)
            for (repository in this@Aether.repositories) {
                addRepository(repository)
            }
        }

        return system.resolveVersionRange(session, versionRangeRequest)
    }

    fun resolveDependencies(name: String): DependencyResult {
        var artifact: Artifact = DefaultArtifact(name)
        val versionConstraint = versionScheme.parseVersionConstraint(artifact.version)
        if (versionConstraint.range != null) {
            val result = resolveVersionRange(name)
            artifact = artifact.setVersion(result.highestVersion.toString())
        }

        val dependency = Dependency(artifact, "compile")
        val collectRequest = CollectRequest().apply {
            root = dependency
            for (repository in this@Aether.repositories) {
                addRepository(repository)
            }
        }
        val collectResult = system.collectDependencies(session, collectRequest)

        val dependencyRequest = DependencyRequest().apply {
            root = collectResult.root
        }
        return system.resolveDependencies(session, dependencyRequest)
    }

    companion object {
        private val versionScheme = GenericVersionScheme()
    }
}
