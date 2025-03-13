# KDeclaration
A simple kotlin program to print all public declarations inside a Kotlin file or project, expoiting [kotlin-compiler-embeddable](https://central.sonatype.com/artifact/org.jetbrains.kotlin/kotlin-compiler-embeddable).

## Requirements

- JDK >= 21
- Gradle >= 8
- Kotlinc >= 2.1.10

## Usage

Clone this repository:

```bash
git clone https://github.com/S-furi/KPublicDeclaration.git && cd KPublicDeclaration
```

And simply run with:

```bash
./solution.sh [KtFile|Directory]
```

## Features

- Using `Sequence`s for lazy file loading and processing;
- Exploiting AST structure for fine grained control over what is printed out;

### What to Expect

The output will show

- basic keyword (`interface`, `class`, `fun`, `var`/`val`, `enum`) without any modifier (so `sealed`, `internal`, etc. won't be shown);
- type parameters if present;
- name;
- type (or return type for functions/methods) if present;
- for functions/methods, the list of parameters with their associated type, *without* the default value;
- enum classes won't be displayed with their constructor, and a list of enum entries is just show separated by a comma. Obviously, fields or methods definitions will be shown as regular classes.

```kotlin
fun <reified T : Any>json(name: String, jsonConfig: Json, kSerializer: KSerializer<T>): Column<T>
class JsonBColumnType: JsonColumnType<T> {
    val usesBinaryFormat: Boolean
    fun sqlType(): String
}
fun <T : Any>jsonb(name: String, serialize: (T) -> String, deserialize: (String) -> T): Column<T>
fun <reified T : Any>jsonb(name: String, jsonConfig: Json, kSerializer: KSerializer<T>): Column<T>
class ExposedConnectionImpl: DatabaseConnectionAutoRegistration {
    fun invoke(connection: Connection)
}
```

---

## Doubts

- Does annotations and KDoc need to be printed?
- When it comes to one liners functions/methods/fields/properties, does their implementation need to be printed?
- Should modifiers like internal, sealed, etc. have to be displayed?
- Does default parameters values need to be printed?
- When it comes to extension functions, does the receiver type (the extended object) need to be shown?
- What about data classes? should they're constuctor be displayed or can they be treated as standard classes (i.e. as it is now)?
