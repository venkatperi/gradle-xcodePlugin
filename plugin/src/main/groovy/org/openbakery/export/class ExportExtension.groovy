package org.openbakery.export

import org.gradle.api.Project
import org.gradle.api.Task
import org.openbakery.XcodeExportArchiveTask

/**
 * Created by venkat on 4/28/15.
 */

class ExportExtension {
    String name
    String format
    String provisioningProfileName
    String signingIdentity
    String installerIdentity

    private final Project project

    public ExportExtension(Project project){
        this.project = project
    }

    public void createTask() {
        name = name ?: format
        def taskName = "exportArchive${name.capitalize()}"
        def task = project.task taskName, type: XcodeExportArchiveTask, group: "exportArchive"
        task.extension = this

        def export = project.tasks.findByPath("exportArchive")
        if (export == null) {
            export = project.task "exportArchive", type: Task, group: "exportArchive"
        }
        export.dependsOn taskName
    }
}
