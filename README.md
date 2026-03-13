# CodecMedia Kotlin

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1%2B-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Java Version](https://img.shields.io/badge/Java%20Version-codecmedia--java-007396?logo=openjdk&logoColor=white)](https://github.com/TamKungZ/codecmedia-java)

Idiomatic Kotlin DSL wrapper for the [CodecMedia](https://github.com/CodecMediaLib/codecmedia-java) Java library.

---

## Requirements

- Java 17+
- Kotlin 2.1+
- Gradle 8+

---

## Installation

```kotlin
dependencies {
    implementation("me.tamkungz.codecmedia:codecmedia-kotlin:1.1.0")
}
```

To use a local build:

```bash
./gradlew publishToMavenLocal
```

```kotlin
repositories { mavenLocal() }

dependencies {
    implementation("me.tamkungz.codecmedia:codecmedia-kotlin:1.1.0")
}
```

---

## Quick Start

### Scope-based usage

```kotlin
import me.tamkungz.codecmedia.kotlin.codecMedia

val media = codecMedia()
```

### Block-style entry point

```kotlin
codecMedia {
    val info = probe("song.mp3")
    play("song.mp3") { dryRun = true }
}
```

---

## API Reference

### probe / get

Probes a file and returns technical stream info (codec, sample rate, resolution, container details).

```kotlin
val info = media.probe("song.mp3")
val info = media.probe(File("song.mp3"))
val info = media.probe(Path.of("song.mp3"))

// get() is an alias of probe()
val info = media.get("image.png")
```

---

### readMetadata

Returns probe-derived metadata merged with any entries from the sidecar `.codecmedia.properties` file.

> Note: Not a full embedded-tag extractor — no ID3 album art / APIC.

```kotlin
val meta = media.readMetadata("song.mp3")
```

---

### writeMetadata

Writes metadata to the sidecar `.codecmedia.properties` file next to the input.

```kotlin
// DSL builder
media.writeMetadata("song.mp3") {
    title   = "My Song"
    artist  = "TamKungZ_"
    album   = "My Album"
    year    = "2026"
    comment = "recorded live"
    extra("encoder", "FFmpeg")   // arbitrary key-value
    put("source", "studio")      // alias of extra()
}

// Direct Metadata object
val meta = Metadata(mapOf("title" to "My Song"))
media.writeMetadata("song.mp3", meta)
```

| Field | Type | Description |
|---|---|---|
| `title` | `String?` | Track or file title |
| `artist` | `String?` | Artist or creator name |
| `album` | `String?` | Album name |
| `year` | `String?` | Release year e.g. `"2026"` |
| `comment` | `String?` | Free-text comment |
| `extra(key, value)` | `fun` | Arbitrary sidecar entry |

---

### validate

Validates file existence, size, and optionally runs a strict parser-level check.

```kotlin
// defaults: strict = false, maxBytes = 500 MB
media.validate("video.mp4")

// custom options
media.validate("video.mp4") {
    strict   = true
    maxBytes = 100 * 1024 * 1024L   // 100 MB
}
```

| Option | Type | Default | Description |
|---|---|---|---|
| `strict` | `Boolean` | `false` | Enable strict parser-level check |
| `maxBytes` | `Long` | `524288000` (500 MB) | Maximum allowed file size |

---

### extractAudio

Extracts the audio track from a media file into an output directory.

```kotlin
// defaults: targetFormat = "m4a", bitrateKbps = 192, streamIndex = 0
media.extractAudio("video.mp4", "/output/dir")

// custom options
media.extractAudio("video.mp4", "/output/dir") {
    targetFormat = "mp3"
    bitrateKbps  = 320
    streamIndex  = 0      // null = engine picks best stream
}
```

| Option | Type | Default | Description |
|---|---|---|---|
| `targetFormat` | `String` | `"m4a"` | Output audio container format |
| `bitrateKbps` | `Int?` | `192` | Target bitrate in kbps; `null` = engine decides |
| `streamIndex` | `Int?` | `0` | Zero-based audio stream index; `null` = best stream |

---

### convert

Converts an input file to an output file. The target format is inferred from the output file extension automatically.

```kotlin
// format inferred from extension → "webp"
media.convert("image.png", "image.webp") { overwrite = true }

// explicit format when extension is ambiguous
media.convert("clip.mov", "clip.out") {
    targetFormat = "mp4"
    preset       = "fast"
    overwrite    = true
}
```

| Option | Type | Default | Description |
|---|---|---|---|
| `targetFormat` | `String` | *(inferred from output ext)* | Target format extension |
| `preset` | `String` | `"balanced"` | Encoder preset hint (`"fast"`, `"balanced"`, `"quality"`) |
| `overwrite` | `Boolean` | `false` | Overwrite existing output file |

> Throws `IllegalArgumentException` if `targetFormat` is blank and cannot be inferred.

---

### play

Plays a file or simulates playback for testing.

```kotlin
// defaults: dryRun = false, allowExternalApp = true
media.play("song.mp3")

// custom options
media.play("song.mp3") {
    dryRun           = true    // simulate only — no real playback
    allowExternalApp = false   // stay in-process
}
```

| Option | Type | Default | Description |
|---|---|---|---|
| `dryRun` | `Boolean` | `false` | Simulate playback without opening a player |
| `allowExternalApp` | `Boolean` | `true` | Allow launching the system default app |

---

## Extension Functions

Shorthand functions on `File` and `Path` — no scope needed.

```kotlin
import me.tamkungz.codecmedia.kotlin.*
import java.io.File

// probe
val info = File("song.mp3").probe()

// validate
File("video.mp4").validate { strict = true }

// convert
File("image.png").convertTo(File("image.webp")) { overwrite = true }

// play
File("song.mp3").play { dryRun = true }
```

All extension functions have matching `Path` variants:

```kotlin
Path.of("song.mp3").probe()
Path.of("video.mp4").validate { strict = true }
Path.of("image.png").convertTo(Path.of("image.webp"))
Path.of("song.mp3").play { dryRun = true }
```

---

## Escape Hatch

Access the underlying Java engine directly via `CodecMediaScope.engine`:

```kotlin
val media = codecMedia()
val rawResult = media.engine.someAdvancedMethod(...)
```

---

## Build

```bash
./gradlew build

# publish to local Maven cache for testing
./gradlew publishToMavenLocal
```

---

## License

Apache License 2.0 — see [LICENSE](LICENSE).

---

*by TamKungZ_*