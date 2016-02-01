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

    def static String publish(def String project, def File folder) {
        synchronized (published) {
            if (published.containsKey(project)) {
                return published.get(project)
            } else {
                def root = new ProjectBuildHelper(project: project).prepareProject(folder)
                new BuildScriptHelper(project: root).updateVersionOfResultArtifact(version)
                Assert.that(new ProjectArtifactsPublisher(project: root).publishArtifacts(),"some task $project not SUCCESS")
                published.put(project, version)
                return version
            }
        }
    }
}