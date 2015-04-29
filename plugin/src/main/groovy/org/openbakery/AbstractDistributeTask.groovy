package org.openbakery

import org.apache.commons.io.FileUtils
import org.openbakery.signing.PackageTask

/**
 * User: rene
 * Date: 11/11/14
 */
class AbstractDistributeTask extends AbstractXcodeTask {

	private File _archiveDirectory;

	@Lazy def applicationNameFromArchive = archiveDirectory.name - ".xcarchive"

	@Lazy File applicationBundleDirectory = _getApplicationBundleDirectory()

	@Lazy File appBundleInfoPlist = _getAppBundleInfoPlist()

	File _getApplicationBundleDirectory() {
		File appBundleDirectory = new File(archiveDirectory, "Products/Applications/${applicationNameFromArchive}.app")
		if (!appBundleDirectory.exists()) {
			throw new IllegalStateException("app directory not found: $appBundleDirectory.absolutePath");
		}
		return appBundleDirectory;
	}

	def _getAppBundleInfoPlist() {
		File infoPlist = new File(applicationBundleDirectory, "Info.plist")
		if (!infoPlist.exists()) {
			throw new IllegalStateException("Info.plist not found: $infoPlist.absolutePath");
		}
		return infoPlist;
	}

	def getDestinationFile = { File outputDirectory, String extension ->
		if (project.xcodebuild.bundleNameSuffix != null) {
			return new File(outputDirectory, "${applicationNameFromArchive}$project.xcodebuild.bundleNameSuffix$extension")
		}
		return new File(outputDirectory, "${applicationNameFromArchive}$extension")
	}

	File getDestinationBundleFile(File outputDirectory, File bundle) {
		if (!bundle.exists()) {
			throw new IllegalArgumentException("cannot find bundle: " + bundle)
		}

		String name = bundle.getName();
		String extension = ""
		int index = name.indexOf(".")
		if (index > 0) {
			extension = name.substring(index);
		}

		return getDestinationFile(outputDirectory, extension)
	}

	File copyBundleToDirectory(File outputDirectory, File bundle) {
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs()
		}

		File destinationBundle = getDestinationBundleFile(outputDirectory, bundle)
		FileUtils.copyFile(getIpaBundle()	, destinationBundle)

		logger.lifecycle("Created bundle archive in {}", outputDirectory)
		return destinationBundle;
	}

	File copyIpaToDirectory(File outputDirectory) {
		return copyBundleToDirectory(outputDirectory, getIpaBundle())
	}


	File copyDsymToDirectory(File outputDirectory) {
		return copyBundleToDirectory(outputDirectory, getDSymBundle())
	}

	File getIpaBundle() {
		File packageDirectory = new File(project.getBuildDir(), PackageTask.PACKAGE_PATH)

		if (!packageDirectory.exists()) {
			throw new IllegalStateException("package does not exist: " + packageDirectory)
		}

		def fileList = packageDirectory.list(
						[accept: { d, f -> f ==~ /.*ipa/ }] as FilenameFilter
		).toList()


		if (fileList.isEmpty()) {
			throw new IllegalStateException("No ipa found")
		}

		return new File(packageDirectory, fileList.get(0))
	}

	File getDSymBundle() {
		File dSym = new File(getArchiveDirectory(), "dSYMs/" + getApplicationNameFromArchive() + ".app.dSYM");
		if (!dSym.exists()) {
			throw new IllegalStateException("dSYM not found: " + dSym)
		}
		return dSym;
	}

	def getArchiveDirectory() {
		if (_archiveDirectory != null) {
			return _archiveDirectory;
		}
		File archiveDirectory = new File(project.getBuildDir(), XcodeBuildArchiveTask.ARCHIVE_FOLDER)
		if (!archiveDirectory.exists()) {
			throw new IllegalStateException("Archive does not exist: " + archiveDirectory)
		}

		def fileList = archiveDirectory.list(
						[accept: { d, f -> f ==~ /.*xcarchive/ }] as FilenameFilter
		).toList()
		if (fileList.isEmpty()) {
			throw new IllegalStateException("No xcarchive found")
		}
		return new File(archiveDirectory, fileList.get(0))
	}

}
