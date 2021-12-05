package dex.plugins

import groovy.json.JsonSlurper
import net.fabricmc.loader.api.SemanticVersion
import net.fabricmc.loader.api.metadata.version.VersionPredicate
import net.fabricmc.loader.impl.game.minecraft.McVersion
import net.fabricmc.loader.impl.util.version.SemanticVersionImpl
import net.fabricmc.loader.impl.util.version.VersionPredicateParser

import java.util.regex.Matcher
import java.util.regex.Pattern

class McVersionWorker {
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+\\.\\d+(\\.\\d+)?")
    protected ArrayList<LinkedHashMap> mcVersions
    protected Map<String, String> mcVer2Semver
    protected Map<String, Integer> mcVer2JavaVer
    private Tuple2<String, Set<String>> mcVersionsCache

    McVersionWorker() throws MalformedURLException {
        this.mcVer2Semver = new HashMap<>()
        this.mcVer2JavaVer = new HashMap<>()
        this.mcVersions = new JsonSlurper().parse(new URL("https://meta.fabricmc.net/v2/versions/game")) as ArrayList<LinkedHashMap>
    }

    /**
     * Get the latest MC version.
     *
     * @param isStable if snapshots should be ignored or not.
     * @return the MC version.
     */
    String getLatestMc(boolean isStable) {
        if (!isStable) return mcVersions.first().version
        for (LinkedHashMap mcVer : mcVersions) {
            if (mcVer.stable) return mcVer.version
        }
        return "" // No MC version was found
    }

    String getLatestMcForRange(boolean isStable, String range) {
        def x = getAcceptableMcVersions(range) // Pull list specific for the given range
        if (!isStable && x.contains(mcVersions.first().version)) return mcVersions.first().version
        for (LinkedHashMap mcVer : mcVersions) {
            if (mcVer.stable && x.contains(mcVer.version)) return mcVer.version
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
    LinkedHashSet<String> getAcceptableMcVersions(String range) throws MalformedURLException {
        if (mcVersionsCache != null && mcVersionsCache.first == range) {
            return mcVersionsCache.second
        }

        LinkedHashSet<String> list = new LinkedHashSet<String>()

        VersionPredicate x = VersionPredicateParser.parse(range)
        def mcVersions = new JsonSlurper().parse(new URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"))
        for (Object mcver : mcVersions.versions) {
            // Don't go below 1.14.4, mostly because change in version formatting
            if ("1.14.4" == (mcver.id as String)) break

            String semverMc = genMcVersionString(mcver.id as String, mcver.url as String)
            SemanticVersion v = new SemanticVersionImpl(semverMc, false)

            mcVer2Semver.put((String) mcver.get("id"), semverMc)

            if (x.test(v)) list.add(mcver.id as String)
        }

        mcVersionsCache = new Tuple2<>(range, list)
        return list
    }

    /**
     * Converts a given MC version to its semver format.
     *
     * @param version the version to convert.
     * @param metaURL the metadata URL to pull the major MC version from.
     * @return the semver-compatible version for the given MC version.
     */
    private String genMcVersionString(String version, String metaURL) {
        Tuple2<String, Integer> m = getMajorMcVersion(metaURL)
        String majorVersion = m.first
        mcVer2JavaVer.put(version, m.second)

        // Fixes minor versions that have prereleases
        Matcher match = VERSION_PATTERN.matcher(version)
        if (version.startsWith(majorVersion) && match.find(0)) {
            majorVersion = match.group()
        }

        McVersion.Builder builder = new McVersion.Builder()
        builder.setName(version)
        builder.setRelease(majorVersion)

        return builder.build().normalized
    }

    /**
     * Pulls the major game version from official launcher metadata's 'assets' key.
     *
     * @param metaURL the launcher meta URL for the version in question.
     * @return the major game version, such as '1.17'
     */
    static Tuple2<String, Integer> getMajorMcVersion(String metaURL) {
        try {
            def meta = new JsonSlurper().parse(new URL(metaURL))
            return new Tuple2<String, Integer>(meta.assets, meta.javaVersion.majorVersion ?: 8)
        } catch (MalformedURLException ignored) {
            return new Tuple2<String, Integer>("", 8)
        }
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
