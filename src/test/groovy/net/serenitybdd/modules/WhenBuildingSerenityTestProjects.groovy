package net.serenitybdd.modules

import net.serenitybdd.modules.utils.BuildScriptHelper
import net.serenitybdd.modules.utils.ProjectArtifactsPublisher
import net.serenitybdd.modules.utils.ProjectBuildHelper
import net.serenitybdd.modules.utils.ProjectDependencyHelper
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

/**
 * User: YamStranger
 * Date: 1/31/16
 * Time: 2:07 AM
 */
class WhenBuildingSerenityTestProjects extends Specification {

    @Rule
    final TemporaryFolder temporary = new TemporaryFolder()

    def "serenityTestProject tests should work with last changes in serenity modules"() {
        given:
            def File location = temporary.getRoot()
            def coreVersion = ProjectDependencyHelper.publish("serenity-core", location)

            def jbehave = new ProjectBuildHelper(project: "serenity-jbehave").prepareProject(location)
            new BuildScriptHelper(project: jbehave).updateVersionOfSerenityCore(coreVersion)
            def jbehaveVersion = ProjectDependencyHelper.publish(jbehave)

            def cucumber = new ProjectBuildHelper(project: "serenity-cucumber").prepareProject(location)
            new BuildScriptHelper(project: cucumber).updateVersionOfSerenityCore(coreVersion)
            def cucumberVersion = ProjectDependencyHelper.publish(cucumber)

            def project = new ProjectBuildHelper(project: "serenity-test-projects").prepareProject(location)
            new BuildScriptHelper(project: project).updateVersionOfSerenityCore(coreVersion)
                .updateVersionOfSerenityCucumber(cucumberVersion)
                .updateVersionOfSerenityJBehave(jbehaveVersion)
        when:
            def result = GradleRunner.create().forwardOutput()
                .withProjectDir(project)
                .withArguments('clean', 'test', 'aggregate')
                .build()
        then:
            result.tasks.findAll({ it.outcome == FAILED }).size() == 0
    }
}