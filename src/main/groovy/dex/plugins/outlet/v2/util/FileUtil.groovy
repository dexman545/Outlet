package dex.plugins.outlet.v2.util

import org.gradle.api.Project

import java.nio.file.Path

class FileUtil {
    public static FileUtil INSTANCE

    private final Project project

    FileUtil(Project project) {
        this.project = project
        INSTANCE = this
    }

    Path globalCache() {
        project.getGradle().getGradleUserHomeDir().toPath().resolve('caches').resolve('outlet')
    }

    Path mc2FabricCachePath() {
        globalCache().resolve('mc2fabric.json')
    }

    static Artifact mc2FabricCacheArtifact() {
        new Artifact(name: 'mc2fabric.json',
                url: 'https://raw.githubusercontent.com/dexman545/outlet-database/master/mc2fabric.json',
                containingPath: INSTANCE.mc2FabricCachePath())
    }
}