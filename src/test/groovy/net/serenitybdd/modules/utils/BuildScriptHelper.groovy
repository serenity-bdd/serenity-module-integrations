package net.serenitybdd.modules.utils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * User: YamStranger
 * Date: 1/30/16
 * Time: 6:39 AM
 */
class BuildScriptHelper {
    def Path project

    def updateVersionOfResultArtifact(def String version) {
        if (!project) {
            throw new IllegalArgumentException("Project should be specified")
        }
        def Path buildFile = project.resolve("build.gradle")
        rewriteLines(buildFile, ["pom.withXml":
                                         "println \"Publishing updated to \$project.name:\$project.group:$version\"\n"
                                                 + "groupId \"\$project.group\"\n"
                                                 + "artifactId \"\$project.name\"\n"
                                                 + "version \"$version\"\n"
        ], true)
        this
    }

    def updateVersionOfSerenityCore(def String version) {
        if (!project) {
            throw new IllegalArgumentException("Project should be specified")
        }
        def Path buildFile = project.resolve("build.gradle")
        rewriteLines(buildFile, ["\${serenityCoreVersion}"              : "$version",
                                 "\${serenityGradlePluginVersion}": "$version"])
        this
    }


    def private rewriteLines(def buildFile, def Map<String, String> updates, def keepSource = false) {
        def Path resultFile = project.resolve("updated.build.gradle")

        if (Files.exists(resultFile)) {
            Files.delete(resultFile)
        }

        resultFile.withWriter { w ->
            buildFile.eachLine { line ->
                updates.entrySet().each { entry ->
                    def source = entry.key
                    def newValue = entry.value
                    if (keepSource) {
                        newValue = newValue.concat(source)
                    }
                    if (line.contains(source)) {
                        line = line.replace(source, newValue)
                    }
                }
                w.println(line)
            }
        }
        Files.move(resultFile, buildFile, StandardCopyOption.REPLACE_EXISTING)
    }
}