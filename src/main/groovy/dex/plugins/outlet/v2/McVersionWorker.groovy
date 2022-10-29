package dex.plugins.outlet.v2

import dex.plugins.outlet.v2.util.FileUtil
import dex.plugins.outlet.v2.util.McOutletMeta
import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import net.fabricmc.loader.api.SemanticVersion
import net.fabricmc.loader.api.metadata.version.VersionPredicate
import net.fabricmc.loader.impl.util.version.SemanticVersionImpl
import net.fabricmc.loader.impl.util.version.VersionPredicateParser

import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.regex.Pattern

class McVersionWorker {
    private static final Pattern RELEASE_PATTERN = Pattern.compile("\\d+\\.\\d+(\\.\\d+)?")

    //protected ArrayList<LinkedHashMap> mcVersions
    protected Map<String, String> mcVer2Semver
    public Map<String, Integer> mcVer2JavaVer
    private Tuple2<String, Set<String>> mcVersionsCache

    McVersionWorker() throws MalformedURLException {
        this.mcVer2Semver = new LinkedHashMap<>()
        this.mcVer2JavaVer = new HashMap<>()
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
                    FileUtil.mc2FabricCacheArtifact().download(true)
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
    }

    /**
     * Get the latest MC version.
     *
     * @param isStable if snapshots should be ignored or not.
     * @return the MC version.
     */
    String getLatestMc(boolean isStable) {
        if (!isStable) return mcVer2Semver.keySet().first()
        mcVer2Semver.reverseEach { v ->
            if (RELEASE_PATTERN.matcher(v.key).matches()) {
                return v.key
            }
        }

        return "" // No MC version was found
    }

    String getLatestMcForRange(boolean isStable, String range) {
        def x = getAcceptableMcVersions(range, !isStable) // Pull list specific for the given range
        if (!isStable && x.contains(mcVer2Semver.keySet().last())) return mcVer2Semver.keySet().last()
        for (def mcVer : mcVer2Semver.keySet().asList().reverse()) {
            if (RELEASE_PATTERN.matcher(mcVer).matches() && x.contains(mcVer)) return mcVer
        }
        return "" // No MC version was found
    }

    /**
     * Return a list of acceptable MC versions based on the semver string in project.range.
     * Parsed from the official launcher metadata, could break at anytime.
     *
     * @return the list of acceptable MC versions
     */
    //todo respect snapshot control
    LinkedHashSet<String> getAcceptableMcVersions(String range, boolean allowSnapshots) throws MalformedURLException {
        if (mcVersionsCache != null && mcVersionsCache.first == range) {
            return mcVersionsCache.second
        }

        LinkedHashSet<String> list = new LinkedHashSet<String>()
        def min = SemanticVersion.parse("1.14.4")

        VersionPredicate x = VersionPredicateParser.parse(range)
        for (final def mcver in mcVer2Semver.entrySet().asList().reverse()) {
            //Don't go below 1.14.4, mostly because change in version formatting
            if (!allowSnapshots && !RELEASE_PATTERN.matcher(mcver.key).matches()) continue

            SemanticVersion v = new SemanticVersionImpl(mcver.value, false)

            //Don't go below 1.14.4, mostly because change in version formatting
            if (v <= min) {
                break
            }

            if (x.test(v)) list.add(mcver.key as String)
        }

        mcVersionsCache = new Tuple2<>(range, list)
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
