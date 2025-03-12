package io.github.sfuri

import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger(PublicDeclarationFinder::class.java)
    if (args.size != 1) {
        logger.error("No directory has been provided as an argument. Please provide a directory.")
        exitProcess(1)
    }

    val pdf = PublicDeclarationFinder()
    val declarations = pdf.loadDeclarations(File(args[0]))
    pdf.printPublicDeclarations(declarations)
}
