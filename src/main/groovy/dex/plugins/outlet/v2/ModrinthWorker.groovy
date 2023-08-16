package dex.plugins.outlet.v2

import dex.plugins.outlet.v2.util.FileUtil
import groovy.json.JsonSlurper

class ModrinthWorker {
    static def latestModVersion(String modNameOrId, Set<String> mcVersions, Set<String> loaders = ["fabric"], Boolean featuredOnly = null) {
        def modVersions = []
        def a = buildArtifact(modNameOrId, loaders, mcVersions, featuredOnly)
        a.download(true) // Attempt to update
        def f = a.fetchArtifact() // Get file
        if (f.text != null && f.text != "") {
            modVersions = new JsonSlurper().parseText(f.text)
        }

        if (modVersions instanceof ArrayList<LinkedHashMap>) {
            if (modVersions.isEmpty()) {
                return null
            }
            return modVersions.first().version_number
        }

        throw new IllegalStateException("Missing data entirely for Modrinth mod $modNameOrId")
    }

    private static def buildArtifact(String modNameOrId, Set<String> loaders, Set<String> mcVersions, Boolean featuredOnly) {
        def name = 'modrinth_' + modNameOrId + '_' + loaders.hashCode() + mcVersions.hashCode() + '_' + featuredOnly
        def url = "https://api.modrinth.com/v2/project/${modNameOrId}/version"

        def nextQuery = '?'
        if (mcVersions != null) {
            url += "${nextQuery}game_versions=${set2Url(mcVersions)}"
            nextQuery = '&'
        }

        if (loaders != null) {
            url += "${nextQuery}loaders=${set2Url(loaders)}"
            nextQuery = '&'
        }

        if (featuredOnly != null) {
            url += "${nextQuery}featured=${featuredOnly}"
            nextQuery = '&'
        }

        return FileUtil.buildArtifact(name, url)
    }

    private static def set2Url(Set<String> set) {
        def setString = "["
        boolean first = true
        for (String e : set) {
            if (first) {
                setString += '%22' + e + '%22'
                first = false
            } else {
                setString += "," + '%22' + e + '%22' // %22 = ", used to fix encoding issue
            }
        }
        setString += "]"

        return setString
    }
}
