import com.bybutter.sisyphus.project.gradle.SisyphusProjectPlugin
import nebula.plugin.publishing.maven.MavenPublishPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlatformPlugin
import org.gradle.plugins.ide.idea.IdeaPlugin

class BomProjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(IdeaPlugin::class.java)
        target.plugins.apply(JavaPlatformPlugin::class.java)
        target.plugins.apply(SisyphusProjectPlugin::class.java)
        target.plugins.apply(MavenPublishPlugin::class.java)

        target.group = "com.bybutter.sisyphus"
    }
}