/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openbakery.signing

import org.gradle.api.tasks.TaskAction
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.openbakery.XcodePlugin

class KeychainCreateTask extends AbstractKeychainTask {

	@Lazy def keychainPath = project.xcodebuild.signing.keychainPathInternal.absolutePath
	@Lazy Signing signing = project.xcodebuild.signing

	KeychainCreateTask() {
		super()
		this.description = "Create a keychain that is used for signing the app"

//		dependsOn(XcodePlugin.KEYCHAIN_CLEAN_TASK_NAME)

		if (!signing.keychain) {
			if (signing.certificateURI)
				inputs.file(signing.certificateURI)
			outputs.file(keychainPath)
		}

		this.setOnlyIf {
			return !project.xcodebuild.isSDK(XcodePlugin.SDK_IPHONESIMULATOR)
		}
	}

	@TaskAction
	def create(IncrementalTaskInputs inputs) {

		if (project.xcodebuild.isSDK(XcodePlugin.SDK_IPHONESIMULATOR)) {
			logger.lifecycle("The simulator build does not need a provisioning profile");
			return
		}

		if (project.xcodebuild.signing.keychain) {
			logger.debug("Using keychain {}", project.xcodebuild.signing.keychain)
			logger.debug("Internal keychain {}", project.xcodebuild.signing.keychainPathInternal)
			return
		}

		if (project.xcodebuild.signing.certificateURI == null) {
			logger.debug("not certificateURI specifed so do not create the keychain");
			return
		}

		if (project.xcodebuild.signing.certificatePassword == null) {
			throw new InvalidUserDataException("Property project.xcodebuild.signing.certificatePassword is missing")
		}

		logger.debug("Create Keychain: {}", keychainPath)

		def file = new File(keychainPath)
		inputs.outOfDate { file.delete() }
		inputs.removed { file.delete() }

		if (!file.exists()) {
			createKeychain keychainPath, project.xcodebuild.signing.keychainPassword

			project.xcodebuild.signing.certificateURI.each {
				def certificateFile = download(project.xcodebuild.signing.signingDestinationRoot, it)
				importCertificate keychainPath, project.xcodebuild.signing.certificatePassword, certificateFile
			}
		}

		if (getOSVersion().minor >= 9) {
			def keychainList = loadKeychainList()
			keychainList.add(keychainPath)
			saveKeychainList(keychainList)
		}

		// Set a custom timeout on the keychain if requested
		if (project.xcodebuild.signing.timeout > 0) {
			commandRunner.run(["security", "-v", "set-keychain-settings", "-lut", project.xcodebuild.signing.timeout.toString(), keychainPath])
		}

		unlockKeychain keychainPath, project.xcodebuild.signing.keychainPassword
	}



}