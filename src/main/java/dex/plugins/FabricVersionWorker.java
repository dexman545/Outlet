package dex.plugins;

import groovy.json.JsonSlurper;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.util.slurpersupport.NodeChild;
import groovy.util.slurpersupport.NodeChildren;
import org.apache.groovy.json.internal.LazyMap;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FabricVersionWorker extends McVersionWorker {

    public FabricVersionWorker() throws MalformedURLException {
        super();
    }

    /**
     * Get the latest FAPI version.
     * Assumes latest is always added to bottom of maven pom.
     * May not be the specific version needed for a given MC version.
     *
     * @return latest fapi version. If not found, returns the version in gradle.properties
     */
    String getLatestFapi(String projectMcVer) throws Exception {
        GPathResult fapiVersions = new XmlSlurper().parse("https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml");
        List<NodeChild> fvers = ((NodeChildren) ((GPathResult) ((GPathResult) fapiVersions.getProperty("versioning")).getProperty("versions")).getProperty("version")).list();
        Collections.reverse(fvers);
        AtomicReference<String> out = new AtomicReference<>();

        this.mcVer2Semver.forEach((id, sem) -> {
            if (projectMcVer.equals(id)) {
                String majorMc = fixSnapshot(sem, false);
                for (NodeChild version : fvers) {
                    String target = version.text().split("\\+")[1];
                    if (target.contains("build")) break; // These versions are old and have useless metadata
                    if (target.contains(majorMc)) {
                        out.set(version.text());
                        break;
                    }
                }
            }
        });

        return out.get();
    }

    /**
     * Get the newest available loader version.
     *
     * @return loader version.
     */
    String getNewestLoaderVersion() throws MalformedURLException {
        List<LazyMap> loaderVersions = (List<LazyMap>) new JsonSlurper().parse(new URL("https://meta.fabricmc.net/v2/versions/loader/"));
        return (String) DefaultGroovyMethods.first(loaderVersions).get("version");
    }

    /**
     * Get correct Yarn version for project's MC version.
     *
     * @param newest whether to return the newest available Yarn version, or the oldest. Oldest may be preferred to avoid
     * invalidating genSources' output.
     * @return the correct Yarn version.
     */
    String getChosenYarnVersion(String ver, boolean newest) throws MalformedURLException {
        List<LazyMap> yarnVersions = (List<LazyMap>) new JsonSlurper().parse(
                new URL("https://meta.fabricmc.net/v2/versions/yarn/"+ ver));
        return (String) (newest ? DefaultGroovyMethods.first(yarnVersions).get("version") : DefaultGroovyMethods.last(yarnVersions).get("version"));
    }
}
