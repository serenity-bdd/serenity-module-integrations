package net.serenitybdd.modules

import net.serenitybdd.modules.utils.BuildScriptHelper
import net.serenitybdd.modules.utils.ProjectBuildHelper
import net.serenitybdd.modules.utils.ProjectDependencyHelper
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

/**
 * User: YamStranger
 * Date: 1/31/16
 * Time: 2:07 AM
 */
class WhenBuildingSerenityJIRA extends Specification {

    @Rule
    final TemporaryFolder temporary = new TemporaryFolder()

    def "clean test integrationTests install should execute successfully for serenity-jira"() {
        given:
        def project = new ProjectBuildHelper(project: "serenity-jira").prepareProject(temporary.getRoot())
        def version = ProjectDependencyHelper.publish("serenity-core", temporary.getRoot())
        new BuildScriptHelper(project: project).updateVersionOfSerenityCore(version)
        when:
        def result = GradleRunner.create().forwardOutput()
                .withProjectDir(project.toFile())
                .withArguments('clean', 'test','integrationTests','install')
                .build()
        then:
        result.tasks.each { it.outcome == SUCCESS }
    }
}