package dex.plugins.outlet.v2

import dex.plugins.outlet.v2.util.FileUtil

class NeoForgeVersionWorker extends FabricVersionWorker {
    NeoForgeVersionWorker() throws MalformedURLException {
        super()
    }

    /**
     * Get the latest NeoForge version.
     * Assumes latest is always added to bottom of maven pom.
     *
     * @return latest neoforge version. If not found, returns the version in gradle.properties
     */
    String getLatestNeoforge(String projectMcVer) {
        def neoforgeVersions = null
        def a = FileUtil.neoforgeArtifact()
        a.download(true) // Attempt to update
        def f = a.fetchArtifact() // Get file
        if (f.text != null && f.text != "") {
            neoforgeVersions = new XmlSlurper().parseText(f.text)
        }
        def mcv = mcVer2Semver.get(projectMcVer)
        if (mcv == null) return null

        // Kinda hacky way to check if this is a snapshot
        if (mcv != projectMcVer) {
            return null // Neo doesn't do MC snapshots
        }

        // All neo versions start with the minor and patch of a MC version
        // https://docs.neoforged.net/docs/gettingstarted/versioning/#neoforge
        def startOfVersionString = mcv.replaceFirst("1\\.", "") + '.'

        for (String version : (neoforgeVersions.versioning.versions.version.list() as List).reverse()) {
            if (version.startsWith(startOfVersionString)) return version
        }

        return null
    }
}
