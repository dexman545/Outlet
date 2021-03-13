package dex.plugins

class OutletExtension {
    //todo make setting lazy https://tomgregory.com/introduction-to-gradle-plugins/
    public String mcVersionRange
    public boolean allowSnapshotsForProject = true
    public boolean useLatestYarn = true
    private FabricVersionWorker worker
    private boolean isAlive = false

    //todo add option for latest MC version to respect version range
    OutletExtension() {
        try {
            this.worker = new FabricVersionWorker()
        } catch (MalformedURLException e) {
            e.printStackTrace()
        }
    }

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

    Set<String> curseforgeMcVersions() {
        this.establishLiving()
        return worker.getMcVersionsForCurseforge(this.mcVersions())
    }

    String latestMc() {
        return worker.getLatestMc(!this.allowSnapshotsForProject)
    }

    String loaderVersion() {
        this.establishLiving()
        try {
            return worker.getNewestLoaderVersion()
        } catch (MalformedURLException e) {
            e.printStackTrace()
        }
        return null
    }

    String yarnVersion() {
        this.establishLiving()
        try {
            return worker.getChosenYarnVersion(this.latestMc(), this.useLatestYarn)
        } catch (MalformedURLException e) {
            e.printStackTrace()
        }
        return null
    }

    String fapiVersion() {
        this.establishLiving()
        try {
            return worker.getLatestFapi(this.latestMc())
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
