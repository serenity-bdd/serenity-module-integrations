package net.serenitybdd.modules.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileSystemLoopException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

/**
 * User: YamStranger
 * Date: 1/31/16
 * Time: 8:39 PM
 */
class TreeCopier implements FileVisitor<Path> {
    private static final Logger log = LoggerFactory.getLogger(TreeCopier.class);

    private final Path source;
    private final Path target;

    TreeCopier(Path source, Path target) {
        this.source = source;
        this.target = target;
    }

    @Override
    def FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if(dir.getFileName().startsWith(".git")){
            return FileVisitResult.SKIP_SUBTREE;
        }
        Path newdir = target.resolve(source.relativize(dir));
        try {
            Files.copy(dir, newdir, StandardCopyOption.REPLACE_EXISTING);
        } catch (FileAlreadyExistsException x) {
            // ignore
        } catch (IOException x) {
            log.error(String.format("Unable to create: %s: %s%n", newdir, x))
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    def FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        copyFile(file, target.resolve(source.relativize(file)));
        return FileVisitResult.CONTINUE;
    }

    @Override
    def FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        if (exc == null) {
            Path newdir = target.resolve(source.relativize(dir));
            try {
                FileTime time = Files.getLastModifiedTime(dir);
                Files.setLastModifiedTime(newdir, time);
            } catch (IOException x) {
                log.error(String.format("Unable to copy all attributes to: %s: %s%n", newdir, x));
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    def FileVisitResult visitFileFailed(Path file, IOException exc) {
        if (exc instanceof FileSystemLoopException) {
            log.error(String.format("cycle detected: " + file));
        } else {
            log.error(String.format("Unable to copy: %s: %s%n", file, exc));
        }
        return FileVisitResult.CONTINUE;
    }

    def void copyFile(Path source, Path target) {
        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException x) {
            log.error(String.format("Unable to copy: %s: %s%n", source, x));
        }
    }
}