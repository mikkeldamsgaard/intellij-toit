package org.toitlang.intellij.ui

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.externalSystem.service.ui.setSelectedJdkReference
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkModel
import com.intellij.openapi.projectRoots.SdkModificator
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.*
import com.intellij.openapi.util.NlsContexts.ConfigurableName
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.PlatformUtils
import org.jetbrains.annotations.Nls
import org.toitlang.intellij.sdk.SimpleToitSdkType
import javax.swing.JComponent
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel
import java.awt.Dialog
import java.awt.Frame
import javax.swing.JDialog

class ToitApplicationSettings : Configurable {
    var changed = false
    var sdkComboBox: SdkComboBox? = null
    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) @ConfigurableName String? {
        return "Toit"
    }
    override fun createComponent(): JComponent? {
        if (PlatformUtils.isIntelliJ() || PlatformUtils.getPlatformPrefix() == "AndroidStudio") {
            return panel {
                row() {
                    text("Use project settings to change Toit SDK")
                }
            }
        } else {
            ToitNotifier.notifyDebug("Yo")
            return panel {
                row {
                    text("Select toit sdk")
                }

                ProjectManager.getInstance().defaultProject.let { project ->
                    val sdksModel = ProjectSdksModel()
                    sdksModel.reset(project)
                    sdksModel.addListener(object : SdkModel.Listener {
                        override fun sdkAdded(sdk: Sdk) {
                            runWriteAction {
                                val jdkTable = ProjectJdkTable.getInstance()
                                if (jdkTable.findJdk(sdk.name) == null) {
                                    jdkTable.addJdk(sdk)
                                }
                            }
                        }
                    })
                    SdkComboBoxModel.createSdkComboBoxModel(project, sdksModel, {it is SimpleToitSdkType}).let { model ->
                        sdkComboBox = SdkComboBox(model)

                        sdkComboBox!!.addItemListener {
                            ToitNotifier.notifyDebug("CHANGE Selected sdk: ${sdkComboBox!!.getSelectedSdk()?.name}")
                            changed = true

                        }

                        if (ProjectRootManager.getInstance(project).projectSdk != null) {
                            sdkComboBox!!.setSelectedSdk(ProjectRootManager.getInstance(project).projectSdk!!)
                            ToitNotifier.notifyDebug("Selected sdk: ${sdkComboBox!!.getSelectedSdk()?.name}")
                        }

                        row {
                            cell(sdkComboBox!!)
                        }
                    }
                }
            }
        }
    }

    override fun disposeUIResources() {
    }

    override fun isModified(): Boolean {
        return changed;
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        if (sdkComboBox != null) {
            ProjectManager.getInstance().defaultProject.let { project ->
                runWriteAction {
                    ProjectRootManager.getInstance(project).projectSdk = sdkComboBox!!.getSelectedSdk()
                }
            }
        }
    }
}
