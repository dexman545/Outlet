package dex.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class OutletPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create("outlet", OutletExtension.class);

        OutletExtension ext1 = project.getExtensions().getByType(OutletExtension.class);
    }
}
