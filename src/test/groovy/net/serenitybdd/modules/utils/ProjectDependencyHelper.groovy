package net.serenitybdd.modules.utils

import org.spockframework.util.Assert
import java.util.concurrent.ConcurrentHashMap

/**
 * User: YamStranger
 * Date: 2/1/16
 * Time: 8:18 AM
 */
class ProjectDependencyHelper {
    private def final static Map<String, String> published = new ConcurrentHashMap<>()
    private def static final String version = ProjectDependencyHelper.class.simpleName.concat("-version")

    /**
     * Copy project from resource dir to particular dir, and build it from here and publish with generated version to
     * local maven repository. If same project already published - build will not be executed.
     * @param project name of resource dir
     * @param folder where project should build (rmp dir for current test)
     * @return version of published artifact
     */
    def static String publish(def String project, def File folder) {
        synchronized (published) {
            if (published.containsKey(project)) {
                println "$project alredy published for version ${published.get(project)}"
                return published.get(project)
            } else {
                def root = new ProjectBuildHelper(project: project).prepareProject(folder)
                new BuildScriptHelper(project: root).updateVersionOfResultArtifact(version)
                Assert.that(new ProjectArtifactsPublisher(project: root).publishArtifacts(), "some task $project not SUCCESS")
                published.put(project, version)
                return version
            }
        }
    }

    def static String publish(def Projects project, def File folder) {
        return publish(project.toString(), folder)
    }

    /**
     * Publish project in this dir, if not published jet. Accepts closure updates to update project
     * @param project directory where build.gradle is located
     * @return version of published artifact
     */
    def static String publish(def String project, def File location, Closure<BuildScriptHelper>... updates) {
        synchronized (published) {
            if (published.containsKey(project)) {
                println "$project alredy published for version ${published.get(project)}"
                return published.get(project)
            } else {
                def root = new ProjectBuildHelper(project: project).prepareProject(location)
                updates.each { func ->
                    func(root)
                }
                new BuildScriptHelper(project: root).updateVersionOfResultArtifact(version)
                Assert.that(new ProjectArtifactsPublisher(project: root).publishArtifacts(), "some task $project not SUCCESS")
                published.put(root.getAbsolutePath(), version)
                return version
            }
        }
    }

    def static String publish(def Projects project, def File location, Closure<BuildScriptHelper>... updates) {
        return publish(project.toString(), location, updates)
    }
}