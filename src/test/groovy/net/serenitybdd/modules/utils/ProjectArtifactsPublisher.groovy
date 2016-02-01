package net.serenitybdd.modules.utils

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

import java.nio.file.Path

/**
 * User: YamStranger
 * Date: 1/31/16
 * Time: 8:40 PM
 */
class ProjectArtifactsPublisher {
    def Path project

    def boolean publishArtifacts() {
        def publish = "publishToMavenLocal"
        GradleRunner.create().forwardOutput()
                .withProjectDir(project.toFile())
                .withArguments('clean', "-xtest", publish)
                .build().taskPaths(TaskOutcome.FAILED).size() == 0
    }
}