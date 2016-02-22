package net.serenitybdd.modules

import net.serenitybdd.modules.utils.*
import net.thucydides.core.reports.OutcomeFormat
import net.thucydides.core.reports.TestOutcomeLoader
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

import static net.serenitybdd.modules.utils.Projects.*
import static org.gradle.testkit.runner.TaskOutcome.FAILED

/**
 * User: YamStranger
 * Date: 1/31/16
 * Time: 2:07 AM
 */
class WhenBuildingJBehaveTestProject extends Specification {

    @Rule
    final TemporaryFolder temporary = new TemporaryFolder()

    def "jbeahve-tags test project report should contains correct number of manual tests"() {
        given:
            def File location = temporary.getRoot()
            def coreVersion = ProjectDependencyHelper.publish(SERENITY_CORE, location)

            def ExecutorService service = new TestThreadExecutorService().getExecutorService();
            def jbehaveFeature = service.submit(new Callable() {
                @Override
                String call() throws Exception {
                    ProjectDependencyHelper.publish(SERENITY_JBEHAVE, location, { it ->
                        new BuildScriptHelper(project: it).updateVersionOfSerenityCore(coreVersion)
                    })
                }
            })

            def testProject = service.submit(new Callable() {
                @Override
                File call() throws Exception {
                    new ProjectBuildHelper(project: JBEHAVE_TAGS).prepareProject(location)
                }
            })

            def project = (File) testProject.get()
            new BuildScriptHelper(project: project).updateVersionOfSerenityCore(coreVersion)
                .updateVersionOfSerenityJBehave((String) jbehaveFeature.get())
        when:
            def result = GradleRunner.create().forwardOutput()
                .withProjectDir(project)
                .withArguments('clean', 'test', 'aggregate')
                .build()
            def output = project.toPath().resolve("target").resolve("site").resolve("serenity").toFile()
            def outcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.JSON).from(output);
        then:
            outcomes.getTests().size() == 11
            outcomes.getOutcomes().findAll({ it -> it.isManual() }).size() == 10
    }
}