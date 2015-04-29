package org.openbakery

import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by Stefan Gugarel on 02/03/15.
 */
class PlistHelper {
	private static Logger logger = LoggerFactory.getLogger(PlistHelper.class)

	private Project project
	private CommandRunner commandRunner

	PlistHelper(Project project, CommandRunner commandRunner) {
		this.project = project
		this.commandRunner = commandRunner
	}
/**
	 * Reads the value for the given key from the given plist
	 *
	 * @param plist
	 * @param key
     * @param commandRunner The commandRunner to execute commands (This is espacially needed for Unit Tests)
 	 *
	 * @return returns the value for the given key
	 */
	def getValueFromPlist(plist, key) {
		if (plist instanceof File) {
			plist = plist.absolutePath
		}

		try {
			String result = commandRunner.runWithResult([
					"/usr/libexec/PlistBuddy",
					plist,
					"-c",
					"Print :$key"])

			result.startsWith("Array {") ?  result.split("\n").collect{ it.trim() } : result
		} catch (IllegalStateException ignored) {
		}
	}

	def setValueForPlist(def plist, String key, String value) {
		setValueForPlist(plist, "Set :$key $value")
	}

	def setValueForPlist(def plist, String command) {
		File infoPlistFile = plist instanceof File ? plist : new File(project.projectDir, plist)

		if (!infoPlistFile.exists()) {
			throw new IllegalStateException("Info Plist does not exist: $infoPlistFile.absolutePath");
		}

		logger.debug("Set Info Plist Value: {}", command)
		commandRunner.run([
				"/usr/libexec/PlistBuddy",
				infoPlistFile.absolutePath,
				"-c",
				command
		])
	}

}
