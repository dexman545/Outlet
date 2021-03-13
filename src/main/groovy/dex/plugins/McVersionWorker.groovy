package dex.plugins

import com.github.yuchi.semver.Direction
import com.github.yuchi.semver.Range
import com.github.yuchi.semver.Version
import groovy.json.JsonSlurper

import java.util.regex.Matcher
import java.util.regex.Pattern

class McVersionWorker {
    protected ArrayList<LinkedHashMap> mcVersions
    protected Map<String, String> mcVer2Semver

    McVersionWorker() throws MalformedURLException {
        this.mcVer2Semver = new HashMap<>()
        this.mcVersions = new JsonSlurper().parse(new URL("https://meta.fabricmc.net/v2/versions/game")) as ArrayList<LinkedHashMap>
        getAcceptableMcVersions("*")
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
    LinkedHashSet<String> getAcceptableMcVersions(String range) throws MalformedURLException {
        LinkedHashSet<String> list = new LinkedHashSet<String>()

        // Fragile
        def mcVersions = new JsonSlurper().parse(new URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"))
        for (Object mcver : mcVersions.versions) {
            // Don't go below 1.14.4, mostly because change in version formatting
            if ("1.14.4" == (mcver.id as String)) break

            Range r = Range.from(range.replace("-", ".0-"), false)

            String semverMc = genMcVersionString(mcver.id as String, mcver.url as String)
            Version v = Version.from(semverMc, false)

            if (v == null || r == null) {
                //System.out.println(String.format("The version was null for : %s", mcver.id))
                break
            }

            mcVer2Semver.put((String) mcver.get("id"), semverMc)

            // isOutside check is to fix java version of npm-semver not having includePrerelease flag, as by default
            // different prereleases from different minor versions will not match
            // NOTE: May break things (*may*)
            if (r.test(v) /*|| r.isOutside(v, Direction.LOW)*/) {
                list.add(mcver.id as String)
            }
        }

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
        // From Fabric Loader https://github.com/FabricMC/fabric-loader/blob/master/src/main/java/net/fabricmc/loader/minecraft/McVersionLookup.java
        Pattern RELEASE_PATTERN = Pattern.compile("\\d+\\.\\d+(\\.\\d+)?")
        Pattern PRE_RELEASE_PATTERN = Pattern.compile(".+(?:-pre| Pre-[Rr]elease )(\\d+)")
        Pattern RELEASE_CANDIDATE_PATTERN = Pattern.compile(".+(?:-rc| [Rr]elease Candidate )(\\d+)")
        Pattern SNAPSHOT_PATTERN = Pattern.compile("(?:Snapshot )?(\\d+)w0?(0|[1-9]\\d*)([a-z])")

        if (RELEASE_PATTERN.matcher(version).matches()) return version

        String majorVersion = getMajorMcVersion(metaURL) + ".0" // fix for parser

        Matcher snapshotMatcher = SNAPSHOT_PATTERN.matcher(version)
        if (snapshotMatcher.matches()) {
            return majorVersion + "-alpha." + snapshotMatcher.group(1) + "." + snapshotMatcher.group(2) + "." + snapshotMatcher.group(3)
        }

        Matcher rcMatcher = RELEASE_CANDIDATE_PATTERN.matcher(version)
        if (rcMatcher.matches()) {
            return majorVersion + "-rc." + rcMatcher.group(1)
        }

        Matcher preMatcher = PRE_RELEASE_PATTERN.matcher(version)
        if (preMatcher.matches()) {
            return majorVersion + "-beta." + preMatcher.group(1)
        }

        return ""
    }

    /**
     * Pulls the major game version from official launcher metadata's 'assets' key.
     *
     * @param metaURL the launcher meta URL for the version in question.
     * @return the major game version, such as '1.17'
     */
    static String getMajorMcVersion(String metaURL) {
        try {
            def meta = new JsonSlurper().parse(new URL(metaURL))
            return meta.assets
        } catch (MalformedURLException ignored) {
            return ""
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
            return major.replaceAll(".0", "") + (appdendData ? "-Snapshot" : "")
        } else if (!appdendData) {
            return major.substring(0, 4) // Get major version for non-snapshots
        }
        return major
    }
}
