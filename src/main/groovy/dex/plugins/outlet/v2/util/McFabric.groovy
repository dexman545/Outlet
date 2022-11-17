package dex.plugins.outlet.v2.util

import groovy.transform.ToString

@ToString
class McFabric {
    String id
    String normalized
    Integer javaVersion = 8
    ReleaseType type
}
