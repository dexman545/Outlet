package dex.plugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

public class OutletPluginTest {

    @Test
    public void mcVersionGenTest() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("dex.plugins.outlet");
        project.getExtensions().configure(OutletExtension.class, (a) -> {
            a.mcVersionRange = ">=1.16-alpha.20.22.a";
        });

        System.out.println();
        assert project.getExtensions().getByType(OutletExtension.class).mcVersions() != null;
        assert project.getExtensions().getByType(OutletExtension.class).fapiVersion() != null;
        assert project.getExtensions().getByType(OutletExtension.class).curseforgeMcVersions() != null;
        assert project.getExtensions().getByType(OutletExtension.class).fapiVersion() != null;
        assert project.getExtensions().getByType(OutletExtension.class).yarnVersion() != null;
        assert project.getExtensions().getByType(OutletExtension.class).loaderVersion() != null;
        assert project.getExtensions().getByType(OutletExtension.class).latestMc() != null;

    }
}
