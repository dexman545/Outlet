package dex.plugins;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

public class OutletExtension {
    public String mcVersionRange;
    public boolean allowSnapshotsForProject = true;
    public boolean useLatestYarn = true;
    private FabricVersionWorker worker;
    private boolean isAlive = false;

    public OutletExtension() {
        try {
            this.worker = new FabricVersionWorker();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public List<String> mcVersions() {
        try {
            if (mcVersionRange == null) {
                System.out.println("Please specify an MC version range, such as '*' or '>=1.16-alpha.20.22.a'");
                return null;
            }
            isAlive = true;
            return worker.getAcceptableMcVersions(this.mcVersionRange);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Set<String> curseforgeMcVersions() {
        this.establishLiving();
        return worker.getMcVersionsForCurseforge(this.mcVersions());
    }

    public String latestMc() {
        return worker.getLatestMc(this.allowSnapshotsForProject);
    }

    public String loaderVersion() {
        this.establishLiving();
        try {
            return worker.getNewestLoaderVersion();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String yarnVersion() {
        this.establishLiving();
        try {
            return worker.getChosenYarnVersion(this.latestMc(), this.useLatestYarn);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String fapiVersion() {
        this.establishLiving();
        try {
            return worker.getLatestFapi(this.latestMc());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void establishLiving() {
        if (!this.isAlive) {
            this.mcVersions();
        }
    }
}
