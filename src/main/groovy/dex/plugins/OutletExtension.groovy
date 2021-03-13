package dex.plugins

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
    private FabricVersionWorker worker
    private boolean isAlive = false

    OutletExtension() {
        try {
            this.worker = new FabricVersionWorker()
        } catch (MalformedURLException e) {
            e.printStackTrace()
        }
    }

    /**
     * Get the set of Minecraft version strings
     * Can be used for automated Modrinth upload
     */
    Set<String> mcVersions() {
        try {
            if (mcVersionRange == null) {
                System.out.println("Please specify an MC version range, such as '*' or '>=1.16-alpha.20.22.a'")
                return null
            }
            isAlive = true
            return worker.getAcceptableMcVersions(this.mcVersionRange)
        } catch (MalformedURLException e) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Get the set of Minecraft version strings for automated curseforge upload
     */
    Set<String> curseforgeMcVersions() {
        this.establishLiving()
        return worker.getMcVersionsForCurseforge(this.mcVersions())
    }

    /**
     * Get the latest Minecraft version
     */
    String latestMc() {
        if (this.latestMcRespectsRange) {
            return worker.getLatestMcForRange(!this.allowSnapshotsForProject, this.mcVersionRange)
        } else {
            return worker.getLatestMc(!this.allowSnapshotsForProject)
        }
    }

    /**
     * Get the latest Fabric Loader version
     */
    String loaderVersion() {
        this.establishLiving()
        try {
            return worker.getNewestLoaderVersion()
        } catch (MalformedURLException e) {
            e.printStackTrace()
        }
        return null
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
        try {
            return worker.getChosenYarnVersion(mcVer, this.useLatestYarn)
        } catch (MalformedURLException e) {
            e.printStackTrace()
        }
        return null
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
        try {
            return worker.getLatestFapi(ver)
        } catch (Exception e) {
            e.printStackTrace()
        }
        return null
    }

    private void establishLiving() {
        if (!this.isAlive) {
            this.mcVersions()
        }
    }
}
