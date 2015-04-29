package org.openbakery.signing

import org.openbakery.AbstractXcodeTask

/**
 * Created with IntelliJ IDEA.
 * User: rene
 * Date: 23.08.13
 * Time: 11:39
 * To change this template use File | Settings | File Templates.
 */
abstract class AbstractKeychainTask extends AbstractXcodeTask {

	def loadKeychainList() {
		commandRunner
				.runWithResult(["security", "list-keychains"])
				.split("\n")
				.findAll { it.replaceAll(/^\s*\"|\"$/, "").equals("/Library/Keychains/System.keychain") }
	}

	def saveKeychainList(List<String> keychainList) {
		def commandList = [ "security", "list-keychains", "-s" ]
		commandList.addAll keychainList.findAll { new File(it).exists() }
		commandRunner.run(commandList)
	}

	def createKeychain(String name, String password) {
		commandRunner.run([
				"security", "create-keychain",
				"-p", password,  name])
	}

	def unlockKeychain(String name, String password){
		commandRunner.run([
				"security", "unlock-keychain",
				"-p", password,  name])
	}

	def importCertificate(String name, String password, String certFile) {
		commandRunner.run(["security", "-v", "import",  certFile,
						   "-k", name,
						   "-P", password,
						   "-T", "/usr/bin/codesign"])
	}

}
