package net.serenitybdd.modules

import net.serenitybdd.modules.utils.ProjectBuildHelper
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Ignore
import spock.lang.Specification
import static org.gradle.testkit.runner.TaskOutcome.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder

public class WhenBuildingSerenityCore extends Specification {

    @Rule
    final TemporaryFolder temporary = new TemporaryFolder()

    def "clean test integrationTests install should execute successfully for serenity-core"() {
        given:
            def core = new ProjectBuildHelper(project: "serenity-core").prepareProject(temporary.getRoot())
        when:
            def result = GradleRunner.create().forwardOutput()
                .withProjectDir(core)
                .withArguments(
                'clean', 'test', "integrationTests", 'install')
                .build()
        then:
            result.tasks.findAll({ it.outcome == FAILED }).size() == 0
    }
}