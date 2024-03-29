package dex.plugins.outlet.v2.util

import groovy.time.TimeCategory
import groovy.time.TimeDuration

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Artifact {
    String url // Download URL
    String name // File name
    String containingPath
    TimeDuration updateFreq = null

    File fetchArtifact() {
        def path = getPath()
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
        lazyDownload(true, forced)
    }

    void lazyDownload(boolean observesTime, boolean forced) {
        if (!observesTime || !isUpToDate()) {
            def p = getPath()
            p.parent.toFile().mkdirs()
            println ((forced ? 'Missing artifact: ' : 'Updating artifact: ') + name + ' At: ' + containingPath)
            println 'Attempting to download...'

            try {
                p.withOutputStream { it << new URL(url).newInputStream() }
            } catch (Exception e) {
                println 'Failed to get artifact'
            }
        }
    }

    Path getContainingPath() {
        return Paths.get(containingPath)
    }

    Path getPath() {
        getContainingPath().resolve(name ?: nameFromUrl(url))
    }

    boolean isUpToDate() {
        if (updateFreq != null) {
            def path = getPath()
            if (Files.exists(path)) {
                def t = Date.from(Files.getLastModifiedTime(path).toInstant())

                return TimeCategory.minus(new Date(), t) < updateFreq
            }
        }

        return false
    }

    static String nameFromUrl(String url) {
        if (url == null) return "" + url.hashCode()
        return url.split("/").last()
    }
}
