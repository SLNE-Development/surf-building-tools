plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

group = "dev.slne.surf.building"
version = findProperty("version") as String

surfPaperPluginApi {
    mainClass("dev.slne.surf.building.PaperMain")
    generateLibraryLoader(false)

    authors.add("red")
}