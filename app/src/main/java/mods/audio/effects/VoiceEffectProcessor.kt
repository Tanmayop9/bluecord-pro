package mods.audio.effects

import mods.audio.converters.AudioConstants
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.PI
import kotlin.math.sin

/**
 * Processes PCM16 audio data and applies various voice effects
 */
class VoiceEffectProcessor {
    
    companion object {
        private const val TAG = "VoiceEffectProcessor"
        private val echoBuffer = mutableListOf<Short>()
        private val reverbBuffer = Array(3) { ShortArray(0) }
        private var reverbIndex = 0
        
        /**
         * Apply the selected voice effect to PCM16 audio data
         * @param buffer The audio buffer (PCM16 format)
         * @param size Number of valid bytes in buffer
         * @param effect The voice effect to apply
         * @return Modified buffer with effect applied
         */
        @JvmStatic
        fun applyEffect(buffer: ByteArray, size: Int, effect: VoiceEffect): ByteArray {
            if (effect == VoiceEffect.NONE || size <= 0) {
                return buffer
            }
            
            // Convert bytes to shorts (PCM16)
            val samples = ShortArray(size / 2)
            for (i in samples.indices) {
                val byteIndex = i * 2
                if (byteIndex + 1 < size) {
                    samples[i] = ((buffer[byteIndex + 1].toInt() shl 8) or (buffer[byteIndex].toInt() and 0xFF)).toShort()
                }
            }
            
            // Apply the effect
            val processed = when (effect) {
                VoiceEffect.PITCH_HIGH -> pitchShift(samples, 1.5f)
                VoiceEffect.PITCH_LOW -> pitchShift(samples, 0.7f)
                VoiceEffect.ROBOT -> robotEffect(samples)
                VoiceEffect.ECHO -> echoEffect(samples)
                VoiceEffect.REVERB -> reverbEffect(samples)
                VoiceEffect.ALIEN -> alienEffect(samples)
                VoiceEffect.RADIO -> radioEffect(samples)
                VoiceEffect.TELEPHONE -> telephoneEffect(samples)
                else -> samples
            }
            
            // Convert back to bytes
            val result = ByteArray(size)
            for (i in processed.indices) {
                val byteIndex = i * 2
                if (byteIndex + 1 < size) {
                    result[byteIndex] = (processed[i].toInt() and 0xFF).toByte()
                    result[byteIndex + 1] = (processed[i].toInt() shr 8).toByte()
                }
            }
            
            return result
        }
        
        /**
         * Simple pitch shifting using sample rate conversion
         */
        private fun pitchShift(samples: ShortArray, factor: Float): ShortArray {
            val outputSize = (samples.size / factor).toInt()
            val output = ShortArray(samples.size)
            
            for (i in output.indices) {
                val srcIndex = (i * factor).toInt()
                if (srcIndex < samples.size) {
                    output[i] = samples[srcIndex]
                } else {
                    output[i] = 0
                }
            }
            
            return output
        }
        
        /**
         * Robot/vocoder effect using ring modulation
         */
        private fun robotEffect(samples: ShortArray): ShortArray {
            val output = ShortArray(samples.size)
            val modulationFreq = 30.0 // Hz
            val sampleRate = AudioConstants.SAMPLE_RATE.toDouble()
            
            for (i in samples.indices) {
                val modulator = cos(2.0 * PI * modulationFreq * i / sampleRate)
                output[i] = (samples[i] * modulator * 0.7).toInt().toShort()
            }
            
            return output
        }
        
        /**
         * Echo effect with delay
         */
        private fun echoEffect(samples: ShortArray): ShortArray {
            val output = ShortArray(samples.size)
            val delayInSamples = AudioConstants.SAMPLE_RATE / 3 // ~333ms delay
            val decayFactor = 0.5f
            
            // Initialize echo buffer if needed
            if (echoBuffer.isEmpty()) {
                repeat(delayInSamples) { echoBuffer.add(0) }
            }
            
            for (i in samples.indices) {
                val echoSample = if (echoBuffer.size > 0) echoBuffer[0] else 0
                val mixed = (samples[i] + echoSample * decayFactor).toInt()
                output[i] = clamp(mixed).toShort()
                
                // Update echo buffer
                if (echoBuffer.size > 0) {
                    echoBuffer.removeAt(0)
                }
                echoBuffer.add(output[i])
                
                // Maintain buffer size
                while (echoBuffer.size > delayInSamples) {
                    echoBuffer.removeAt(0)
                }
            }
            
            return output
        }
        
        /**
         * Reverb effect using multiple delays
         */
        private fun reverbEffect(samples: ShortArray): ShortArray {
            val output = ShortArray(samples.size)
            val delays = intArrayOf(
                AudioConstants.SAMPLE_RATE / 20,  // 50ms
                AudioConstants.SAMPLE_RATE / 15,  // 67ms
                AudioConstants.SAMPLE_RATE / 10   // 100ms
            )
            val decays = floatArrayOf(0.3f, 0.25f, 0.2f)
            
            // Initialize reverb buffers
            for (i in reverbBuffer.indices) {
                if (reverbBuffer[i].isEmpty()) {
                    reverbBuffer[i] = ShortArray(delays[i])
                }
            }
            
            for (i in samples.indices) {
                var mixed = samples[i].toInt()
                
                // Add delayed signals
                for (j in delays.indices) {
                    if (reverbBuffer[j].isNotEmpty()) {
                        val delayedSample = reverbBuffer[j][reverbIndex % delays[j]]
                        mixed += (delayedSample * decays[j]).toInt()
                    }
                }
                
                output[i] = clamp(mixed).toShort()
                
                // Update buffers
                for (j in delays.indices) {
                    if (reverbBuffer[j].isNotEmpty()) {
                        reverbBuffer[j][reverbIndex % delays[j]] = output[i]
                    }
                }
                
                reverbIndex++
            }
            
            return output
        }
        
        /**
         * Alien effect using frequency modulation
         */
        private fun alienEffect(samples: ShortArray): ShortArray {
            val output = ShortArray(samples.size)
            val modulationFreq = 5.0 // Hz
            val modulationDepth = 0.3
            val sampleRate = AudioConstants.SAMPLE_RATE.toDouble()
            
            for (i in samples.indices) {
                val modulation = 1.0 + modulationDepth * sin(2.0 * PI * modulationFreq * i / sampleRate)
                val tremolo = cos(2.0 * PI * 15.0 * i / sampleRate) * 0.3 + 0.7
                output[i] = (samples[i] * modulation * tremolo).toInt().toShort()
            }
            
            return output
        }
        
        /**
         * Radio effect with bandpass filtering and noise
         */
        private fun radioEffect(samples: ShortArray): ShortArray {
            val output = ShortArray(samples.size)
            
            for (i in samples.indices) {
                // Simple bandpass simulation
                val compressed = samples[i] * 0.6
                val distorted = if (abs(compressed) > 8000) {
                    compressed * 0.8
                } else {
                    compressed
                }
                
                output[i] = distorted.toInt().toShort()
            }
            
            return output
        }
        
        /**
         * Telephone effect with limited frequency range
         */
        private fun telephoneEffect(samples: ShortArray): ShortArray {
            val output = ShortArray(samples.size)
            
            for (i in samples.indices) {
                // Simulate telephone bandwidth limitation
                var sample = samples[i] * 0.5
                
                // Add slight distortion
                if (abs(sample) > 5000) {
                    sample *= 0.7
                }
                
                output[i] = sample.toInt().toShort()
            }
            
            return output
        }
        
        /**
         * Clamp value to 16-bit signed range
         */
        private fun clamp(value: Int): Int {
            return when {
                value > Short.MAX_VALUE -> Short.MAX_VALUE.toInt()
                value < Short.MIN_VALUE -> Short.MIN_VALUE.toInt()
                else -> value
            }
        }
        
        /**
         * Reset all effect buffers (call when starting a new recording)
         */
        @JvmStatic
        fun reset() {
            echoBuffer.clear()
            reverbIndex = 0
            for (i in reverbBuffer.indices) {
                reverbBuffer[i] = ShortArray(0)
            }
        }
    }
}
