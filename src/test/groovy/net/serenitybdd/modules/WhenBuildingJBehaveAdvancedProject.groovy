package net.serenitybdd.modules

import net.serenitybdd.modules.utils.*
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

import static net.serenitybdd.modules.utils.Projects.*
import static org.gradle.testkit.runner.TaskOutcome.FAILED

/**
 * User: YamStranger
 * Date: 1/31/16
 * Time: 2:07 AM
 */
class WhenBuildingJBehaveAdvancedProject extends Specification {

    @Rule
    final TemporaryFolder temporary = new TemporaryFolder()

    def "jbehave-advanced should be successful with last changes in serenity modules"() {
        given:
            def File location = new File("/home/yamstranger/dev/src/serenity/serenity-all/serenity-module-integrations/build")//temporary.getRoot()
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
                    new ProjectBuildHelper(project: JBEHAVE_ADVANCED).prepareProject(location)
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
        then:
            result.tasks.findAll({ it.outcome == FAILED }).size() == 0
    }
}