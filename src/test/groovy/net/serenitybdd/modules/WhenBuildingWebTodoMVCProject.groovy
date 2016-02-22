package net.serenitybdd.modules

import net.serenitybdd.modules.utils.BuildScriptHelper
import net.serenitybdd.modules.utils.Project
import net.serenitybdd.modules.utils.ProjectBuildHelper
import net.serenitybdd.modules.utils.ProjectDependencyHelper
import net.serenitybdd.modules.utils.Projects
import net.serenitybdd.modules.utils.TestThreadExecutorService
import net.thucydides.core.model.TestOutcome
import net.thucydides.core.reports.OutcomeFormat
import net.thucydides.core.reports.TestOutcomeLoader
import net.thucydides.core.reports.TestOutcomes
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static Projects.*

/**
 * User: YamStranger
 * Date: 1/31/16
 * Time: 2:07 AM
 */
class WhenBuildingWebTodoMVCProject extends Specification {

    @Rule
    final TemporaryFolder temporary = new TemporaryFolder()

    def "web-todomvc-tests should be successful with last changes in serenity modules"() {
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

            def cucumberFeature = service.submit(new Callable() {
                @Override
                String call() throws Exception {
                    ProjectDependencyHelper.publish(SERENITY_CUCUMBER, location, { it ->
                        new BuildScriptHelper(project: it).updateVersionOfSerenityCore(coreVersion)
                    })
                }
            })

            def testProject = service.submit(new Callable() {
                @Override
                File call() throws Exception {
                    new ProjectBuildHelper(project: WEB_TODOMVC_TESTS).prepareProject(location)
                }
            })

            def project = (File) testProject.get()
            new BuildScriptHelper(project: project).updateVersionOfSerenityCore(coreVersion)
                .updateVersionOfSerenityCucumber((String) cucumberFeature.get())
                .updateVersionOfSerenityJBehave((String) jbehaveFeature.get())
        when:
            def result = GradleRunner.create().forwardOutput()
                .withProjectDir(project)
                .withArguments('clean', 'test', 'aggregate')
                .build()
        then:
            result.tasks.findAll({ it.outcome == FAILED }).size() == 0
    }
}