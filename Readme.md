# Outlet
_A Gradle plugin for lazy Minecraft mod developement._

Provides the following features:
- Getting the latest Minecraft version available, 
  optionally within a given range
- Getting the latest or earliest Yarn version for a Minecraft version
- Getting the latest available version of Fabric loader
- Getting the latest available version of Fabric API for a Minecraft version
- Getting the required Java version for a Minecraft version
- Getting the Java compatibility level for a range of Miencraft versions
- Generating the list of acceptable Minecraft versions based on a given range,
  useful for instance with publishing on Modrinth via [Minotaur](https://fabricmc.net/wiki/tutorial:minotaur)
- Generating the list of acceptable Minecraft versions based on a given range 
  for use on Curseforge via [CurseGradle](https://fabricmc.net/wiki/tutorial:cursegradle)
- Optional nondestructive automatic updating of gradle.properties versions with the new values Outlet has produced

## Usage
There are three parts to using Outlet: applying the plugin, 
feeding it options, then calling the methods. The latter two occur anywhere 
after application.

### Application
[![](https://jitpack.io/v/dexman545/Outlet.svg)](https://jitpack.io/#dexman545/Outlet)

At the top of `build.gradle`, add (merging as needed):
```groovy
buildscript {
    repositories {
        maven {url 'https://jitpack.io'}
    }
    dependencies {
        classpath 'com.github.dexman545:Outlet:<version>' // For bleeding-edge, use master-SNAPSHOT
    }
}
```
Beneath the `plugins` block, add (merging as needed):
```groovy
apply plugin: 'dex.plugins.outlet'
```

### Telling it what you want
**_Dummy values in use!_**

Add any of the following (`mcVersionRange` is required):
```groovy
// The Minecraft version range from the fabric.mod.json.
// Use '*' to match any MC version
// Default: null - set it!
outlet.mcVersionRange = '*'
// Whether outlet.mcVersions() should return snapshots
// Default: true
outlet.allowSnapshotsForProject = true
// Whether outlet.yarnVersion() should return the latest yarn version or 
// the earliest yarn version for a given Minecraft version
// Default: true
outlet.useLatestYarn = true
// Whether outlet.latestMc() respects the provided range
// Default: true
outlet.latestMcRespectsRange = true

// Whether the properties file versions should be updated.
// This also doubles as an environment check, eg. update them in dev 
// but not on a build server
// Default: false
outlet.maintainPropertiesFile = true

// The map of entries to update in the properties file and their new version
// Any key/value pair in the properties file can be updated in this way, not just those Outlet manages!
// Default: empty
outlet.propertiesData = ['fabric_version': outlet.fapiVersion()]
```
_Note: these can also be set using the `outlet` block!_

### Getting stuff out of it
You can set the outputs of these methods to settings or use them directly.

```groovy
// Get the set of Minecraft version strings
// Can be used for automated Modrinth upload
outlet.mcVersions() // Returns Set<String>

// Get the set of Minecraft version strings for automated curseforge upload
outlet.curseforgeMcVersions() // Returns Set<String>

// Get the latest Minecraft version
outlet.latestMc() // Returns String

// Get the latest Fabric Loader version
outlet.loaderVersion() // Returns String

// Get the Yarn version for the latest MC version (output of latestMc())
outlet.yarnVersion() // Returns String

// Get the Yarn version for the given MC version
// mcVer - the Minecraft version, such as '21w10a'
outlet.yarnVersion(mcVer) // Returns String

// Get the latest Fabric API version for the latest MC version (output of latestMc())
outlet.fapiVersion() // Returns String

// Get the latest Fabric API version for the given MC version
// mcVer - the Minecraft version, such as '21w10a'
outlet.fapiVersion(mcVer) // Returns String

// Get the Java version for the latest MC version
// For use in setting the compiler level
// Defaults to 8 if it cannot be found
outlet.outlet.javaVersion() // Returns int

// Get the Java version for the given MC version
// For use in setting the compiler level
// Defaults to 8 if it cannot be found
// mcVer - the Minecraft version, such as '21w10a'
outlet.javaVersion(mcVer) // Returns int

// Get the Java language compatibility level that all versions in mcVersions() can support
// For use in setting compatibility level
// Defaults to 8 if it cannot be found
// Examples:
//      Range: 1.16 - 1.18; Returns: 8
//      Range: 1.17 - 1.18; Returns: 16
//      Range: 1.18.x;      Returns: 17
outlet.javaLanguageCompatibility() // Returns int


```

#### Example
```groovy
// In this project, range is in gradle.properties, could also read it from fabric.mod.json
outlet.mcVersionRange = project.range

// Use fabric example mod's variables to keep template mostly unchanged
project.minecraft_version = outlet.latestMc()
project.loader_version = outlet.loaderVersion()
project.yarn_mappings =  outlet.yarnVersion()
project.fabric_version = outlet.fapiVersion()
```