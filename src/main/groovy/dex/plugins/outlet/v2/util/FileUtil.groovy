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
        project.getGradle().getGradleUserHomeDir().toPath().resolve('caches').resolve('outlet').resolve('cache')
    }

    static Artifact mc2FabricCacheArtifact() {
        new Artifact(name: 'mc2fabric.json',
                url: 'https://raw.githubusercontent.com/dexman545/outlet-database/master/mc2fabric.json',
                containingPath: INSTANCE.globalCache())
    }

    static Artifact fapiArtifact() {
        new Artifact(name: 'fapi.xml',
                url: 'https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml',
                containingPath: INSTANCE.globalCache())
    }

    static Artifact yarnArtifact(String ver) {
        new Artifact(name: "yarn_mc${ver}.json",
                url: "https://meta.fabricmc.net/v2/versions/yarn/${ver}",
                containingPath: INSTANCE.globalCache())
    }

    static Artifact loaderArtifact() {
        new Artifact(name: 'loader.json',
                url: 'https://meta.fabricmc.net/v2/versions/loader/',
                containingPath: INSTANCE.globalCache())
    }
}