import org.gradle.api.Plugin
import org.gradle.api.Project

class MiddlewareProjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(KotlinProjectPlugin::class.java)

        target.group = "com.bybutter.sisyphus.middleware"
    }
}