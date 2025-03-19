import io.github.sfuri.PsiUtils
import io.github.sfuri.PublicDeclarationFinder
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.haveSubstring
import java.io.File
import java.nio.file.Files
import kotlin.io.path.absolutePathString

class PublicDeclarationFinderTest :
    StringSpec({
        val psiFactory = PsiUtils.defaultPsiFactory
        val pdf = PublicDeclarationFinder()

        val testSourceCode =
            """
            package test
            
            class Test {
                val property: Int = 0
                var mutableProperty: String = ""
                
                private val privateProperty: Boolean = false
                
                fun function(): Unit {
                    println("Hello, world!")
                }
                
                companion object {
                    protected fun staticFunction(): Unit {
                        println("Hello, world!")
                    }
                }
            }
            
            sealed interface SealedClass {
                object Object : SealedClass()
                data class Data(val value: Int) : SealedClass()
            }
            
            enum class EnumClass {
                VALUE1,
                VALUE2
            }
            
            private fun privateFunction(): Unit {
                println("Hello, world!")
            }
            
            """.trimIndent()

        "PublicDeclarationFinder should find public declarations in a file" {
            val psiFile = psiFactory.createFile(testSourceCode)
            val publicDeclarations = pdf.stringifyPublicDeclarations(psiFile.declarations.asSequence()).toList()

            publicDeclarations.size shouldBe 3
            publicDeclarations shouldContain
                """
                class Test {
                    val property: Int
                    var mutableProperty: String
                    fun function(): Unit
                    companion object  {

                    }
                }
                """.trimIndent()
        }
        
        "PublicDeclarationFinder should find public declarations in a project" {
            val projectDir = ReposUtils.cloneRepoToTmp("kmm-plot-gql", "https://github.com/S-furi/kmm-plot-gql.git")
            val declarations = pdf.loadDeclarations(projectDir)
            val publicDeclarations = pdf.stringifyPublicDeclarations(declarations).toList()

            publicDeclarations.size shouldNotBe 0
            publicDeclarations.find{ it.contains("main") } shouldNotBe null
            publicDeclarations.find{ it.contains("fun ApplicationEngineEnvironment.browserLauncherModule") } shouldNotBe null
        }
    })

object ReposUtils {
    fun cloneRepoToTmp(name: String, url: String): File {
        val repoPath = Files.createTempDirectory("$name-repo")

        val process = ProcessBuilder("git", "clone", url, repoPath.absolutePathString())
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        require(exitCode == 0) { "Failed to clone repository (exit code $exitCode): $output" }
        return repoPath.toFile()
    }
}