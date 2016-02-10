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
    def File project

    def updateVersionOfResultArtifact(def String version) {
        if (!project) {
            throw new IllegalArgumentException("Project should be specified")
        }
        def File buildFile = new File(project, "build.gradle")
        rewriteLines(buildFile, ["pom.withXml":
                                     "println \"Publishing updated to \$project.name:\$project.group:$version\"\n"
                                         + "groupId \"\$project.group\"\n"
                                         + "artifactId \"\$project.name\"\n"
                                         + "version \"$version\"\n"
        ], true)
        rewriteLines(buildFile, ["project.version.toString()":
                                     "\"$version\""], false)
        rewriteLines(buildFile, ["<version>\${version}</version>":
                                     "<version>${version}</version>"], false)
        this
    }

    def updateVersionOfSerenityCore(def String version) {
        if (!project) {
            throw new IllegalArgumentException("Project should be specified")
        }
        def File buildFile = new File(project, "build.gradle")
        rewriteLines(buildFile, ["\${serenityCoreVersion}"                : "$version",
                                 "\${project.serenityCoreVersion}"        : "$version",
                                 "\${serenityGradlePluginVersion}"        : "$version",
                                 "\${project.serenityGradlePluginVersion}": "$version"
        ])
        this
    }

    def updateVersionOfSerenityCucumber(def String version) {
        if (!project) {
            throw new IllegalArgumentException("Project should be specified")
        }
        def File buildFile = new File(project, "build.gradle")
        rewriteLines(buildFile, ["\${serenityCucumberVersion}": "$version"])
        this
    }

    def updateVersionOfSerenityJBehave(def String version) {
        if (!project) {
            throw new IllegalArgumentException("Project should be specified")
        }
        def File buildFile = new File(project, "build.gradle")
        rewriteLines(buildFile, ["\${serenityJBehaveVersion}": "$version"])
        this
    }


    def private rewriteLines(def File buildFile, def Map<String, String> updates, def keepSource = false) {
        def File resultFile = new File(project, "updated.build.gradle")

        if (resultFile.exists()) {
            resultFile.delete()
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
        Files.copy(resultFile.toPath(), buildFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}