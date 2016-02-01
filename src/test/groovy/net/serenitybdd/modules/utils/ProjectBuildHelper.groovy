package net.serenitybdd.modules.utils


import net.thucydides.core.util.FileSystemUtils
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path

/**
 * User: YamStranger
 * Date: 1/31/16
 * Time: 1:55 AM
 */
public class ProjectBuildHelper {

    def String project

    def public Path prepareProject(def File destination) {
        final def projectDir = FileSystemUtils.getResourceAsFile("projects/$project");
        if (!destination.exists()) {
            destination.mkdirs()
        }
        destination = new File(destination, project)
        if (!projectDir) {
            throw new IllegalArgumentException("can not find project with name $project in resources")
        }
        EnumSet<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        TreeCopier copier = new TreeCopier(projectDir.toPath(), destination.toPath());
        Files.walkFileTree(projectDir.toPath(), options, Integer.MAX_VALUE, copier);
        destination.toPath()
    }
}