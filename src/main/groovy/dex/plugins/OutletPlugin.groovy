package dex.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.PluginAware
import org.gradle.api.tasks.TaskContainer

class OutletPlugin implements Plugin<PluginAware> {
    @Override
    void apply(final PluginAware pluginAware) {
        if (pluginAware instanceof Project) {
            apply((Project)pluginAware)
        } else if (pluginAware instanceof Settings) {
            apply((Settings)pluginAware)
        }
    }

    void apply(final Project project) {
        def ext = project.getExtensions().create("outlet", OutletExtension.class, project)

        project.afterEvaluate {ep ->
            TaskContainer tasks = ep.getTasks()
            def task = tasks.register("outletPropertiesUpdate", TaskPropertiesUpdate.class).get()

            task.setGroup("outlet")
            task.setDescription('Updates properties file of versions')
            task.onlyIf {ext.maintainPropertiesFile}

            // This handles runClient and Idea run configs
            tasks.classes.finalizedBy task
        }
    }

    void apply(final Settings settings) {//todo will this conflict with other setting in project?
        def ext = settings.getExtensions().create("outlet", OutletExtension.class, settings)
    }
}
