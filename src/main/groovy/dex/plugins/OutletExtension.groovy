package dex.plugins

import dex.plugins.outlet.v2.util.FileUtil
import org.gradle.api.Project

class OutletExtension {
    //todo make setting lazy https://tomgregory.com/introduction-to-gradle-plugins/
    /**
     * The Minecraft version range you would give Fabric Loader via your fabric.mod.json
     * Semver-esque string, use '*' for all versions
     */
    public String mcVersionRange
    /**
     * Whether {@see latestMc()} should include non-release versions of Minecraft
     */
    public boolean allowSnapshotsForProject = true
    /**
     * Whether {@see yarnVersion()} should return the latest yarn version or the earliest yarn version for a given
     * Minecraft version
     */
    public boolean useLatestYarn = true
    /**
     * Whether {@see latestMc()} should respect the version range
     */
    public boolean latestMcRespectsRange = true
    /**
     * The map of input properties to update in the given config file, defaults to gradle.properties.
     * Nondestructive update.
     */
    public Map<String, Object> propertiesData = new HashMap<>()
    /**
     * The properties file to read values from and optionally update, defaults to gradle.properties.
     * Nondestructive update.
     */
    public File propertiesFile = project.file('gradle.properties')
    /**
     * Whether Outlet should update the version properties file on successful compilation.
     * This also doubles as a side control. E.g., have this evaluate to true in your local dev environment,
     * but false on build server such as Github Actions.
     */
    public boolean maintainPropertiesFile = false
    /**
     * The map of input properties that correspond to what Outlet can produce.
     * By default, it contains the keys used by the Fabric Example Mod.
     *
     * These are used for reading in values from the properties file as a fallback in case of version fetch failure.
     *
     * These keys should be overridden for use in other environments.
     *
     * Looks for the following:
     * - fabric (as in Fabric API)
     * - loader (as in Fabric Loader)
     * - minecraft
     * - yarn
     * - java
     */
    public Map<String, String> propertyKeys = [java: 'java', fabric: 'fabric_version',
                                               yarn: 'yarn_mappings', minecraft: 'minecraft_version',
                                               loader: 'fabric_loader']

    protected dex.plugins.outlet.v2.FabricVersionWorker worker
    private boolean isAlive = false
    private Project project
    public boolean hasErrored = false

    OutletExtension(Project project) {
        this.project = project
        new FileUtil(project)
        try {
            this.worker = new dex.plugins.outlet.v2.FabricVersionWorker()
        } catch (Exception e) {
            e.printStackTrace()
            hasErrored = true
        }
    }

    /**
     * Get the set of Minecraft version strings
     * Can be used for automated Modrinth upload
     */
    Set<String> mcVersions() {
        if (!hasErrored) {
            try {
                if (mcVersionRange == null) {
                    System.out.println("Please specify an MC version range, such as '*' or '>=1.16-alpha.20.22.a'")
                    return null
                }
                isAlive = true
                return worker.getAcceptableMcVersions(this.mcVersionRange, allowSnapshotsForProject)
            } catch (MalformedURLException e) {
                e.printStackTrace()
            }
        }

        return null
    }

    /**
     * Get the set of Minecraft version strings for automated curseforge upload
     */
    Set<String> curseforgeMcVersions() {
        this.establishLiving()
        if (!hasErrored) {
            return worker.getMcVersionsForCurseforge(this.mcVersions())
        }
        throw new RuntimeException("Could not init. Outlet")
    }

    /**
     * Get the latest Minecraft version
     */
    String latestMc() {
        this.establishLiving()
        if (!hasErrored) {
            def v
            if (this.latestMcRespectsRange) {
                v = worker.getLatestMcForRange(!this.allowSnapshotsForProject, this.mcVersionRange)
            } else {
                v = worker.getLatestMc(!this.allowSnapshotsForProject)
            }

            if (v != "" || v != null) {
                return v
            }
        }

        return VersionCodec.readProperty(propertiesFile, propertyKeys, 'minecraft')
    }

    /**
     * Get the Java version for the latest MC version
     * Defaults to 8 if it cannot be found
     */
    Integer javaVersion() {
        javaVersion(latestMc())
    }

    /**
     * Get the Java version for the given MC version
     * Defaults to 8 if it cannot be found
     */
    Integer javaVersion(String mcVer) {
        this.establishLiving()
        if (!hasErrored) {
            return worker.mcVer2JavaVer.get(mcVer)
        }

        return 8
    }

    /**
     * Get the Java language compatibility level that all versions in {@see #mcVersions} can support
     * Defaults to 8 if it cannot be found
     */
    Integer javaLanguageCompatibility() {
        this.establishLiving()
        if (!hasErrored) {
            def minJava = worker.mcVer2JavaVer.get(mcVersions().first())
            mcVersions().forEach {
                def j = worker.mcVer2JavaVer.get(it)
                minJava = Math.min(minJava, j)
            }

            return minJava
        }

        return 8
    }

    /**
     * Get the latest Fabric Loader version
     */
    String loaderVersion() {
        this.establishLiving()
        if (!hasErrored) {
            try {
                return worker.getNewestLoaderVersion()
            } catch (MalformedURLException e) {
                e.printStackTrace()
            }
        }

        return VersionCodec.readProperty(propertiesFile, propertyKeys, 'loader')
    }

    /**
     * Get the Yarn version for the latest MC version
     */
    String yarnVersion() {
        return yarnVersion(this.latestMc())
    }

    /**
     * Get the Yarn version for the given MC version
     */
    String yarnVersion(String mcVer) {
        this.establishLiving()
        if (!hasErrored) {
            try {
                return worker.getChosenYarnVersion(mcVer, this.useLatestYarn)
            } catch (MalformedURLException e) {
                e.printStackTrace()
            }
        }

        return VersionCodec.readProperty(propertiesFile, propertyKeys, 'yarn')
    }

    /**
     * Get the latest Fabric API version for the latest MC version
     */
    String fapiVersion() {
        return fapiVersion(this.latestMc())
    }

    /**
     * Get the latest Fabric API version for the given MC version
     */
    String fapiVersion(String ver) {
        this.establishLiving()
        if (!hasErrored) {
            try {
                return worker.getLatestFapi(ver)//todo null handling
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
        return VersionCodec.readProperty(propertiesFile, propertyKeys, 'fabric')
    }

    private void establishLiving() {
        if (!this.isAlive && !hasErrored) {
            this.mcVersions()
        }
    }
}
