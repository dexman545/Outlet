package dex.plugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;

public class OutletPluginTest {

    @Test
    public void mcVersionGenTest() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("dex.plugins.outlet");
        project.getExtensions().configure(OutletExtension.class, (a) -> {
            a.mcVersionRange = ">=1.18.2-alpha.22.6.a";//">=1.18 <1.19-"
            a.propertiesData.put("meh", 1);
            a.allowSnapshotsForProject = true;
            a.propertiesFile = new File("gradle.properties");
        });
        System.out.println(project.getExtensions().getByType(OutletExtension.class).latestMc());
        System.out.println(project.getExtensions().getByType(OutletExtension.class).mcVersions());
        System.out.println(project.getExtensions().getByType(OutletExtension.class).javaVersion());
        System.out.println(project.getExtensions().getByType(OutletExtension.class).curseforgeMcVersions());
        System.out.println(project.getExtensions().getByType(OutletExtension.class).javaLanguageCompatibility());
        System.out.println(project.getExtensions().getByType(OutletExtension.class).fapiVersion());
        //project.getExtensions().getByType(OutletExtension.class).meh();
        assert project.getExtensions().getByType(OutletExtension.class).mcVersions() != null;
        assert project.getExtensions().getByType(OutletExtension.class).fapiVersion() != null;
        assert project.getExtensions().getByType(OutletExtension.class).curseforgeMcVersions() != null;
        assert project.getExtensions().getByType(OutletExtension.class).yarnVersion() != null;
        assert project.getExtensions().getByType(OutletExtension.class).loaderVersion() != null;
        assert project.getExtensions().getByType(OutletExtension.class).latestMc() != null;
        System.out.println("cached");
        System.out.println(project.getExtensions().getByType(OutletExtension.class).loaderVersion());
        project.getExtensions().getByType(OutletExtension.class).cacheTime = null;
        System.out.println("not cached");
        System.out.println(project.getExtensions().getByType(OutletExtension.class).loaderVersion());

    }
}
