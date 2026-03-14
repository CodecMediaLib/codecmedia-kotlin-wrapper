@file:Suppress("unused")

package me.tamkungz.codecmedia.kotlin

import me.tamkungz.codecmedia.CodecMedia
import me.tamkungz.codecmedia.CodecMediaEngine
import me.tamkungz.codecmedia.model.Metadata
import me.tamkungz.codecmedia.options.AudioExtractOptions
import me.tamkungz.codecmedia.options.ConversionOptions
import me.tamkungz.codecmedia.options.PlaybackOptions
import me.tamkungz.codecmedia.options.ValidationOptions
import java.io.File
import java.nio.file.Path

// ---------------------------------------------------------------------------
// DSL marker
// ---------------------------------------------------------------------------

/**
 * Marks all CodecMedia DSL builder scopes.
 *
 * Prevents scope leaking — you cannot call an outer builder's methods from
 * inside a nested builder block.
 */
@DslMarker
annotation class CodecMediaDsl

// ---------------------------------------------------------------------------
// Top-level entry points
// ---------------------------------------------------------------------------

/**
 * Creates a [CodecMediaScope] backed by the default [CodecMediaEngine].
 *
 * ```kotlin
 * val media = codecMedia()
 * val info  = media.probe("song.mp3")
 * ```
 */
fun codecMedia(): CodecMediaScope = CodecMediaScope(CodecMedia.createDefault())

/**
 * Creates a [CodecMediaScope], runs [block] on it, and returns the scope.
 *
 * ```kotlin
 * codecMedia {
 *     val info = probe("song.mp3")
 *     play("song.mp3") { dryRun = true }
 * }
 * ```
 */
@CodecMediaDsl
inline fun codecMedia(block: CodecMediaScope.() -> Unit): CodecMediaScope =
    codecMedia().apply(block)

// ---------------------------------------------------------------------------
// Main wrapper scope
// ---------------------------------------------------------------------------

/**
 * Idiomatic Kotlin wrapper around [CodecMediaEngine].
 *
 * Accepts [String], [File], or [Path] for all file arguments and delegates
 * to the underlying engine using [Path] internally.
 *
 * Obtain an instance via [codecMedia]:
 * ```kotlin
 * val media = codecMedia()
 * ```
 *
 * @property engine The underlying Java engine. Accessible for advanced
 *   or escape-hatch use cases.
 */
class CodecMediaScope internal constructor(
    val engine: CodecMediaEngine,
) {

    private fun probePath(file: Path) = engine.probe(file)
    private fun getPath(file: Path) = engine.get(file)
    private fun readMetadataPath(file: Path) = engine.readMetadata(file)
    private fun writeMetadataPath(file: Path, metadata: Metadata) = engine.writeMetadata(file, metadata)
    private fun validatePath(file: Path, options: ValidationOptions) = engine.validate(file, options)
    private fun extractAudioPath(file: Path, outputDir: Path, options: AudioExtractOptions) =
        engine.extractAudio(file, outputDir, options)
    private fun convertPath(input: Path, output: Path, options: ConversionOptions) = engine.convert(input, output, options)
    private fun playPath(file: Path, options: PlaybackOptions) = engine.play(file, options)

    // ------------------------------------------------------------------
    // probe / get
    // ------------------------------------------------------------------

    /**
     * Probes [file] and returns technical stream information such as
     * codec, sample rate, resolution, and container details.
     *
     * ```kotlin
     * val info = media.probe("song.mp3")
     * ```
     */
    fun probe(file: String) = probePath(file.toPath())
    /** @see probe */
    fun probe(file: File)   = probePath(file.toPath())
    /** @see probe */
    fun probe(file: Path)   = probePath(file)

    /**
     * Alias of [probe] — matches the Java API `get()` convenience method.
     *
     * ```kotlin
     * val info = media.get("image.png")
     * ```
     */
    fun get(file: String) = getPath(file.toPath())
    /** @see get */
    fun get(file: File)   = getPath(file.toPath())
    /** @see get */
    fun get(file: Path)   = getPath(file)

    // ------------------------------------------------------------------
    // readMetadata / writeMetadata
    // ------------------------------------------------------------------

    /**
     * Returns probe-derived metadata merged with any entries from the
     * sidecar `.codecmedia.properties` file next to [file].
     *
     * > Note: This is **not** a full embedded-tag extractor (e.g. no ID3
     * > album art / APIC).
     *
     * ```kotlin
     * val meta = media.readMetadata("song.mp3")
     * ```
     */
    fun readMetadata(file: String) = readMetadataPath(file.toPath())
    /** @see readMetadata */
    fun readMetadata(file: File)   = readMetadataPath(file.toPath())
    /** @see readMetadata */
    fun readMetadata(file: Path)   = readMetadataPath(file)

    /**
     * Writes [metadata] to the sidecar `.codecmedia.properties` file
     * next to [file].
     *
     * ```kotlin
     * val meta = Metadata(mapOf("title" to "My Song"))
     * media.writeMetadata("song.mp3", meta)
     * ```
     */
    fun writeMetadata(file: String, metadata: Metadata) = writeMetadataPath(file.toPath(), metadata)
    /** @see writeMetadata */
    fun writeMetadata(file: File,   metadata: Metadata) = writeMetadataPath(file.toPath(), metadata)
    /** @see writeMetadata */
    fun writeMetadata(file: Path,   metadata: Metadata) = writeMetadataPath(file, metadata)

    /**
     * Writes metadata to the sidecar file using a [MetadataBuilder] DSL.
     *
     * ```kotlin
     * media.writeMetadata("song.mp3") {
     *     title   = "My Song"
     *     artist  = "TamKungZ_"
     *     album   = "My Album"
     *     year    = "2026"
     *     extra("encoder", "FFmpeg")
     * }
     * ```
     */
    @CodecMediaDsl
    fun writeMetadata(file: String, block: MetadataBuilder.() -> Unit) =
        writeMetadataPath(file.toPath(), MetadataBuilder().apply(block).build())
    /** @see writeMetadata */
    @CodecMediaDsl
    fun writeMetadata(file: File, block: MetadataBuilder.() -> Unit) =
        writeMetadataPath(file.toPath(), MetadataBuilder().apply(block).build())
    /** @see writeMetadata */
    @CodecMediaDsl
    fun writeMetadata(file: Path, block: MetadataBuilder.() -> Unit) =
        writeMetadataPath(file, MetadataBuilder().apply(block).build())

    // ------------------------------------------------------------------
    // validate
    // ------------------------------------------------------------------

    /**
     * Validates [file] using [ValidationOptions.defaults] (500 MB limit,
     * no strict parse check).
     *
     * ```kotlin
     * media.validate("video.mp4")
     * ```
     */
    fun validate(file: String) = validatePath(file.toPath(), ValidationOptions.defaults())
    /** @see validate */
    fun validate(file: File)   = validatePath(file.toPath(), ValidationOptions.defaults())
    /** @see validate */
    fun validate(file: Path)   = validatePath(file, ValidationOptions.defaults())

    /**
     * Validates [file] with custom options via [ValidateBuilder].
     *
     * ```kotlin
     * media.validate("video.mp4") {
     *     strict   = true
     *     maxBytes = 100 * 1024 * 1024L   // 100 MB
     * }
     * ```
     */
    @CodecMediaDsl
    fun validate(file: String, block: ValidateBuilder.() -> Unit) =
        validatePath(file.toPath(), ValidateBuilder().apply(block).build())
    /** @see validate */
    @CodecMediaDsl
    fun validate(file: File, block: ValidateBuilder.() -> Unit) =
        validatePath(file.toPath(), ValidateBuilder().apply(block).build())
    /** @see validate */
    @CodecMediaDsl
    fun validate(file: Path, block: ValidateBuilder.() -> Unit) =
        validatePath(file, ValidateBuilder().apply(block).build())

    // ------------------------------------------------------------------
    // extractAudio
    // ------------------------------------------------------------------

    /**
     * Extracts the audio track from [file] into [outputDir].
     *
     * Defaults: `targetFormat = "m4a"`, `bitrateKbps = 192`,
     * `streamIndex = 0`.
     *
     * ```kotlin
     * media.extractAudio("video.mp4", "/output/dir") {
     *     targetFormat = "mp3"
     *     bitrateKbps  = 320
     * }
     * ```
     */
    @CodecMediaDsl
    fun extractAudio(file: String, outputDir: String, block: ExtractBuilder.() -> Unit = {}) =
        extractAudioPath(file.toPath(), outputDir.toPath(), ExtractBuilder().apply(block).build())
    /** @see extractAudio */
    @CodecMediaDsl
    fun extractAudio(file: File, outputDir: File, block: ExtractBuilder.() -> Unit = {}) =
        extractAudioPath(file.toPath(), outputDir.toPath(), ExtractBuilder().apply(block).build())
    /** @see extractAudio */
    @CodecMediaDsl
    fun extractAudio(file: Path, outputDir: Path, block: ExtractBuilder.() -> Unit = {}) =
        extractAudioPath(file, outputDir, ExtractBuilder().apply(block).build())

    // ------------------------------------------------------------------
    // convert
    // ------------------------------------------------------------------

    /**
     * Converts [input] to [output].
     *
     * [ConvertBuilder.targetFormat] is inferred automatically from the
     * output file extension when not set explicitly.
     *
     * ```kotlin
     * // extension inferred → targetFormat = "webp"
     * media.convert("image.png", "image.webp") { overwrite = true }
     *
     * // explicit format
     * media.convert("clip.mov", "clip.out") {
     *     targetFormat = "mp4"
     *     preset       = "fast"
     * }
     * ```
     *
     * @throws IllegalArgumentException if [ConvertBuilder.targetFormat] is
     *   blank and cannot be inferred from the output path.
     */
    @CodecMediaDsl
    fun convert(input: String, output: String, block: ConvertBuilder.() -> Unit = {}) =
        convertPath(input.toPath(), output.toPath(), ConvertBuilder(output.toPath().extensionOrNull()).apply(block).build())
    /** @see convert */
    @CodecMediaDsl
    fun convert(input: File, output: File, block: ConvertBuilder.() -> Unit = {}) =
        convertPath(input.toPath(), output.toPath(), ConvertBuilder(output.toPath().extensionOrNull()).apply(block).build())
    /** @see convert */
    @CodecMediaDsl
    fun convert(input: Path, output: Path, block: ConvertBuilder.() -> Unit = {}) =
        convertPath(input, output, ConvertBuilder(output.extensionOrNull()).apply(block).build())

    // ------------------------------------------------------------------
    // play
    // ------------------------------------------------------------------

    /**
     * Plays [file] using [PlaybackOptions.defaults] (`dryRun = false`,
     * `allowExternalApp = true`).
     *
     * ```kotlin
     * media.play("song.mp3")
     * ```
     */
    fun play(file: String) = playPath(file.toPath(), PlaybackOptions.defaults())
    /** @see play */
    fun play(file: File)   = playPath(file.toPath(), PlaybackOptions.defaults())
    /** @see play */
    fun play(file: Path)   = playPath(file, PlaybackOptions.defaults())

    /**
     * Plays [file] with custom options via [PlayBuilder].
     *
     * ```kotlin
     * media.play("song.mp3") {
     *     dryRun           = true   // simulate only — no real playback
     *     allowExternalApp = false  // stay in-process
     * }
     * ```
     */
    @CodecMediaDsl
    fun play(file: String, block: PlayBuilder.() -> Unit = {}) =
        playPath(file.toPath(), PlayBuilder().apply(block).build())
    /** @see play */
    @CodecMediaDsl
    fun play(file: File, block: PlayBuilder.() -> Unit = {}) =
        playPath(file.toPath(), PlayBuilder().apply(block).build())
    /** @see play */
    @CodecMediaDsl
    fun play(file: Path, block: PlayBuilder.() -> Unit = {}) =
        playPath(file, PlayBuilder().apply(block).build())
}

// ---------------------------------------------------------------------------
// DSL option builders
// ---------------------------------------------------------------------------

/**
 * Builder for [Metadata].
 *
 * Standard fields ([title], [artist], [album], [year], [comment]) map to
 * well-known sidecar keys. Use [put] or [extra] for arbitrary keys.
 *
 * ```kotlin
 * media.writeMetadata("song.mp3") {
 *     title  = "My Song"
 *     artist = "TamKungZ_"
 *     extra("encoder", "FFmpeg")
 * }
 * ```
 */
@CodecMediaDsl
class MetadataBuilder {
    /** Track or file title. */
    var title: String? = null
    /** Artist or creator name. */
    var artist: String? = null
    /** Album name. */
    var album: String? = null
    /** Release year (e.g. `"2026"`). */
    var year: String? = null
    /** Free-text comment. */
    var comment: String? = null

    private val entries: MutableMap<String, String> = linkedMapOf()

    /**
     * Adds an arbitrary sidecar key-value pair.
     *
     * ```kotlin
     * put("encoder", "FFmpeg")
     * ```
     */
    fun put(key: String, value: String) {
        entries[key] = value
    }

    /**
     * Alias of [put].
     *
     * ```kotlin
     * extra("encoder", "FFmpeg")
     * ```
     */
    fun extra(key: String, value: String) = put(key, value)

    internal fun build(): Metadata {
        title?.let   { entries["title"]   = it }
        artist?.let  { entries["artist"]  = it }
        album?.let   { entries["album"]   = it }
        year?.let    { entries["year"]    = it }
        comment?.let { entries["comment"] = it }
        return Metadata(entries.toMap())
    }
}

/**
 * Builder for [ValidationOptions].
 *
 * ```kotlin
 * media.validate("video.mp4") {
 *     strict   = true
 *     maxBytes = 100 * 1024 * 1024L
 * }
 * ```
 */
@CodecMediaDsl
class ValidateBuilder {
    /**
     * When `true`, the engine runs a strict parser-level check in addition
     * to the size check. Defaults to `false`.
     */
    var strict: Boolean = false

    /**
     * Maximum allowed file size in bytes. Defaults to `500 MB`.
     * Files larger than this will fail validation.
     */
    var maxBytes: Long = 500L * 1024 * 1024

    internal fun build(): ValidationOptions = ValidationOptions(strict, maxBytes)
}

/**
 * Builder for [AudioExtractOptions].
 *
 * ```kotlin
 * media.extractAudio("video.mp4", "/out") {
 *     targetFormat = "mp3"
 *     bitrateKbps  = 320
 *     streamIndex  = 0
 * }
 * ```
 */
@CodecMediaDsl
class ExtractBuilder {
    /** Output audio container format. Defaults to `"m4a"`. */
    var targetFormat: String = "m4a"

    /**
     * Target audio bitrate in kbps. `null` lets the engine decide.
     * Defaults to `192`.
     */
    var bitrateKbps: Int? = 192

    /**
     * Zero-based index of the audio stream to extract.
     * `null` extracts the default/best stream. Defaults to `0`.
     */
    var streamIndex: Int? = 0

    internal fun build(): AudioExtractOptions = AudioExtractOptions(targetFormat, bitrateKbps, streamIndex)
}

/**
 * Builder for [ConversionOptions].
 *
 * The [targetFormat] is pre-populated from the output file extension when
 * [CodecMediaScope.convert] is called, so you rarely need to set it explicitly.
 *
 * ```kotlin
 * media.convert("image.png", "image.webp") {
 *     overwrite = true
 *     preset    = "fast"
 * }
 * ```
 *
 * @constructor Created internally by [CodecMediaScope.convert].
 * @param defaultTargetFormat Extension inferred from the output path, or
 *   `null` if the path has no extension (must then be set via [targetFormat]).
 */
@CodecMediaDsl
class ConvertBuilder internal constructor(defaultTargetFormat: String?) {
    /**
     * Target format extension (e.g. `"mp4"`, `"webp"`).
     * Auto-set from the output file extension; override only when needed.
     */
    var targetFormat: String = defaultTargetFormat ?: ""

    /**
     * Encoder preset hint (e.g. `"fast"`, `"balanced"`, `"quality"`).
     * Defaults to `"balanced"`.
     */
    var preset: String = "balanced"

    /**
     * When `true`, an existing output file is silently overwritten.
     * Defaults to `false`.
     */
    var overwrite: Boolean = false

    internal fun build(): ConversionOptions {
        val format = targetFormat.ifBlank {
            throw IllegalArgumentException(
                "targetFormat is required for conversion. " +
                "Set it in convert { targetFormat = \"...\" } or use an output file with an extension."
            )
        }
        return ConversionOptions(format, preset, overwrite)
    }
}

/**
 * Builder for [PlaybackOptions].
 *
 * ```kotlin
 * media.play("song.mp3") {
 *     dryRun           = true
 *     allowExternalApp = false
 * }
 * ```
 */
@CodecMediaDsl
class PlayBuilder {
    /**
     * When `true`, playback is simulated without opening a real player.
     * Useful for testing. Defaults to `false`.
     */
    var dryRun: Boolean = false

    /**
     * When `true`, the engine may launch the system default application
     * to open the file. Defaults to `true`.
     */
    var allowExternalApp: Boolean = true

    internal fun build(): PlaybackOptions = PlaybackOptions(dryRun, allowExternalApp)
}

// ---------------------------------------------------------------------------
// Extension functions on File / Path
// ---------------------------------------------------------------------------

/**
 * Probes this file using a fresh default engine.
 *
 * ```kotlin
 * val info = File("song.mp3").probe()
 * ```
 */
fun File.probe() = codecMedia().probe(this)
/** @see File.probe */
fun Path.probe() = codecMedia().probe(this)

/**
 * Validates this file with optional DSL options.
 *
 * ```kotlin
 * File("video.mp4").validate { strict = true }
 * ```
 */
@CodecMediaDsl
fun File.validate(block: ValidateBuilder.() -> Unit = {}) = codecMedia().validate(this, block)
/** @see File.validate */
@CodecMediaDsl
fun Path.validate(block: ValidateBuilder.() -> Unit = {}) = codecMedia().validate(this, block)

/**
 * Converts this file to [output] with optional DSL options.
 *
 * ```kotlin
 * File("image.png").convertTo(File("image.webp")) { overwrite = true }
 * ```
 */
@CodecMediaDsl
fun File.convertTo(output: File, block: ConvertBuilder.() -> Unit = {}) =
    codecMedia().convert(this, output, block)
/** @see File.convertTo */
@CodecMediaDsl
fun Path.convertTo(output: Path, block: ConvertBuilder.() -> Unit = {}) =
    codecMedia().convert(this, output, block)

/**
 * Plays this file with optional DSL options.
 *
 * ```kotlin
 * File("song.mp3").play { dryRun = true }
 * ```
 */
@CodecMediaDsl
fun File.play(block: PlayBuilder.() -> Unit = {}) = codecMedia().play(this, block)
/** @see File.play */
@CodecMediaDsl
fun Path.play(block: PlayBuilder.() -> Unit = {}) = codecMedia().play(this, block)

// ---------------------------------------------------------------------------
// Internal helpers
// ---------------------------------------------------------------------------

private fun String.toPath(): Path = Path.of(this)

private fun Path.extensionOrNull(): String? {
    val name = fileName?.toString().orEmpty()
    val idx  = name.lastIndexOf('.')
    return if (idx >= 0 && idx < name.length - 1) name.substring(idx + 1) else null
}
