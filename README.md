# Intellij IDEA Concord plugin

## Description

A Concord flow plugin for IntelliJ IDEA

## Building

Dependencies:
- Java 11

```shell
git clone ...
cd concord-intellij
./gradlew buildPlugin
```

The resulting concord-intellij-version-SNAPSHOT.zip is located in build/distributions and can then be [installed](https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk) either manually
or uploaded to a [custom plugin repository](https://plugins.jetbrains.com/docs/intellij/update-plugins-format.html).

## Execute an IntelliJ IDEA instance with concord plugin

```shell
git clone ...
cd concord-intellij
./gradlew runIde
```