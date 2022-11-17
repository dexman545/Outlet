package dex.plugins.outlet.v2

import dex.plugins.outlet.v2.util.FileUtil
import dex.plugins.outlet.v2.util.McFabric
import dex.plugins.outlet.v2.util.McOutletMeta
import dex.plugins.outlet.v2.util.ReleaseType
import groovy.json.JsonOutput
import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import net.fabricmc.loader.api.metadata.version.VersionPredicate
import net.fabricmc.loader.impl.util.version.SemanticVersionImpl
import net.fabricmc.loader.impl.util.version.VersionPredicateParser

import java.time.ZoneId
import java.time.ZonedDateTime

class McVersionWorker {
    private List<McFabric> mcVersions
    private Tuple2<String, Set<String>> mcVersionsCache
    protected Map<String, String> mcVer2Semver
    public Map<String, Integer> mcVer2JavaVer

    McVersionWorker() throws MalformedURLException {
        this.mcVer2Semver = new LinkedHashMap<>()
        this.mcVer2JavaVer = new HashMap<>()
        this.mcVersions = new ArrayList<>()
        init()
    }

    private void init() {
        def f = FileUtil.mc2FabricCacheArtifact().fetchArtifact()

        def cachedData = new McOutletMeta(lastChanged: Date.from(ZonedDateTime.of(2012, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant()), versions: [])
        if (f.text != null && f.text != "") {
            cachedData = new JsonSlurper(type: JsonParserType.INDEX_OVERLAY).parseText(f.text) as McOutletMeta
        }

        def usedData = cachedData

        if (!FileUtil.mc2FabricCacheArtifact().isUpToDate()) {
            try {
                def newData = new JsonSlurper(type: JsonParserType.INDEX_OVERLAY).parse(new URL(FileUtil.mc2FabricCacheArtifact().url)) as McOutletMeta
                if (newData.lastChanged > cachedData.lastChanged) {
                    f.write(JsonOutput.prettyPrint(JsonOutput.toJson(newData)))
                    usedData = newData
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
        }

        usedData.versions.each { v ->
            mcVer2Semver.put(v.id, v.normalized)
            mcVer2JavaVer.put(v.id, v.javaVersion)
        }
        mcVersions.addAll(usedData.versions)
    }

    /**
     * Get the latest MC version.
     *
     * @param isStable if snapshots should be ignored or not.
     * @return the MC version.
     */
    String getLatestMc(Set<ReleaseType> allowedReleaseTypes) {
        def s = mcVersions.findAll {allowedReleaseTypes.contains(it.type as ReleaseType) /*Cast is needed for some reason*/ }
        if (s.empty) {
            throw new IllegalStateException("No valid MC versions for the given constraints.")
        }
        return s.last().id
    }

    String getLatestMcForRange(Set<ReleaseType> allowedReleaseTypes, String range) {
        def s = getAcceptableMcVersions(range, allowedReleaseTypes)
        if (s.empty) {
            throw new IllegalStateException("No valid MC versions for the given constraints.")
        }
        s.last()
    }

    /**
     * Return a list of acceptable MC versions based on the semver string in project.range.
     *
     * @return the list of acceptable MC versions
     */
    LinkedHashSet<String> getAcceptableMcVersions(String range, Set<ReleaseType> allowedReleaseTypes) throws MalformedURLException {
        if (mcVersionsCache != null && mcVersionsCache.first == (range + allowedReleaseTypes)) {
            return mcVersionsCache.second
        }

        VersionPredicate x = VersionPredicateParser.parse(range)

        def filteredVersions = mcVersions.findAll {allowedReleaseTypes.contains(it.type as ReleaseType) /*Cast is needed for some reason*/ }
        def list = filteredVersions.findAll { x.test(new SemanticVersionImpl(it.normalized, false)) }.collect {
            it.id
        } as LinkedHashSet

        mcVersionsCache = new Tuple2<>((range + allowedReleaseTypes), list)
        return list
    }

    LinkedHashSet<String> getMcVersionsForCurseforge(Set<String> versions) {
        LinkedHashSet<String> out = new LinkedHashSet<>()
        for (String version : versions) {
            out.add(fixSnapshot(this.mcVer2Semver.get(version), true))
        }

        return out
    }

    protected static String fixSnapshot(String ver, boolean appdendData) {
        if (ver == null) return ""
        String major = ver.split("-")[0]
        if (ver.contains("-")) {
            return major.substring(0, 4) + (appdendData ? "-Snapshot" : "")
        } else if (!appdendData) {
            return major.substring(0, 4) // Get major version for non-snapshots
        }
        return major
    }
}
