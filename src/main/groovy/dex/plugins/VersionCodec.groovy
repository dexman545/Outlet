package dex.plugins

import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.io.FileHandler

class VersionCodec {

    /**
     * Updates the property files with the latest versions the project is using
     */
    static def updateProperties(File properties, Map<String, Object> propertiesData) {
        def versionPropsFile = properties

        def config = new PropertiesConfiguration()
        def fileHandler = new FileHandler(config)
        fileHandler.file = versionPropsFile
        fileHandler.load()

        propertiesData.each { k, v ->
            config.setProperty(k, v)
        }

        fileHandler.save()
    }

    /**
     * Updates the property files with the latest versions the project is using
     */
    static def readProperty(File properties, Map<String, String> propertiesKeys, String property) {
        def versionPropsFile = properties

        def config = new PropertiesConfiguration()
        def fileHandler = new FileHandler(config)
        fileHandler.file = versionPropsFile
        fileHandler.load()

        return config.getProperty(propertiesKeys.get(property, property))
    }

    //todo task and extension stuff
    //todo make version workers return what was found in properties based on a switch

}
