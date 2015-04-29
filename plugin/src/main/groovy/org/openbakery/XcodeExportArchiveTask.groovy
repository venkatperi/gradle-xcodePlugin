package org.openbakery

import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskAction
import org.gradle.logging.StyledTextOutput
import org.gradle.logging.StyledTextOutputFactory
import org.openbakery.export.ExportExtension
import org.openbakery.output.XcodeBuildOutputAppender

/**
 * Created by venkat on 4/28/15.
 */
class XcodeExportArchiveTask extends AbstractXcodeBuildTask {
    public static final String EXPORTS_FOLDER = "exports"

    @Lazy def outputDirectory = _outputDir()

    @Lazy def outputFileName = "${outputDirectory.path}/${project.xcodebuild.bundleName}"

    ExportExtension extension

    XcodeExportArchiveTask() {
        super()
        dependsOn(XcodePlugin.ARCHIVE_TASK_NAME)
        this.description = "Exports archive in specified format"
    }

    def _outputDir()  {
        def path = [EXPORTS_FOLDER]
        if (extension.name != null)
            path.add(extension.name)
        def dir = new File(project.buildDir, path.join("/") )
        dir.mkdirs()
        return dir
    }

    @TaskAction
    def export() {
        def commandList = [
        project.xcodebuild.xcodebuildCommand, "-exportArchive" ,
         "-archivePath", project.xcodebuild.archiveDirectory.path,
         "-exportFormat", extension.format,
         "-exportProvisioningProfile", extension.provisioningProfileName,
         "-exportPath",  outputFileName.toString()
        ]

        if (extension.signingIdentity != null) {
            commandList.add("-exportSigningIdentity")
            commandList.add(extension.signingIdentity)
        }

        if (extension.installerIdentity != null) {
            commandList.add("-exportInstallerIdentity")
            commandList.add(extension.installerIdentity)
        }

        def outputFile = new File("${outputFileName}.${extension.format}")
        if (outputFile.exists())
            outputFile.delete()

        StyledTextOutput output = getServices().get(StyledTextOutputFactory.class).create(XcodeBuildTask.class, LogLevel.LIFECYCLE);
        Map<String, String> environment = project.xcodebuild.environment

        if (!project.getBuildDir().exists()) {
            project.getBuildDir().mkdirs()
        }
        commandRunner.setOutputFile(new File(project.buildDir, "xcodebuild-output.txt"));
        commandRunner.run("${project.projectDir.absolutePath}", commandList, environment, new XcodeBuildOutputAppender(output))
        logger.lifecycle("Done")
    }

}
