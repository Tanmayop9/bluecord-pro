package mods.audio.effects

/**
 * Voice effects that can be applied during audio recording
 */
enum class VoiceEffect(val displayName: String) {
    NONE("Normal"),
    PITCH_HIGH("Chipmunk"),
    PITCH_LOW("Deep Voice"),
    ROBOT("Robot"),
    ECHO("Echo"),
    REVERB("Reverb"),
    ALIEN("Alien"),
    RADIO("Radio"),
    TELEPHONE("Telephone");

    companion object {
        @JvmStatic
        fun fromOrdinal(ordinal: Int): VoiceEffect {
            return values().getOrNull(ordinal) ?: NONE
        }
    }
}
