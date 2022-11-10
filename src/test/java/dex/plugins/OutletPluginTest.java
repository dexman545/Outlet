package dex.plugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;

public class OutletPluginTest {

    @Test
    public void mcVersionGenTest() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("io.github.dexman545.outlet");
        project.getExtensions().configure(OutletExtension.class, (a) -> {
            a.mcVersionRange = ">=1.18.2-alpha.22.6.a";//">=1.18 <1.19-"
            a.propertiesData.put("meh", 1);
            a.allowSnapshotsForProject = true;
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

    }
}
