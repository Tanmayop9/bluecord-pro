package mods.audio.effects

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import mods.constants.PreferenceKeys

/**
 * Manages voice effect preferences
 */
object VoiceEffectSettings {
    
    private fun getPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
    
    /**
     * Get the currently selected voice effect
     */
    @JvmStatic
    fun getSelectedEffect(context: Context): VoiceEffect {
        val ordinal = getPreferences(context).getInt(PreferenceKeys.VOICE_EFFECT_SELECTION, 0)
        return VoiceEffect.fromOrdinal(ordinal)
    }
    
    /**
     * Set the selected voice effect
     */
    @JvmStatic
    fun setSelectedEffect(context: Context, effect: VoiceEffect) {
        getPreferences(context)
            .edit()
            .putInt(PreferenceKeys.VOICE_EFFECT_SELECTION, effect.ordinal)
            .apply()
    }
    
    /**
     * Check if voice effects are enabled (not set to NONE)
     */
    @JvmStatic
    fun isEffectEnabled(context: Context): Boolean {
        return getSelectedEffect(context) != VoiceEffect.NONE
    }
}
