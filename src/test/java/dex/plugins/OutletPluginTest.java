package dex.plugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;

public class OutletPluginTest {

    @Test
    public void mcVersionGenTest() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("io.github.dexman545.outlet");
        project.getExtensions().configure(OutletExtension.class, (a) -> {
            a.mcVersionRange = "1.20.2";//">=1.18 <1.19-" >=1.20.2-alpha.23.31.a
            a.propertiesData.put("meh", 1);
            a.setAllowSnapshotsForProject(true);
            a.propertiesFile = new File("gradle.properties");
        });
        OutletExtension murry = project.getExtensions().getByType(OutletExtension.class);
        System.out.println(murry.latestMc());
        System.out.println(murry.mcVersions());
        System.out.println(murry.javaVersion());
        System.out.println(murry.curseforgeMcVersions());
        System.out.println(murry.javaLanguageCompatibility());
        System.out.println(murry.fapiVersion());
        assert murry.mcVersions() != null;
        assert murry.fapiVersion() != null;
        assert murry.curseforgeMcVersions() != null;
        assert murry.yarnVersion() != null;
        assert murry.loaderVersion() != null;
        assert murry.latestMc() != null;
        System.out.println("cached");
        System.out.println(murry.loaderVersion());
        murry.cacheTime = null;
        System.out.println("not cached");
        System.out.println(murry.loaderVersion());
        
        System.out.println(murry.latestModrinthModVersion("modmenu", Collections.singleton("1.18.2")));
        System.out.println(murry.latestModrinthModVersion("fabric-api", Collections.singleton("1.20.2")));
        System.out.println(murry.fapiVersion("1.20.1"));
        System.out.println(murry.fapiVersion("23w32a"));
        System.out.println(murry.neoforgeVersion("1.20.1"));
        System.out.println(murry.neoforgeVersion("1.20.4"));

    }
}
