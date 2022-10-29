package dex.plugins.outlet.v2.util

import java.nio.file.Path
import java.nio.file.Paths

class Artifact {
    String url // Download URL
    String name // File name
    String containingPath

    File fetchArtifact() {
        def path = getContainingPath().resolve(name)
        ensureArtifactPresence(path)

        return path.toFile()
    }

    void ensureArtifactPresence(Path p) {
        def f = p.toFile()
        if (!f.exists()) {
            download(false)
        }
    }

    void download(boolean forced) {
        def p = getContainingPath().resolve(name)
        p.parent.toFile().mkdirs()
        println (forced ? 'Missing artifact: ' : 'Updating artifact: ') + name + ' At: ' + containingPath
        println 'Attempting to download...'

        try {
            p.withOutputStream { it << new URL(url).newInputStream() }
        } catch (Exception e) {
            println 'Failed to get artifact'
        }
    }

    Path getContainingPath() {
        return Paths.get(containingPath)
    }

    static String nameFromUrl(String url) {
        if (url == null) return "" + url.hashCode()
        return url.split("/").last()
    }
}
