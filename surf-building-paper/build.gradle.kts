plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.building.paper.PaperMain")

    authors.add("red")

    generateLibraryLoader(false)
    foliaSupported(false)
}