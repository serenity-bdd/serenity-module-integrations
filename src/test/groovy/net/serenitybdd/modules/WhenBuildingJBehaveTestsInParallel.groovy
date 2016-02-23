package net.serenitybdd.modules

import net.serenitybdd.modules.utils.BuildScriptHelper
import net.serenitybdd.modules.utils.ProjectBuildHelper
import net.serenitybdd.modules.utils.ProjectDependencyHelper
import net.serenitybdd.modules.utils.TestThreadExecutorService
import net.thucydides.core.reports.OutcomeFormat
import net.thucydides.core.reports.TestOutcomeLoader
import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.charset.Charset
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

import static net.serenitybdd.modules.utils.Projects.*

/**
 * User: YamStranger
 * Date: 1/31/16
 * Time: 2:07 AM
 */
class WhenBuildingJBehaveTestsInParallel extends Specification {

    @Rule
    final TemporaryFolder temporary = new TemporaryFolder()

    def "jbeahve module should have ability to work in parallel"() {
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
                    new ProjectBuildHelper(project: JBEHAVE_IN_PARALLEL).prepareProject(location)
                }
            })

            def project = (File) testProject.get()
            new BuildScriptHelper(project: project).updateVersionOfSerenityCore(coreVersion)
                .updateVersionOfSerenityJBehave((String) jbehaveFeature.get())
        when:
            def File classFile = project.toPath()
                .resolve("src")
                .resolve("test")
                .resolve("java")
                .resolve("net")
                .resolve("serenitybdd")
                .resolve("demos")
                .resolve("actions")
                .resolve("UserCanPerformSimpleActionNumber.java").toFile()
            def File story = project.toPath()
                .resolve("src")
                .resolve("test")
                .resolve("resources")
                .resolve("stories")
                .resolve("single_operations")
                .resolve("user_can_perform_simple_action_number.story").toFile()

            def amount = Runtime.runtime.availableProcessors() * 3
            generateTestClasses(classFile, story, amount)
            GradleRunner.create().forwardOutput()
                .withProjectDir(project)
                .withArguments('clean', 'test', 'aggregate')
                .build()
            def output = project.toPath().resolve("target").resolve("site").resolve("serenity").toFile()
            def outcomes = TestOutcomeLoader.loadTestOutcomes().inFormat(OutcomeFormat.JSON).from(output);
        then:
            outcomes.getTests().size() == (amount + 1)
    }

    private def static generateTestClasses(final File classFile, final File story, final int amount) {
        amount.times { number ->
            def createdClass = new File(classFile.getAbsolutePath().replace("Number.java", "Number${number}.java"))
            def List<String> lines = classFile.readLines("UTF-8")
            createdClass.withWriter { w ->
                lines.each { line ->
                    w.write(line.replace("Number extends", "Number${number} extends") + "\n")
                }
                w.flush()
            }
            def createdStory = new File(story.getAbsolutePath().replace("number.story", "number_${number}.story"))
            lines = story.readLines("UTF-8")
            createdStory.withWriter { w ->
                lines.each { line ->
                    w.write(line.replace("scenario number", "scenario number ${number}") + "\n")
                }
                w.flush()
            }
        }
    }
}
