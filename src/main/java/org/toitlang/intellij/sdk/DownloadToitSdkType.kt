package org.toitlang.intellij.sdk

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.ControlFlowException
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkModel
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.projectRoots.impl.jdkDownloader.*
import com.intellij.openapi.roots.ui.configuration.projectRoot.SdkDownload
import com.intellij.openapi.roots.ui.configuration.projectRoot.SdkDownloadTask
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.dialog
import com.intellij.ui.dsl.builder.*
import com.intellij.util.BitUtil
import com.intellij.util.io.Decompressor
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.awt.event.ItemEvent
import java.io.FileInputStream
import java.io.IOException
import java.net.http.HttpClient
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.DosFileAttributeView
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import java.util.*
import java.util.function.Consumer
import javax.swing.JComponent

private val LOG = logger<DownloadToitSdkType>()

class DownloadToitSdkType : SdkDownload {
    private val versionComboBox : ComboBox<ToitReleaseVersion> = ComboBox<ToitReleaseVersion>()
    private val installDirTextField : TextFieldWithBrowseButton = TextFieldWithBrowseButton()

    init {
        installDirTextField.addBrowseFolderListener(
                TextBrowseFolderListener(
                        FileChooserDescriptor(false, true, false,
                                false, false, false)))
        versionComboBox.onSelectionChange(::onVersionSelectionChange)
        installDirTextField.setTextFieldPreferredWidth(50);
    }

    private fun onVersionSelectionChange(it: ToitReleaseVersion?) {
        if (it !is ToitReleaseVersion) return

        versionComboBox.selectedItem = it
        installDirTextField.text = FileUtil.getLocationRelativeToUserHome(defaultSdkHome().resolve(it.version).toString())
    }

    override fun supportsDownload(sdkTypeId: SdkTypeId): Boolean {
        return "Toit SDK" == sdkTypeId.name &&
                !ApplicationManager.getApplication().isUnitTestMode
    }

    private fun createPanel(items: List<ToitReleaseVersion>): JComponent {
        versionComboBox.model = CollectionComboBoxModel(items)
        onVersionSelectionChange(items[0])
        return panel {
            row {
                text("Select Toit SDK version to download:")
            }
            row("Versions:") {
                cell(versionComboBox).widthGroup("fields").focused()
            }
            row("Download to:") {
                cell(installDirTextField).widthGroup("fields")
            }
        }
    }

    // Mostly ripped from platform/lang-impl/src/com/intellij/openapi/projectRoots/impl/jdkDownloader/JdkDownloader.kt
    override fun showDownloadUI(sdkTypeId: SdkTypeId, sdkModel: SdkModel, parentComponent: JComponent, selectedSdk: Sdk?, sdkCreatedCallback: Consumer<in SdkDownloadTask>) {
        val dataContext = DataManager.getInstance().getDataContext(parentComponent)
        val project = CommonDataKeys.PROJECT.getData(dataContext)
        if (project?.isDisposed == true) return

        val items = try {
            computeInBackground(project, "Downloading SDK List") {
                fetchVersions()
            }
        } catch (e: Throwable) {
            if (e is ControlFlowException) throw e
            LOG.warn("Failed to download the list of installable JDKs. ${e.message}", e)
            null
        }

        if (project?.isDisposed == true) return

        if (items == null) {
            Messages.showErrorDialog(project, "Could not download the list of installable JDKs", "Download Toit SDK")
            return
        }

        val d = dialog("Download Toit SDK", panel = createPanel(items))
        if (!d.showAndGet()) return

        val installDir = Paths.get(FileUtil.expandUserHome(installDirTextField.text))
        val selectedVersion = versionComboBox.selectedItem

        if (selectedVersion !is ToitReleaseVersion) {
            Messages.showErrorDialog(project, "Please select a version and an install directory", "Download Toit SDK")
            return
        }

        sdkCreatedCallback.accept(newDownloadTask(selectedVersion, installDir.toString()))
    }

    private fun newDownloadTask(selectedVersion: ToitReleaseVersion, installDir: String): SdkDownloadTask {
        return object : SdkDownloadTask {
            override fun getSuggestedSdkName(): String {
                return suggestedNameFromVersion(selectedVersion.version)
            }

            override fun getPlannedHomeDir(): String {
                return installDir
            }

            override fun getPlannedVersion(): String {
                return selectedVersion.version
            }

            override fun doDownload(indicator: ProgressIndicator) {
                indicator.fraction = 0.0
                val asset = selectedVersion.platformAsset
                if (asset == null) {
                    throw Exception("Could not find platform asset for version ${selectedVersion.version}")
                }

                indicator.fraction = 0.1
                indicator.text = "Downloading ${selectedVersion.version}"
                // Create a temp file
                val tempFile = java.io.File.createTempFile("toit", ".tar.gz")
                // Download the asset to the temp file
                val client = HttpClient.newBuilder()
                        .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                        .build()
                val request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(asset))
                        .header("Accept", "application/octet-stream")
                        .header("X-GitHub-Api-Version", "2022-11-28")
                        .method("GET", java.net.http.HttpRequest.BodyPublishers.noBody())
                        .build()
                val status = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofFile(Paths.get(tempFile.absolutePath))).statusCode()
                if (status != 200) {
                    throw Exception("Could not download asset $asset")
                }
                indicator.fraction = 0.7

                // extract the tar.gz file
                indicator.text = "Extracting ${selectedVersion.version}"
                FileInputStream(tempFile).use { fin ->
                    GzipCompressorInputStream(fin).use { gzIn ->
                        TarArchiveInputStream(gzIn).use { tarIn ->
                            var entry: TarArchiveEntry?
                            while (tarIn.nextTarEntry.also { entry = it } != null) {
                                if (entry!!.isDirectory) {
                                    continue
                                }
                                var name : String = entry!!.name
                                if (name.startsWith("toit")) {
                                    name = name.substring(5)
                                }

                                val outputFile = java.io.File(installDir, name)
                                val parent = outputFile.parentFile
                                if (!parent.exists()) {
                                    parent.mkdirs()
                                }

                                Files.copy(tarIn, Paths.get(outputFile.absolutePath), StandardCopyOption.REPLACE_EXISTING)
                                if (entry!!.mode != 0) {
                                    setAttributes(entry!!.mode, Paths.get(outputFile.absolutePath))
                                }
                            }
                        }
                    }
                }
                indicator.fraction = 0.98
                tempFile.delete()
            }
        }
    }

    private inline fun <T : Any?> computeInBackground(project: Project?,
                                                      @NlsContexts.DialogTitle title: String,
                                                      crossinline action: (ProgressIndicator) -> T): T =
            ProgressManager.getInstance().run(object : Task.WithResult<T, Exception>(project, title, true) {
                override fun compute(indicator: ProgressIndicator) = action(indicator)
            })

}

private inline fun <reified T> ComboBox<T>.onSelectionChange(crossinline action: (T) -> Unit) {
    this.addItemListener { e ->
        if (e.stateChange == ItemEvent.SELECTED) action(e.item as T)
    }
}

@Throws(IOException::class)
private fun setAttributes(mode: Int, outputFile: Path) {
    if (SystemInfo.isWindows) {
        val attrs = Files.getFileAttributeView(outputFile, DosFileAttributeView::class.java)
        if (attrs != null) {
            if (BitUtil.isSet(mode, Decompressor.Entry.DOS_READ_ONLY)) attrs.setReadOnly(true)
            if (BitUtil.isSet(mode, Decompressor.Entry.DOS_HIDDEN)) attrs.setHidden(true)
        }
    } else {
        val attrs = Files.getFileAttributeView(outputFile, PosixFileAttributeView::class.java)
        if (attrs != null) {
            val permissions: MutableSet<PosixFilePermission> = EnumSet.noneOf(PosixFilePermission::class.java)
            if (BitUtil.isSet(mode, 256)) permissions.add(PosixFilePermission.OWNER_READ)
            if (BitUtil.isSet(mode, 128)) permissions.add(PosixFilePermission.OWNER_WRITE)
            if (BitUtil.isSet(mode, 64)) permissions.add(PosixFilePermission.OWNER_EXECUTE)
            if (BitUtil.isSet(mode, 32)) permissions.add(PosixFilePermission.GROUP_READ)
            if (BitUtil.isSet(mode, 16)) permissions.add(PosixFilePermission.GROUP_WRITE)
            if (BitUtil.isSet(mode, 8)) permissions.add(PosixFilePermission.GROUP_EXECUTE)
            if (BitUtil.isSet(mode, 4)) permissions.add(PosixFilePermission.OTHERS_READ)
            if (BitUtil.isSet(mode, 2)) permissions.add(PosixFilePermission.OTHERS_WRITE)
            if (BitUtil.isSet(mode, 1)) permissions.add(PosixFilePermission.OTHERS_EXECUTE)
            attrs.setPermissions(permissions)
        }
    }
}
