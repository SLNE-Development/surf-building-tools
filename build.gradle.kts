import dev.slne.surf.api.gradle.util.registerRequired

plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

group = "dev.slne.surf.buildsystem"
version = findProperty("version") as String

surfPaperPluginApi {
    mainClass("dev.slne.surf.buildsystem.PaperMain")
    bootstrapper("dev.slne.surf.buildsystem.PaperBootstrap")
    generateLibraryLoader(false)

    authors.add("red")

    serverDependencies {
        registerRequired("surf-bitmap-provider-paper")
    }
}

dependencies {
    compileOnly("dev.slne.surf.bitmap:surf-bitmap-provider-common:+")
}
