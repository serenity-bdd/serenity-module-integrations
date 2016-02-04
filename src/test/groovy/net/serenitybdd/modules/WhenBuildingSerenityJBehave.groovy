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
class WhenBuildingSerenityJBehave extends Specification {

    @Rule
    final TemporaryFolder temporary = new TemporaryFolder()


    def "serenity-jbehave tests should work with last changes in serenity modules"() {
        given:
            def File location = temporary.getRoot()
            def project = new ProjectBuildHelper(project: "serenity-jbehave").prepareProject(location)
            def version = ProjectDependencyHelper.publish("serenity-core", location)
            new BuildScriptHelper(project: project).updateVersionOfSerenityCore(version)

        when:
            def result = GradleRunner.create().forwardOutput()
                .withProjectDir(project)
                .withArguments('clean', 'test', 'install')
                .build()

        then:
            result.tasks.findAll({ it.outcome == FAILED }).size() == 0
    }

    def "serenity-jbehave tests should execute successfully with latest published serenity modules"() {
        given:
            def File location = temporary.getRoot()
            def project = new ProjectBuildHelper(project: "serenity-jbehave").prepareProject(location)
        when:
            def result = GradleRunner.create().forwardOutput()
                .withProjectDir(project)
                .withArguments('clean', 'test', 'install')
                .build()

        then:
            result.tasks.findAll({ it.outcome == FAILED }).size() == 0
    }
}