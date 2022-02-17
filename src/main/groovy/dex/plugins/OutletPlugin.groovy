package dex.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer

class OutletPlugin implements Plugin<Project> {
    @Override
    void apply(final Project project) {
        project.getExtensions().create("outlet", OutletExtension.class, project)

        project.afterEvaluate {ep ->
            TaskContainer tasks = ep.getTasks()
            def task = tasks.register("outletPropertiesUpdate", TaskPropertiesUpdate.class)

            task.setGroup("outlet")
            task.setDescription('Updates properties file of versions')

            // This handles runClient and Idea run configs
            tasks.classes.finalizedBy task
        }
    }
}
