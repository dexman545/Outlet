package dex.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class TaskPropertiesUpdate extends DefaultTask{
    private final OutletExtension extension = getProject().getExtensions().getByType(OutletExtension.class)

    @TaskAction
    def apply() {
        onlyIf {
            extension.maintainPropertiesFile
        }

        doFirst {
            logger.lifecycle('Updating properties file...')
            VersionCodec.updateProperties(extension.propertiesFile, extension.propertiesData)
        }
    }
}
