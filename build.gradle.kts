import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    main {
        java.srcDirs("src")
        java.destinationDirectory.set(buildDir.resolve("classes"))
    }

    test {
        java.srcDirs("test")
        java.destinationDirectory.set(buildDir.resolve("tests"))
    }
}

configurations {
    create("client")
    all {
        resolutionStrategy.cacheDynamicVersionsFor(60, TimeUnit.SECONDS)
    }
}

fun get(url: String): String {
    return URL(url).readText().trim()
}

fun formatExamplefuncsplayer(content: String): String {
    return content
        .trim()
        .replace("System.out.", "// System.out.")
        .replace("e.printStackTrace()", "// e.printStackTrace()") + "\n"
}

val replayPath = "replays/${project.property("teamA")}-vs-${project.property("teamB")}-on-%MAP%.bc22"

val clientType = with(System.getProperty("os.name").toLowerCase()) {
    when {
        startsWith("windows") -> "win"
        startsWith("mac") -> "mac"
        else -> "linux"
    }
}
val clientName = "battlecode22-client-$clientType"

val battlecodeVersion = file("version.txt").readText().trim()
val battlecodeVersionUrl = "https://play.battlecode.org/versions/2022/version.txt"

val examplefuncsplayerUrl =
    "https://raw.githubusercontent.com/battlecode/battlecode22-scaffold/main/src/examplefuncsplayer/RobotPlayer.java"
val examplefuncsplayerPath = "src/examplefuncsplayer/RobotPlayer.java"

val classLocation = sourceSets["main"].output.classesDirs.asPath

repositories {
    mavenCentral()
    maven("https://us-east1-maven.pkg.dev/battlecode18/battlecode22-engine")
}

dependencies {
    implementation("org.battlecode:battlecode22:$battlecodeVersion")
    implementation("org.battlecode:battlecode22:$battlecodeVersion:javadoc")
    add("client", "org.battlecode:$clientName:$battlecodeVersion")
}

task("checkForUpdates") {
    group = "battlecode"
    description = "Checks for Battlecode updates."

    doLast {
        val latestBattlecodeVersion = get(battlecodeVersionUrl)
        if (battlecodeVersion != latestBattlecodeVersion) {
            print("\n\n\nBATTLECODE UPDATE AVAILABLE ($battlecodeVersion -> $latestBattlecodeVersion)\n\n\n")
        }

        if (file(examplefuncsplayerPath).readText() != formatExamplefuncsplayer(get(examplefuncsplayerUrl))) {
            print("\n\n\nEXAMPLEFUNCSPLAYER UPDATE AVAILABLE\n\n\n")
        }
    }
}

task("update") {
    group = "battlecode"
    description = "Updates to the latest Battlecode version."

    doLast {
        val latestBattlecodeVersion = get(battlecodeVersionUrl)
        if (battlecodeVersion == latestBattlecodeVersion) {
            println("Already using the latest Battlecode version ($battlecodeVersion)")
        } else {
            file("version.txt").writeText(latestBattlecodeVersion + "\n")
            println("Updated Battlecode from $battlecodeVersion to $latestBattlecodeVersion, please reload the Gradle project")
        }

        val latestExamplefuncsplayer = formatExamplefuncsplayer(get(examplefuncsplayerUrl))
        if (file(examplefuncsplayerPath).readText() == latestExamplefuncsplayer) {
            println("Already using the latest examplefuncsplayer")
        } else {
            file(examplefuncsplayerPath).writeText(latestExamplefuncsplayer)
            println("Updated examplefuncsplayer to the latest version")
        }
    }
}

task<Copy>("unpackClient") {
    group = "battlecode"
    description = "Unpacks the client."

    dependsOn(configurations["client"], "checkForUpdates")

    from(configurations["client"].map { if (it.isDirectory) it else zipTree(it) })
    into("client/")
}

task<JavaExec>("run") {
    group = "battlecode"
    description = "Runs a match without starting the client."

    dependsOn("build")

    mainClass.set("battlecode.server.Main")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf("-c=-")
    jvmArgs = listOf(
        "-Dbc.server.mode=headless",
        "-Dbc.server.map-path=maps",
        "-Dbc.engine.robot-player-to-system-out=${project.property("outputVerbose")}",
        "-Dbc.server.debug=true",
        "-Dbc.engine.debug-methods=true",
        "-Dbc.engine.enable-profiler=${project.property("profilerEnabled")}",
        "-Dbc.game.team-a=${project.property("teamA")}",
        "-Dbc.game.team-b=${project.property("teamB")}",
        "-Dbc.game.team-a.url=$classLocation",
        "-Dbc.game.team-b.url=$classLocation",
        "-Dbc.game.maps=${project.property("maps")}",
        "-Dbc.server.save-file=${replayPath.replace("%MAP%", project.property("maps").toString())}"
    )
}

task<JavaExec>("runFromClient") {
    group = "battlecode"
    description = "Runs a match in the client."

    dependsOn("build")

    mainClass.set("battlecode.server.Main")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf("-c=-")
    jvmArgs = listOf(
        "-Dbc.server.wait-for-server=true",
        "-Dbc.server.mode=headless",
        "-Dbc.server.map-path=maps",
        "-Dbc.engine.robot-player-to-system-out=false",
        "-Dbc.server.debug=false",
        "-Dbc.engine.debug-methods=true",
        "-Dbc.engine.enable-profiler=${project.property("profilerEnabled")}",
        "-Dbc.game.team-a=${project.property("teamA")}",
        "-Dbc.game.team-b=${project.property("teamB")}",
        "-Dbc.game.team-a.url=$classLocation",
        "-Dbc.game.team-b.url=$classLocation",
        "-Dbc.game.maps=${project.property("maps")}",
        "-Dbc.server.save-file=${replayPath.replace("%MAP%", project.property("maps").toString())}"
    )
}

task<JavaExec>("runDebug") {
    group = "battlecode"
    description = "Runs a match in debug mode."

    dependsOn("build")

    mainClass.set("battlecode.server.Main")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf("-c=-")
    jvmArgs = listOf(
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005",
        "-Dbc.server.mode=headless",
        "-Dbc.server.map-path=maps",
        "-Dbc.engine.robot-player-to-system-out=true",
        "-Dbc.server.debug=false",
        "-Dbc.engine.debug-methods=true",
        "-Dbc.engine.enable-profiler=${project.property("profilerEnabled")}",
        "-Dbc.game.team-a=${project.property("teamA")}",
        "-Dbc.game.team-b=${project.property("teamB")}",
        "-Dbc.game.team-a.url=$classLocation",
        "-Dbc.game.team-b.url=$classLocation",
        "-Dbc.game.maps=${project.property("maps")}",
        "-Dbc.server.save-file=${replayPath.replace("%MAP%", project.property("maps").toString())}"
    )
}

task("listPlayers") {
    group = "battlecode"
    description = "Lists all available players."

    doLast {
        val players = sourceSets["main"].allSource
            .filter { it.name == "RobotPlayer.java" }
            .map {
                val base = file("src").toURI()
                val full = it.toURI()
                val path = base.relativize(full).toString()
                path.substringBeforeLast('/').replace('/', '.')
            }
            .sortedBy { it.toLowerCase() }

        println("Players (${players.size}):")
        for (player in players) {
            println(player)
        }
    }
}

task("listMaps") {
    group = "battlecode"
    description = "Lists all available maps."

    doLast {
        val officialMapFiles =
            zipTree(sourceSets["main"].compileClasspath.first { it.toString().contains("battlecode22-2022") })
        val customMapFiles = fileTree(file("maps"))

        val maps = (officialMapFiles + customMapFiles)
            .filter { it.name.endsWith(".map22") }
            .map { it.name.substringBeforeLast(".map22") }
            .distinct()
            .sortedBy { it.toLowerCase() }

        println("Maps (${maps.size}):")
        for (map in maps) {
            println(map)
        }
    }
}

task<Zip>("createSubmission") {
    group = "battlecode"
    description = "Creates a submission zip."

    dependsOn("build")

    from(file("src").absolutePath)
    include("camel_case/**/*")
    archiveBaseName.set(DateTimeFormatter.ofPattern("yyyy-MM-dd_kk-mm-ss").format(LocalDateTime.now()))
    destinationDirectory.set(project.buildDir.resolve("submissions"))
}

tasks.named("build") {
    group = "battlecode"

    dependsOn("unpackClient")
}
