package org.toitlang.intellij.sdk

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.*
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jdom.Element
import org.jetbrains.annotations.Nls
import org.toitlang.intellij.psi.ToitFile
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.Icon
import kotlin.io.path.listDirectoryEntries

class SimpleToitSdkType : SdkType("Toit SDK") {
    override fun suggestHomePath(): String? {
        return null
    }

    override fun suggestHomePaths(): MutableCollection<String> {
        val result = mutableListOf<String>()
        defaultSdkHome().listDirectoryEntries().forEach {
            if (isValidSdkHome(it.toString())) {
                result.add(it.toString())
            }
        }
        return result
    }

    override fun getInvalidHomeMessage(path: String): String {
        return "The path $path does not contain a valid sdk"
    }

    override fun isValidSdkHome(path: String): Boolean {
        return File("$path/lib/core/numbers.toit").exists() &&
                (File("$path/bin/toit.run").exists() ||
                File("$path/bin/toit.run.exe").exists())

    }

    override fun isRelevantForFile(project: Project, file: VirtualFile): Boolean {
        return PsiManager.getInstance(project).findFile(file) is ToitFile
    }

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String): String {
        if (sdkHome.startsWith(defaultSdkHome().toString()))
            return suggestedNameFromVersion(getVersionString(sdkHome))
        return sdkHome
    }

    override fun createAdditionalDataConfigurable(sdkModel: SdkModel, sdkModificator: SdkModificator): AdditionalDataConfigurable? {
        return null
    }

    override fun getIcon(): Icon {
        return AllIcons.Nodes.PpJdk
    }

    override fun getPresentableName(): @Nls(capitalization = Nls.Capitalization.Title) String {
        return "Toit SDK"
    }

    override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) {}

    override fun  getDownloadSdkUrl(): String {
        return "https://github.com/toitlang/toit/releases"
    }

    override fun  getVersionString(sdkHome: String): String {
        val str = String(GeneralCommandLine("$sdkHome/bin/toit.run", "--version").createProcess().inputStream.readAllBytes());
        print(str)
        return str.substring(str.indexOf(": ") + 2)
    }

    companion object {
        fun getInstance(): SimpleToitSdkType {
            return EP_NAME.findExtension(SimpleToitSdkType::class.java)!!
        }
    }

}

fun suggestedNameFromVersion(version: String): String {
    if (version.startsWith("v2.0.0-alpha")) {
        val v = version.substring("v2.0.0-alpha.".length)
        return "Toit 2 alpha $v"
    }
    return "Toit $version"
}

fun defaultSdkHome(): Path {
    val home = Paths.get(FileUtil.toCanonicalPath(System.getProperty("user.home") ?: "."))
    return when {
        SystemInfo.isLinux   -> home.resolve(".tdks")
        SystemInfo.isMac     -> home.resolve("Library/Toit/SDKs")
        SystemInfo.isWindows -> home.resolve(".tdks")
        else -> error("Unsupported OS: ${SystemInfo.getOsNameAndVersion()}")
    }
}