package dex.plugins.outlet.v2.util

import dex.plugins.OutletExtension
import org.gradle.api.Project

import java.nio.file.Path

class FileUtil {
    private static FileUtil INSTANCE

    private final Project project
    private final OutletExtension extension

    private FileUtil(Project project, OutletExtension extension) {
        this.extension = extension
        this.project = project
        INSTANCE = this
    }

    Path globalCache() {
        project.getGradle().getGradleUserHomeDir().toPath().resolve('caches').resolve('outlet').resolve('cache')
    }

    static Artifact mc2FabricCacheArtifact() {
        new Artifact(name: 'mc2fabric.json',
                url: 'https://raw.githubusercontent.com/dexman545/outlet-database/master/mc2fabric.json',
                containingPath: INSTANCE.globalCache(),
                updateFreq: INSTANCE.time())
    }

    static Artifact fapiArtifact() {
        new Artifact(name: 'fapi.xml',
                url: 'https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml',
                containingPath: INSTANCE.globalCache(),
                updateFreq: INSTANCE.time())
    }

    static Artifact yarnArtifact(String ver) {
        new Artifact(name: "yarn_mc${ver}.json",
                url: "https://meta.fabricmc.net/v2/versions/yarn/${ver}",
                containingPath: INSTANCE.globalCache(),
                updateFreq: INSTANCE.time())
    }

    static Artifact loaderArtifact() {
        new Artifact(name: 'loader.json',
                url: 'https://meta.fabricmc.net/v2/versions/loader/',
                containingPath: INSTANCE.globalCache(),
                updateFreq: INSTANCE.time())
    }

    static def init(Project project, OutletExtension extension) {
        if (INSTANCE == null || (INSTANCE.project != project || INSTANCE.extension != extension)) {
            new FileUtil(project, extension)
        }
    }

    private def time() {
        extension.cacheTime
    }
}