package io.github.sfuri

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import java.io.File

class PublicDeclarationFinder {
    val indentTab = "    "

    private val psiFactory = PsiUtils.defaultPsiFactory

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
    ) = this.stringifyPublicDeclaration(declaration, indent).also(::println)

    fun stringifyPublicDeclaration(
        declaration: KtDeclaration,
        indent: String,
    ) = when (declaration) {
        is KtProperty -> stringifyProperty(declaration, indent)
        is KtNamedFunction -> stringifyFunction(declaration, indent)
        is KtClassOrObject -> stringifyClassOrObject(declaration, indent)
        else -> ""
    }

    private fun stringifyProperty(
        declaration: KtProperty,
        indent: String,
    ): String {
        val name = declaration.nameIdentifier?.text ?: "unnamed"
        val type = declaration.typeReference?.text?.let { ": $it" } ?: ""
        val mutability = if (declaration.isVar) "var" else "val"

        return "$indent$mutability $name$type"
    }

    private fun stringifyFunction(
        declaration: KtNamedFunction,
        indent: String,
    ): String {
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
        return "$indent${("fun $typeParamsText$name$paramsText$retType").trim()}"
    }

    private fun stringifyClassOrObject(
        declaration: KtClassOrObject,
        indent: String,
    ): String {
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

        val superTypesText =
            declaration.superTypeListEntries
                .map { it.typeReference?.text }
                .apply {
                    if (this.isNotEmpty()) {
                        ": ${this.joinToString(", ")}"
                    } else {
                        ""
                    }
                }

        val name = declaration.nameIdentifier?.text ?: "unnamed"

        val fullDefinition = "$indent$keyword $name$superTypesText {"

        val newIndent = "$indent${this.indentTab}"

        if (declaration is KtClass && declaration.isEnum()) {
            val enumVals =
                declaration.declarations
                    .filterIsInstance<KtEnumEntry>()
                    .joinToString("\n") { "$newIndent${it.nameIdentifier!!.text}," }

            val enumDeclarations =
                declaration.declarations
                    .filter { it !is KtEnumEntry && it.isPublic }
                    .joinToString("\n") { stringifyPublicDeclaration(it, newIndent) }

            return "$fullDefinition\n$enumVals\n$enumDeclarations\n$indent}"
        }

        val declarations =
            declaration.declarations.filter { it.isPublic }.joinToString("\n") {
                stringifyPublicDeclaration(it, newIndent)
            }
        return "$fullDefinition\n$declarations\n$indent}"
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
