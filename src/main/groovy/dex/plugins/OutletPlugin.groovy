package dex.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class OutletPlugin  implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.getExtensions().create("outlet", OutletExtension.class)
    }
}
