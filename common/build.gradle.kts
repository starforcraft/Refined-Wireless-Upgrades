plugins {
    id("com.refinedmods.refinedarchitect.common")
}

repositories {
    maven {
        name = "Refined Storage"
        url = uri("https://maven.creeperhost.net")
        content {
            includeGroup("com.refinedmods.refinedstorage")
        }
    }
    maven {
        name = "JEI"
        url = uri("https://maven.blamejared.com/")
    }
}

val modVersion: String by project

refinedarchitect {
    version = modVersion
    common()
    testing()
    publishing {
        maven = true
    }
}

neoForge {
    accessTransformers.from(rootProject.file("neoforge/src/main/resources/META-INF/accesstransformer.cfg"))
}

base {
    archivesName.set("refinedwirelessupgrades-common")
}

val minecraftVersion: String by project
val refinedstorageVersion: String by project
val refinedstorageQuartzArsenalVersion: String by project
val jeiVersion: String by project

dependencies {
    api(libs.apiguardian)
    api("com.refinedmods.refinedstorage:refinedstorage-common:${refinedstorageVersion}")
    api("com.refinedmods.refinedstorage:refinedstorage-quartz-arsenal-common:${refinedstorageQuartzArsenalVersion}")

    api("mezz.jei:jei-${minecraftVersion}-common-api:${jeiVersion}")
    api("mezz.jei:jei-${minecraftVersion}-common:${jeiVersion}")
}
