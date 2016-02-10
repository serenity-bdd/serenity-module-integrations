package net.serenitybdd.modules

import net.serenitybdd.modules.utils.BuildScriptHelper
import net.serenitybdd.modules.utils.ProjectArtifactsPublisher
import net.serenitybdd.modules.utils.ProjectBuildHelper
import net.serenitybdd.modules.utils.ProjectDependencyHelper
import net.serenitybdd.modules.utils.TestThreadExecutorService
import net.thucydides.core.reports.MultithreadExecutorServiceProvider
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor

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
            def File location = new File(".")//temporary.getRoot()
            def coreVersion = ProjectDependencyHelper.publish("serenity-core", location)

            def ExecutorService service = new TestThreadExecutorService().getExecutorService();
            def jbehaveFeature = service.submit(new Callable() {
                @Override
                String call() throws Exception {
                    def jbehave = new ProjectBuildHelper(project: "serenity-jbehave").prepareProject(location)
                    new BuildScriptHelper(project: jbehave).updateVersionOfSerenityCore(coreVersion)
                    return ProjectDependencyHelper.publish(jbehave)
                }
            })

            def cucumberFeature = service.submit(new Callable() {
                @Override
                String call() throws Exception {
                    def cucumber = new ProjectBuildHelper(project: "serenity-cucumber").prepareProject(location)
                    new BuildScriptHelper(project: cucumber).updateVersionOfSerenityCore(coreVersion)
                    return ProjectDependencyHelper.publish(cucumber)
                }
            })

            def mavenFeature = service.submit(new Callable() {
                @Override
                String call() throws Exception {
                    def maven = new ProjectBuildHelper(project: "serenity-maven-plugin").prepareProject(location)
                    new BuildScriptHelper(project: maven).updateVersionOfSerenityCore(coreVersion)
                    def mavenPluginVersion = ProjectDependencyHelper.publish(maven)
                }
            })

            def testProject = service.submit(new Callable() {
                @Override
                File call() throws Exception {
                    new ProjectBuildHelper(project: "serenity-test-projects").prepareProject(location)
                }
            })

            def project = (File)testProject.get()
            new BuildScriptHelper(project: project).updateVersionOfSerenityCore(coreVersion)
                .updateVersionOfSerenityCucumber((String)cucumberFeature.get())
                .updateVersionOfSerenityJBehave((String)jbehaveFeature.get())
        when:
            def result = GradleRunner.create().forwardOutput()
                .withProjectDir(project)
                .withArguments('clean', 'test', 'aggregate')
                .build()
        then:
            result.tasks.findAll({ it.outcome == FAILED }).size() == 0
    }
}