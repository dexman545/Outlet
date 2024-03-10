package dex.plugins.outlet.v2

import dex.plugins.outlet.v2.util.FileUtil
import dex.plugins.outlet.v2.util.ReleaseType
import groovy.json.JsonSlurper

class ParchmentVersionWorker {
    private final McVersionWorker versionWorker

    ParchmentVersionWorker(McVersionWorker versionWorker) {
        this.versionWorker = versionWorker
    }

    Tuple2<String, String> getLatestParchment(String mcVer) {
        def mcv = versionWorker.mcVer2Semver.get(mcVer)
        if (mcv == null) {
            mcv = mcVer
        }

        def pMcs = getParchmentMcVersions()
        if (pMcs.contains(mcVer)) {
            def lp = getLatestParchment0(mcv)

            // MC version was in the list, but parchment hasn't made a stable release yet
            if (lp != null) {
                return new Tuple2<String, String>(mcVer, lp)
            }
        }

        // Map to nearest MC version
        def futureMc = mcv.split("-")[0]
        if (pMcs.contains(futureMc)) {
            return new Tuple2<String, String>(futureMc, getLatestParchment0(futureMc))
        }

        def pastMc = versionWorker.getAcceptableMcVersions("<${futureMc}", [ReleaseType.RELEASE] as Set).last()
        if (pMcs.contains(pastMc)) {
            return new Tuple2<String, String>(pastMc, getLatestParchment0(pastMc))
        }

        return null
    }

    private String getLatestParchment0(String normalizedMcVersion) {
        def parchmentVersions = null
        def a = FileUtil.parchmentMcArtifact(normalizedMcVersion)
        a.download(true) // Attempt to update
        def f = a.fetchArtifact() // Get file
        if (f.text != null && f.text != "") {
            parchmentVersions = new groovy.xml.XmlSlurper().parseText(f.text)
        }

        return parchmentVersions.versioning.release
    }

    static List<String> getParchmentMcVersions() {
        def parchmentVersions = null
        def a = FileUtil.parchmentVersionArtifact()
        a.download(true) // Attempt to update
        def f = a.fetchArtifact() // Get file
        if (f.text != null && f.text != "") {
            parchmentVersions = new JsonSlurper().parseText(f.text)
        }
        if (parchmentVersions != null) {
            return parchmentVersions.releases.keySet().asList()
        }

        throw new IllegalStateException("Missing data entirely for parchment-mc_versions")
    }

}
