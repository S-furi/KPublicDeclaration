import io.github.sfuri.PsiUtils
import io.github.sfuri.PublicDeclarationFinder
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class PublicDeclarationFinderTest :
    StringSpec({
        val psiFactory = PsiUtils.defaultPsiFactory

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

        "PublicDeclarationFinder should find public declarations" {
            val psiFile = psiFactory.createFile(testSourceCode)
            val pdf = PublicDeclarationFinder()

            val publicDeclarations = pdf.stringifyPublicDeclarations(psiFile.declarations.asSequence()).toList()

            publicDeclarations.forEach(::println)
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
    })
