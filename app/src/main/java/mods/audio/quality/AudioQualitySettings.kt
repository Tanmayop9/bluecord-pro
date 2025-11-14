package mods.audio.quality

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import mods.constants.PreferenceKeys

/**
 * Manages audio quality preferences
 */
object AudioQualitySettings {
    
    private fun getPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
    
    /**
     * Get the currently selected audio quality
     */
    @JvmStatic
    fun getSelectedQuality(context: Context): AudioQuality {
        val ordinal = getPreferences(context).getInt(PreferenceKeys.AUDIO_QUALITY_SELECTION, 1)
        return AudioQuality.fromOrdinal(ordinal)
    }
    
    /**
     * Set the selected audio quality
     */
    @JvmStatic
    fun setSelectedQuality(context: Context, quality: AudioQuality) {
        getPreferences(context)
            .edit()
            .putInt(PreferenceKeys.AUDIO_QUALITY_SELECTION, quality.ordinal)
            .apply()
    }
}
