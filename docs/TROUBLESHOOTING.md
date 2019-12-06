# Troubleshooting

This page is meant to explain how to solve common issues when installing or using KAMI. If you experience an issue and it's not listed here, please [open a new issue](https://github.com/zeroeightysix/KAMI/issues/new) and a contributor will help you further.

## Setup
###### Could not find tools.jar
If you encounter this error when building, you most likely don't have the Java Development Kit (JDK) installed.
Head over to [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) to download the oracle JDK. Install it and try again.

###### Minecraft not loading at all
What `.jar` are you using in your `mods` folder? Make sure to use the one that ends with `-release` (`VERSION-release.jar`)

###### Just doesn't work when using runClient
Don't use that, try building and running forge normally

###### Crashes before game starts with spongepowered error
Make sure your workspace is clean and run
./gradlew setupDecompWorkspace
./gradlew build mkdir rmOld copy

## Crashes in-game

**Please make sure you're on the latest version of forge before proceeding!**

## When using Intellij / Eclipse
Crashes in game but it worked fine / errors link to files you haven't changed

Delete your .gradle or gradle cache or whatever it's called on windows


