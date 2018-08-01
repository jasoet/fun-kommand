# Simple command-line wrapper written in Kotlin

[![Build Status](https://travis-ci.org/jasoet/fun-kommand.svg?branch=master)](https://travis-ci.org/jasoet/fun-kommand)
[![codecov](https://codecov.io/gh/jasoet/fun-kommand/branch/master/graph/badge.svg)](https://codecov.io/gh/jasoet/fun-kommand)
[![Download](https://api.bintray.com/packages/jasoet/fun/fun-kommand/images/download.svg) ](https://bintray.com/jasoet/fun/fun-kommand/_latestVersion)

Execute command-line by spawning ProcessBuilder

## Features
- Accept command as `String` or `List<String>`.
- Accept `File`, `String` or `InputStream` as standard input.
- Redirect standard output to `File` or `OutputStream` (including `System.out`).
- Accept `Map` as environment variable.
- Helper for accept standard input from main Java process as `Sequence<String>`.

## Gradle

### Add maven repository url
```groovy
repositories {
    maven {
       url "https://dl.bintray.com/jasoet/fun"
    }
}
```

### Add dependency 
```groovy
compile 'id.jasoet:fun-kommand:1.0.0'
```

## Usage
### Execute simple command
```kotlin
// Will throw IOException if command return non zero
val returnCode:Int = listOf("ls", "-alh").execute()

// Wrap command inside Try<Int> monad 
val result:Try<Int> = "ls -alh".tryExecute()

// Execute command and redirect output to standard out 
val returnCode:Int = "ls -alh".execute(output = System.out)
```

### Accept `File` Input
```kotlin
val file = File("/var/log/filename.ext")
val returnCode = "tail -f".execute(input = file, output = System.out)
```

### Accept `String` Input
```kotlin
const val stringInput = """
Lorem Ipsum is simply dummy text of the printing and typesetting industry. 
Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, 
when an unknown printer took a galley of type and scrambled it to make a type specimen book.
"""
val returnCode = listOf("cat").execute(input = stringInput, output = System.out)
```

### Accept `InputStream` input
```kotlin
val inputStream = FileInputStream("/home/root/input-text.txt")
val returnCode = listOf("cat").execute(input = inputStream, output = System.out)
```

### Redirect Output to `File` 
```kotlin
val outputFile = Paths.get(tmpDir, UUID.randomUUID().toString()).toFile()
val returnCode = "ls -alh".execute(output = outputFile)
```

### Redirect Output to `OutputStream` and convert it to `String`
```kotlin
val byteOutputStream = ByteArrayOutputStream()
val returnCode = "ls -alh".execute(output = byteOutputStream)
val stringResult = byteOutputStream.use {
    it.toString(Charsets.UTF_8.name())
}
```
