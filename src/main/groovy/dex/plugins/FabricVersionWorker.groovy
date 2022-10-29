package dex.plugins

import groovy.json.JsonSlurper

@Deprecated
class FabricVersionWorker extends McVersionWorker {
    FabricVersionWorker() throws MalformedURLException {
        super()
    }

    /**
     * Get the latest FAPI version.
     * Assumes latest is always added to bottom of maven pom.
     * May not be the specific version needed for a given MC version.
     *
     * @return latest fapi version. If not found, returns the version in gradle.properties
     */
    String getLatestFapi(String projectMcVer) throws Exception {
        def fapiVersions = new XmlSlurper().parseText(
                new URL("https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml").text)
        def mcv = mcVer2Semver.get(projectMcVer)
        if (mcv == null) return null
        String majorMc = fixSnapshot(mcv.substring(0, 4), false)
        for (String version : (fapiVersions.versioning.versions.version.list() as List).reverse()) {
            String target = version.split("\\+")[1]
            if (target.contains("build")) break // These versions are old and have useless metadata
            if (target.contains(majorMc)) return version
        }

        return null
    }

    /**
     * Get the newest available loader version.
     *
     * @return loader version.
     */
    static String getNewestLoaderVersion() throws MalformedURLException {
        def loaderVersions = new JsonSlurper().parse(new URL("https://meta.fabricmc.net/v2/versions/loader/"))
        assert loaderVersions instanceof ArrayList<LinkedHashMap> // Type check to to remove visual errors
        return loaderVersions.first().version
    }

    /**
     * Get correct Yarn version for project's MC version.
     *
     * @param newest whether to return the newest available Yarn version, or the oldest. Oldest may be preferred to avoid
     * invalidating genSources' output.
     * @return the correct Yarn version.
     */
    static String getChosenYarnVersion(String ver, boolean newest) throws MalformedURLException {
        def yarnVersions = new JsonSlurper().parse(
                new URL("https://meta.fabricmc.net/v2/versions/yarn/${ver}"))
        assert yarnVersions instanceof ArrayList<LinkedHashMap> // Type check to to remove visual errors
        return newest ? yarnVersions.first().version : yarnVersions.last().version
    }
}
