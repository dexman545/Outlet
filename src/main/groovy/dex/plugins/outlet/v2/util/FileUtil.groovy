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
        buildArtifact('mc2fabric.json', 'https://raw.githubusercontent.com/dexman545/outlet-database/master/mc2fabric.json')
    }

    static Artifact fapiArtifact() {
        buildArtifact('fapi.xml', 'https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml')
    }

    static Artifact neoforgeArtifact() {
        buildArtifact('neoforge.xml', 'https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml')
    }

    static Artifact yarnArtifact(String ver) {
        buildArtifact("yarn_mc${ver}.json", "https://meta.fabricmc.net/v2/versions/yarn/${ver}")
    }

    static Artifact loaderArtifact() {
        buildArtifact('loader.json', 'https://meta.fabricmc.net/v2/versions/loader/')
    }

    static Artifact parchmentVersionArtifact() {
        buildArtifact('parchmentVersion.json', 'https://versioning.parchmentmc.org/versions')
    }

    static Artifact parchmentMcArtifact(String ver) {
        buildArtifact("parchmentVersion${ver}.xml", "https://maven.parchmentmc.org/org/parchmentmc/data/parchment-${ver}/maven-metadata.xml")
    }

    static Artifact buildArtifact(String name, String url, String containingPath = INSTANCE.globalCache()) {
        return new Artifact(name: name, url: url, containingPath: containingPath, updateFreq: INSTANCE.time())
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