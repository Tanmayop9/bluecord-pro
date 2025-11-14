package mods.audio.quality

/**
 * Audio quality presets for voice recording
 */
enum class AudioQuality(
    val displayName: String,
    val sampleRate: Int,
    val bitrate: Int
) {
    LOW("Low (32kbps)", 24000, 32000),
    STANDARD("Standard (64kbps)", 48000, 64000),
    HIGH("High (128kbps)", 48000, 128000),
    ULTRA("Ultra (192kbps)", 48000, 192000);

    companion object {
        @JvmStatic
        fun fromOrdinal(ordinal: Int): AudioQuality {
            return values().getOrNull(ordinal) ?: STANDARD
        }
    }
}
