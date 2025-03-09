package io.github.sfuri

import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import java.io.File

class PublicDeclarationFinder {
    val indentTab = "    "
    private val compilerConfig =
        CompilerConfiguration().apply {
            put(CommonConfigurationKeys.MODULE_NAME, "test")
        }

    private val environment =
        KotlinCoreEnvironment.createForProduction(Disposer.newDisposable(), compilerConfig, EnvironmentConfigFiles.JVM_CONFIG_FILES)

    private val psiFactory = KtPsiFactory(environment.project)

    fun getFileFromResources(filename: String): File =
        File(
            this::class.java.classLoader
                .getResource(filename)!!
                .toURI(),
        )

    private fun String.toPsiFile() = psiFactory.createFile(this)

    fun printDeclarationAST(
        declaration: KtDeclaration,
        indent: String,
    ) {
        when (declaration) {
            is KtProperty -> {
                val name = declaration.nameIdentifier?.text ?: "unnamed"
                val type = declaration.typeReference?.text?.let { ": $it" } ?: ""
                val mutability = if (declaration.isVar) "var" else "val"

                println("$indent$mutability $name$type")
            }

            is KtNamedFunction -> {
                val name = declaration.nameIdentifier?.text ?: "unnamed"
                val typeParamsText = declaration.typeParameterList?.text ?: ""

                val paramsText =
                    if (declaration.valueParameterList != null) {
                        val params =
                            declaration.valueParameterList!!
                                .parameters
                                .joinToString(", ") { "${it.name}: ${it.typeReference!!.text}" }
                        "($params)"
                    } else {
                        "()"
                    }

                val retType = declaration.typeReference?.text?.let { ": $it" } ?: ""
                println("$indent${("fun $typeParamsText$name$paramsText$retType").trim()}")
            }

            is KtClassOrObject -> {
                val keyword =
                    when (declaration) {
                        is KtClass -> {
                            when {
                                declaration.isEnum() -> "enum class"
                                declaration.isInterface() -> "interface"
                                else -> "class"
                            }
                        }
                        is KtObjectDeclaration -> if (declaration.isCompanion()) "companion object" else "object"
                        else -> "class"
                    }

                val superTypes =
                    declaration.superTypeListEntries
                        .map { it.typeReference?.text }
                val superTypesText =
                    if (superTypes.isNotEmpty()) {
                        ": ${superTypes.joinToString(", ")}"
                    } else {
                        ""
                    }
                val name = declaration.nameIdentifier?.text ?: "unnamed"

                println("$indent$keyword $name$superTypesText {")

                val newIndent = "$indent${this.indentTab}"

                if (declaration is KtClass && declaration.isEnum()) {
                    declaration.declarations
                        .filterIsInstance<KtEnumEntry>()
                        .forEach { println("$newIndent${it.nameIdentifier!!.text},") }

                    declaration.declarations
                        .filter { it !is KtEnumEntry && it.isPublic }
                        .forEach { printDeclarationAST(it, newIndent) }
                } else {
                    declaration.declarations.filter { it.isPublic }.forEach {
                        printDeclarationAST(it, newIndent)
                    }
                }
                println("$indent}")
            }
        }
    }

    fun lazyLoadDirDeclarations(directory: File): Sequence<KtDeclaration> =
        directory
            .walk()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { it.readText().toPsiFile().declarations }
}

fun main() {
    val pdf = PublicDeclarationFinder()
    pdf
        .lazyLoadDirDeclarations(pdf.getFileFromResources("Exposed"))
        .forEach { pdf.printDeclarationAST(it, "") }
}
