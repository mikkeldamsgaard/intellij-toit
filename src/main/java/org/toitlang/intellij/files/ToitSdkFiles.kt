package org.toitlang.intellij.files

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.indexing.IndexableSetContributor
import org.toitlang.intellij.psi.ToitFile
import org.toitlang.intellij.psi.ast.ToitPrimaryLanguageElement
import org.toitlang.intellij.psi.ast.ToitReferenceTarget
import org.toitlang.intellij.psi.scope.ToitScope
import org.toitlang.intellij.sdk.SimpleToitSdkType
import org.toitlang.intellij.ui.ToitNotifier
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.collections.HashSet

class ToitSdkFiles : IndexableSetContributor() {
    override fun getAdditionalProjectRootsToIndex(project: Project): Set<VirtualFile> {
        val sdks = HashSet<Sdk>()

        ModuleManager.getInstance(project).modules.forEach {
            val moduleSdk = ModuleRootManager.getInstance(it).sdk
            if (moduleSdk?.sdkType is SimpleToitSdkType) sdks.add(moduleSdk)
        }

        if (ProjectRootManager.getInstance(project).projectSdk?.sdkType is SimpleToitSdkType) {
            sdks.add(ProjectRootManager.getInstance(project).projectSdk!!)
        }

        val roots = HashSet<VirtualFile>()
        sdks.forEach {
            val root = Util.getSdkRoot(it)
            if (root != null) roots.add(root)
        }

        return roots
    }

    override fun getAdditionalRootsToIndex(): Set<VirtualFile> {
        return emptySet()
    }

    object Util {
        fun getSdk(file: ToitFile): Sdk? {
            // First try to get the SDK for the module
            val module = ModuleUtil.findModuleForFile(file)
            if (module != null) {
                val moduleSdk = ModuleRootManager.getInstance(module).sdk
                if (moduleSdk?.sdkType is SimpleToitSdkType) return moduleSdk
            }

            // Then try to get the SDK for the project
            val projectSdk = ProjectRootManager.getInstance(file.project).projectSdk
            return if (projectSdk?.sdkType is SimpleToitSdkType) projectSdk else null
        }

        @JvmStatic
        fun getSdkRoot(file: ToitFile): VirtualFile? {
            val sdk = getSdk(file) ?: return null
            return getSdkRoot(sdk)
        }

        fun getSdkRoot(sdk: Sdk): VirtualFile? {
            return sdk.homeDirectory?.findFileByRelativePath("lib");
        }

        @JvmStatic
        fun findLibraryFile(file: ToitFile, path: List<String?>?): ToitFile? {
            val root = getSdkRoot(file) ?: return null
            val pathsToFind = ToitFileResolver.constructSearchPaths(path)
            val vf = ToitFileResolver.findRelativeIgnoreUnderscoreMinus(root, "", pathsToFind) ?: return null
            return PsiManager.getInstance(file.project).findFile(vf) as ToitFile?
        }

        private val cachedScopes: MutableMap<Sdk, CachedValue<ToitScope>?> = HashMap()

        @JvmStatic
        fun getCoreScope(file: ToitFile): ToitScope {
            val sdk = getSdk(file) ?: return ToitScope.ROOT.sub("core")

            var cache = cachedScopes[sdk]
            if (cache == null) {
                cache = buildCache(sdk, file.project)
                cachedScopes[sdk] = cache
            }
            return cache.value
        }

        private fun buildCache(sdk: Sdk, project: Project): CachedValue<ToitScope> {
            return CachedValuesManager.getManager(project).createCachedValue {
                val root = getSdkRoot(sdk) ?: return@createCachedValue CachedValueProvider.Result(ToitScope.ROOT, ModificationTracker.NEVER_CHANGED)
                val dependencies: MutableList<PsiElement?> = ArrayList(2000)
                val coreElements: MutableMap<String, List<ToitReferenceTarget?>> = HashMap()
                val core = root.findFileByRelativePath("core")
                if (core != null) {
                    val psiM = PsiManager.getInstance(project)
                    val coreFiles = Arrays.stream(core.children)
                            .map { file: VirtualFile? -> psiM.findFile(file!!) }
                            .filter { obj: PsiFile? -> Objects.nonNull(obj) }
                            .map { obj: PsiFile? -> ToitFile::class.java.cast(obj) }
                            .collect(Collectors.toList())
                    for (toitFile in coreFiles) {
                        val locals = toitFile.toitFileScope.locals
                        coreElements.putAll(locals)
                        dependencies.add(toitFile)
                        locals.values.forEach(Consumer { c: List<ToitPrimaryLanguageElement?>? -> dependencies.addAll(c!!) })
                    }
                }
                val scope = ToitScope.ROOT.subFromMap("core", coreElements)
                CachedValueProvider.Result(scope, *dependencies.toTypedArray<PsiElement?>() as Array<Any?>)
            }
        }
    }

}
