package mods.audio.enhancement

import mods.audio.converters.AudioConstants
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Audio enhancement processor for noise reduction and quality improvement
 */
class AudioEnhancer {
    
    companion object {
        private const val NOISE_THRESHOLD = 500 // Threshold for noise gate
        private val recentSamples = mutableListOf<Short>()
        private const val SMOOTHING_WINDOW = 10
        
        /**
         * Apply noise reduction to audio buffer
         */
        @JvmStatic
        fun applyNoiseReduction(buffer: ByteArray, size: Int): ByteArray {
            if (size <= 0) return buffer
            
            // Convert bytes to shorts (PCM16)
            val samples = ShortArray(size / 2)
            for (i in samples.indices) {
                val byteIndex = i * 2
                if (byteIndex + 1 < size) {
                    samples[i] = ((buffer[byteIndex + 1].toInt() shl 8) or (buffer[byteIndex].toInt() and 0xFF)).toShort()
                }
            }
            
            // Apply noise gate
            for (i in samples.indices) {
                if (abs(samples[i].toInt()) < NOISE_THRESHOLD) {
                    samples[i] = 0
                }
            }
            
            // Apply smoothing
            val smoothed = smoothAudio(samples)
            
            // Convert back to bytes
            val result = ByteArray(size)
            for (i in smoothed.indices) {
                val byteIndex = i * 2
                if (byteIndex + 1 < size) {
                    result[byteIndex] = (smoothed[i].toInt() and 0xFF).toByte()
                    result[byteIndex + 1] = (smoothed[i].toInt() shr 8).toByte()
                }
            }
            
            return result
        }
        
        /**
         * Smooth audio to reduce artifacts
         */
        private fun smoothAudio(samples: ShortArray): ShortArray {
            val output = ShortArray(samples.size)
            
            for (i in samples.indices) {
                recentSamples.add(samples[i])
                if (recentSamples.size > SMOOTHING_WINDOW) {
                    recentSamples.removeAt(0)
                }
                
                // Calculate average of recent samples
                val avg = recentSamples.sum() / recentSamples.size
                output[i] = avg.toShort()
            }
            
            return output
        }
        
        /**
         * Apply automatic gain control
         */
        @JvmStatic
        fun applyAutoGain(buffer: ByteArray, size: Int): ByteArray {
            if (size <= 0) return buffer
            
            // Convert bytes to shorts (PCM16)
            val samples = ShortArray(size / 2)
            for (i in samples.indices) {
                val byteIndex = i * 2
                if (byteIndex + 1 < size) {
                    samples[i] = ((buffer[byteIndex + 1].toInt() shl 8) or (buffer[byteIndex].toInt() and 0xFF)).toShort()
                }
            }
            
            // Find peak
            var peak = 0
            for (sample in samples) {
                peak = max(peak, abs(sample.toInt()))
            }
            
            // Apply gain if needed
            if (peak > 0 && peak < Short.MAX_VALUE / 2) {
                val gain = (Short.MAX_VALUE / 2).toFloat() / peak
                for (i in samples.indices) {
                    samples[i] = (samples[i] * min(gain, 2.0f)).toInt().toShort()
                }
            }
            
            // Convert back to bytes
            val result = ByteArray(size)
            for (i in samples.indices) {
                val byteIndex = i * 2
                if (byteIndex + 1 < size) {
                    result[byteIndex] = (samples[i].toInt() and 0xFF).toByte()
                    result[byteIndex + 1] = (samples[i].toInt() shr 8).toByte()
                }
            }
            
            return result
        }
        
        /**
         * Reset the enhancer state
         */
        @JvmStatic
        fun reset() {
            recentSamples.clear()
        }
    }
}
