# Simple command-line wrapper written in Kotlin

[![Build Status](https://travis-ci.org/jasoet/fun-kommand.svg?branch=master)](https://travis-ci.org/jasoet/fun-kommand)
[![codecov](https://codecov.io/gh/jasoet/fun-kommand/branch/master/graph/badge.svg)](https://codecov.io/gh/jasoet/fun-kommand)
[![Download](https://api.bintray.com/packages/jasoet/fun/fun-kommand/images/download.svg) ](https://bintray.com/jasoet/fun/fun-kommand/_latestVersion)

Execute command-line by spawning ProcessBuilder. Available on JCenter.

## Features
- Accept command as `String` or `List<String>`.
- Accept `File`, `String` or `InputStream` as standard input.
- Redirect standard output to `File` or `OutputStream` (including `System.out`).
- Accept `Map` as environment variable.
- Helper for accept standard input from main Java process as `Sequence<String>`.
- Return `BufferedInputStream` if output is not redirected, null if otherwise.
- `InputStream` from a command will be easily to be piped (as input) to other command. 
- Pipe command with other command

## Gradle

### Add JCenter repository
```groovy
repositories {
    jcenter()
}
```

### Add dependency 
```groovy
compile 'id.jasoet:fun-kommand:<version>'
```

## Usage
### Execute simple command
```kotlin
// Will throw IOException if command return non zero
val result:BufferedInputStream? = listOf("ls", "-alh").execute()

// Wrap command inside Try<BufferedInputStream?> monad 
val result:Try<BufferedInputStream?> = "ls -alh".tryExecute()

// Execute command and redirect output to standard out 
val result:BufferedInputStream? = "ls -alh".execute(output = System.out)
// Result will always be null if output is defined
```

### Accept `File` Input
```kotlin
val file = File("/var/log/filename.ext")
val result = "tail -f".execute(input = file, output = System.out)
// Result will always be null if output is defined
```

### Accept `String` Input
```kotlin
const val stringInput = """
Lorem Ipsum is simply dummy text of the printing and typesetting industry. 
Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, 
when an unknown printer took a galley of type and scrambled it to make a type specimen book.
"""
val result:String = "cat".executeToString(input = stringInput)
```

### Accept `InputStream` input
```kotlin
val inputStream = FileInputStream("/home/root/input-text.txt")
val result:String = "cat".executeToString(input = inputStream)
```

### Redirect Output to `File` 
```kotlin
val outputFile = Paths.get(tmpDir, UUID.randomUUID().toString()).toFile()
val result = "ls -alh".execute(output = outputFile)
// Result will always be null if output is defined
```

### Redirect Output to `OutputStream` and convert it to `String`
```kotlin
val byteOutputStream = ByteArrayOutputStream()
val result = "ls -alh".execute(output = byteOutputStream)
// Result will always be null if output is defined

val stringResult = byteOutputStream.use {
    it.toString(Charsets.UTF_8.name())
}
```

### Execute Command and return String
```kotlin
val result:String = "ls -alh".executeToString()
```

### Pipe several command and return as String
```kotlin
val result = "cat".execute(input = inputFile)
    .pipe("echo")
    .pipe("wc")
    .toString()
    
val result = "cat".execute(input = inputFile)
    .pipe {
        "echo".execute(input = it)
    }
    .pipe {
        "wc".execute(it)
    }    
```
