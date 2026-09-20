plugins {
    id("com.refinedmods.refinedarchitect.neoforge")
}

repositories {
    maven {
        url = uri("https://modmaven.dev/")
        content {
            includeGroup("mekanism")
            includeGroup("dev.technici4n")
        }
    }
    maven { url = uri("https://maven.theillusivec4.top/") }
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
    maven {
        name = "ModMenu"
        url = uri("https://maven.terraformersmc.com/")
    }
    maven {
        name = "Cloth Config"
        url = uri("https://maven.shedaniel.me/")
    }
}

val modVersion: String by project

refinedarchitect {
    modId = "rsinsertexportupgrade"
    version = modVersion
    neoForge()
    dataGeneration(project(":common"))
}

base {
    archivesName.set("rsinsertexportupgrade-neoforge")
}

val minecraftVersion: String by project
val refinedstorageVersion: String by project
val refinedstorageQuartzArsenalVersion: String by project
val jeiVersion: String by project
val curiosVersion: String by project
val mekanismVersion: String by project
val grandpowerVersion: String by project

val commonJava by configurations.existing
val commonResources by configurations.existing

dependencies {
    compileOnly("mekanism:Mekanism:${minecraftVersion}-${mekanismVersion}:api")
    compileOnly("dev.technici4n:GrandPower:${grandpowerVersion}")
    compileOnly("top.theillusivec4.curios:curios-neoforge:${curiosVersion}:api")
    runtimeOnly("top.theillusivec4.curios:curios-neoforge:${curiosVersion}")
    compileOnly(project(":common"))
    commonJava(project(path = ":common", configuration = "commonJava"))
    commonResources(project(path = ":common", configuration = "commonResources"))
    api("com.refinedmods.refinedstorage:refinedstorage-neoforge:${refinedstorageVersion}")
    api("com.refinedmods.refinedstorage:refinedstorage-quartz-arsenal-neoforge:${refinedstorageQuartzArsenalVersion}")

    runtimeOnly("mezz.jei:jei-${minecraftVersion}-neoforge:${jeiVersion}")
    compileOnlyApi("mezz.jei:jei-${minecraftVersion}-common-api:${jeiVersion}")
    compileOnlyApi("mezz.jei:jei-${minecraftVersion}-neoforge-api:${jeiVersion}")
}