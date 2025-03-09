package io.github.sfuri

import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtPsiFactory

object PsiUtils {
    private val compilerConfig =
        CompilerConfiguration().apply {
            put(CommonConfigurationKeys.MODULE_NAME, "test")
        }

    private val environment =
        KotlinCoreEnvironment.createForProduction(Disposer.newDisposable(), compilerConfig, EnvironmentConfigFiles.JVM_CONFIG_FILES)

    val defaultPsiFactory = KtPsiFactory(environment.project)
}
