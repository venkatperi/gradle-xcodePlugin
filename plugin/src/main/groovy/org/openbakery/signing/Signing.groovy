package org.openbakery.signing

import org.gradle.api.Project

/**
 *
 * @author René Pirringer
 *
 */
class Signing {
	public final static KEYCHAIN_NAME_BASE = "gradle-"

	String identity
	List<String> certificateURI = null
	String certificatePassword
	List<String> mobileProvisionURI = null
	String keychainPassword = "This_is_the_default_keychain_password"
	File keychain
	int timeout = -1

	/**
	 * internal parameters
	 */
	@Lazy def signingDestinationRoot = project.getFileResolver().withBaseDir(project.buildDir).resolve("codesign")
	@Lazy def keychainPathInternal = keychain ?: new File(signingDestinationRoot as File, keychainName)
	@Lazy def mobileProvisionDestinationRoot =  project.getFileResolver().withBaseDir(project.buildDir).resolve("provision")

	List<File> mobileProvisionFile = []

//	final String keychainName = "$KEYCHAIN_NAME_BASE${System.currentTimeMillis()}.keychain"
	final String keychainName = "${KEYCHAIN_NAME_BASE}.keychain"
	final Project project

	public Signing(Project project) {
		this.project = project;
	}

	void setCertificateURI(def value) {
		this.certificateURI = value instanceof List ? value : [ value.toString() ]
	}

	void setMobileProvisionURI(def v) {
		this.mobileProvisionURI = v instanceof List ? v : [v.toString()]
	}

	void setMobileProvisionFile(def file) {
		File fileToAdd = file instanceof File ? file : new File(file.toString())

		if (!fileToAdd.exists()) {
			throw new IllegalArgumentException("given mobile provision file does not exist: $fileToAdd.absolutePath")
		}
		mobileProvisionFile.add(fileToAdd)
	}

	@Override
	public String toString() {
		if (this.keychain != null) {
			return "Signing{ identity='$identity', mobileProvisionURI='$mobileProvisionURI', keychain='$keychain'}";
		}
		return "Signing{ identity='$identity', certificateURI='$certificateURI', certificatePassword='$certificatePassword', mobileProvisionURI='$mobileProvisionURI'}";
	}
}
