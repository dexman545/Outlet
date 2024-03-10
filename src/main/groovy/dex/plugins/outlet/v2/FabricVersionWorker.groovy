package dex.plugins.outlet.v2

import dex.plugins.outlet.v2.util.FileUtil
import groovy.json.JsonSlurper

class FabricVersionWorker {
    private final McVersionWorker versionWorker

    FabricVersionWorker(McVersionWorker versionWorker) {
        this.versionWorker = versionWorker
    }

    /**
     * Get the latest FAPI version.
     * Assumes latest is always added to bottom of maven pom.
     * May not be the specific version needed for a given MC version.
     *
     * @return latest fapi version. If not found, returns the version in gradle.properties
     */
    String getLatestFapi(String projectMcVer) {
        // Try lookup exact MC version build on Modrinth first
        try {
            def v = ModrinthWorker.latestModVersion('fabric-api', Collections.singleton(projectMcVer))
            if (v != null) {
                return v
            }
        } catch (IllegalStateException e) {
            e.printStackTrace()
        }

        def fapiVersions = null
        def a = FileUtil.fapiArtifact()
        a.download(true) // Attempt to update
        def f = a.fetchArtifact() // Get file
        if (f.text != null && f.text != "") {
            fapiVersions = new groovy.xml.XmlSlurper().parseText(f.text)
        }
        def mcv = versionWorker.mcVer2Semver.get(projectMcVer)
        if (mcv == null) return null
        String majorMc = versionWorker.fixSnapshot(mcv.substring(0, 4), false)
        for (String version : (fapiVersions.versioning.versions.version.list() as List).reverse()) {
            String target = version.split("\\+")[1]
            if (target.contains("build")) break // These versions are old and have useless metadata
            if (target.contains(majorMc)) return version
        }

        throw new IllegalStateException("Missing data entirely for FAPI")
    }

    /**
     * Get the newest available loader version.
     *
     * @return loader version.
     */
    static String getNewestLoaderVersion() {
        def loaderVersions = null
        def a = FileUtil.loaderArtifact()
        a.download(true) // Attempt to update
        def f = a.fetchArtifact() // Get file
        if (f.text != null && f.text != "") {
            loaderVersions = new JsonSlurper().parseText(f.text)
        }
        if (loaderVersions instanceof ArrayList<LinkedHashMap>) {
            return loaderVersions.first().version
        }

        throw new IllegalStateException("Missing data entirely for Loader")
    }

    /**
     * Get correct Yarn version for project's MC version.
     *
     * @param newest whether to return the newest available Yarn version, or the oldest. Oldest may be preferred to avoid
     * invalidating genSources' output.
     * @return the correct Yarn version.
     */
    static String getChosenYarnVersion(String ver, boolean newest) {
        def yarnVersions = null
        def a = FileUtil.yarnArtifact(ver)
        a.download(true) // Attempt to update
        def f = a.fetchArtifact() // Get file
        if (f.text != null && f.text != "") {
            yarnVersions = new JsonSlurper().parseText(f.text)
        }
        if (yarnVersions instanceof ArrayList<LinkedHashMap>) {
            return newest ? yarnVersions.first().version : yarnVersions.last().version
        }

        throw new IllegalStateException("Missing data entirely for Yarn of MC${ver}")
    }
}
