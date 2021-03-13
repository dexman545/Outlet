# Outlet
_A Gradle plugin for lazy Minecraft mod developement._

Provides the following features:
- Getting the latest Minecraft version available, 
  optionally within a given range
- Getting the latest or earliest Yarn version for a Minecraft version
- Getting the latest available version of Fabric loader
- Getting the latest available version of Fabric API for a Minecraft version
- Generating the list of acceptable Minecraft versions based on a given range,
  useful for instance with publishing on Modrinth via [Minotaur](https://fabricmc.net/wiki/tutorial:minotaur)
- Generating the list of acceptable Minecraft versions based on a given range 
  for use on Curseforge via [CurseGradle](https://fabricmc.net/wiki/tutorial:cursegradle)

## Usage
There are three parts using Outlet: applying the plugin, 
feeding it options, then calling methods. The latter two occur anywhere 
after application.

### Application
At the top of `build.gradle`, add:
```groovy
buildscript {
    repositories {
        maven {url 'https://jitpack.io'}
    }
    dependencies {
        classpath 'com.github.dexman545:Outlet:master-SNAPSHOT'
    }
}
```
Beneath the `plugins` block, add:
```groovy
apply plugin: 'dex.plugins.outlet'
```

### Telling it what you want
**_Dummy values in use!_**

Add any of the following (`mcVersiongRange` is required):
```groovy
// The Minecraft version range from the fabric.mod.json.
// Use '*' to match any MC version
// Default: null - set it!
outlet.mcversionRange = '*'
// Whether outlet.mcVersion() should return snapshots
// Default: true
outlet.allowSnapshotsForProject = true
// Whether outlet.yarnVersion() should return the latest yarn version or 
// the earliest yarn version for a given
// Minecraft version
// Default: true
outlet.useLatestYarn = true
// Whether outlet.mcVersion() respects the provided range
// Default: true
outlet.latestMcRespectsRange = true
```
_Note: these can also be set using the `outlet` block!_

### Getting stuff out of it
You can set the outputs of these methods to settings or use them directly.

**_Note that the return types shouldn't be in your script, they are added 
for your viewing pleasure only._**
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
```

#### Example
```groovy
// Range is in gradle.properties, could also read in from fabric.mod.json
outlet.mcVersionRange = project.range

// Use fabric example mod's variables to keep template mostly unchanged
project.minecraft_version = outlet.latestMc()
project.loader_version = outlet.loaderVersion()
project.yarn_mappings =  outlet.yarnVersion()
project.fabric_version = outlet.fapiVersion()
```