plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.building.paper.PaperMain")

    authors.add("red")

    generateLibraryLoader(false)
    foliaSupported(true)
}

dependencies {
    api("dev.slne.surf:surf-database-r2dbc:1.0.0-SNAPSHOT")
}