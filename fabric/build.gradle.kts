plugins {
    id("com.refinedmods.refinedarchitect.fabric")
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
        name = "ModMenu"
        url = uri("https://maven.terraformersmc.com/")
    }
    maven {
        name = "Cloth Config"
        url = uri("https://maven.shedaniel.me/")
    }
    maven {
        name = "JEI"
        url = uri("https://maven.blamejared.com/")
    }
}

val modVersion: String by project

refinedarchitect {
    modId = "rsinsertexportupgrade"
    version = modVersion
    fabric()
}

base {
    archivesName.set("refinedwirelessupgrades-fabric")
}

val minecraftVersion: String by project
val refinedstorageVersion: String by project
val refinedstorageQuartzArsenalVersion: String by project
val jeiVersion: String by project

val commonJava by configurations.existing
val commonResources by configurations.existing

dependencies {
    api(libs.apiguardian)
    compileOnly(project(":common"))
    commonJava(project(path = ":common", configuration = "commonJava"))
    commonResources(project(path = ":common", configuration = "commonResources"))
    api("com.refinedmods.refinedstorage:refinedstorage-fabric:${refinedstorageVersion}")
    api("com.refinedmods.refinedstorage:refinedstorage-quartz-arsenal-fabric:${refinedstorageQuartzArsenalVersion}")

    runtimeOnly("mezz.jei:jei-${minecraftVersion}-fabric:${jeiVersion}")
    compileOnlyApi("mezz.jei:jei-${minecraftVersion}-common-api:${jeiVersion}")
    compileOnlyApi("mezz.jei:jei-${minecraftVersion}-common:${jeiVersion}")
    compileOnlyApi("mezz.jei:jei-${minecraftVersion}-fabric-api:${jeiVersion}")
}