package com.phunware.kotlin.sample.routing

/* Copyright (C) 2018 Phunware, Inc.

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL Phunware, Inc. BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of Phunware, Inc. shall
not be used in advertising or otherwise to promote the sale, use or
other dealings in this Software without prior written authorization
from Phunware, Inc. */
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.phunware.kotlin.sample.R
import com.phunware.kotlin.sample.routing.util.ManeuverDisplayHelper
import com.phunware.mapping.manager.Navigator
import com.phunware.mapping.model.RouteOptions
import java.util.Locale

class VoicePromptActivity : RoutingActivity(), TextToSpeech.OnInitListener {

    // Voice Views
    private lateinit var voice: ImageButton
    private lateinit var voiceStatusTextView: TextView

    private var currentManeuverPosition: Int? = null

    private var voiceListener: View.OnClickListener = View.OnClickListener { toggleVoice() }

    private var tts: TextToSpeech? = null
    private val displayHelper: ManeuverDisplayHelper = ManeuverDisplayHelper()

    private lateinit var handler: Handler

    private val sharedPref by lazy {
        getSharedPreferences("lbs_sample_voice", Context.MODE_PRIVATE)
    }

    private var voiceEnabled: Boolean
        get() {
            return sharedPref.getBoolean(PREF_VOICE_ENABLED, true)
        }
        set(value) {
            sharedPref.edit().putBoolean(PREF_VOICE_ENABLED, value).apply()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Initialize Text-To-Speech
        tts = TextToSpeech(this, this)

        // Initialize views for routing
        voice = findViewById(R.id.voice)
        voice.setOnClickListener(voiceListener)
        voiceStatusTextView = findViewById(R.id.voice_status)
    }

    public override fun onStart() {
        super.onStart()
        handler = Handler()
    }

    public override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }

    public override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    /**
     * TextToSpeech.OnInitListener
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts?.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "The Language specified is not supported!")
            }
        } else {
            Log.e(TAG, "Initialization Failed!")
        }
    }

    /**
     * RoutingActivity.dispatchManeueverChanged
     *
     */
    override fun dispatchManeuverChanged(navigator: Navigator, position: Int) {
        //TODO: remove log statement before PR Merge
        Log.d("VoiceRepeatDebug", "dispatchedManeuverChanged (VoicePromptActivity) called with position: $position")

        // Play the text that is associated with the maneuver position
        if (voiceEnabled) {
            Log.d("VoiceRepeatDebug", "Voicing TTS for position: $position")
            textToVoice(getTextForPosition(navigator, position))
        }
    }

    override fun startNavigating(route: RouteOptions) {
        super.startNavigating(route)

        if (voiceEnabled) {
            voice.setImageResource(R.drawable.ic_unmuted)
            voiceStatusTextView.setText(R.string.demo_voice_prompt_unmuted)
        } else {
            voice.setImageResource(R.drawable.ic_muted)
            voiceStatusTextView.setText(R.string.demo_voice_prompt_muted)
        }

        voice.visibility = View.VISIBLE
        voiceStatusTextView.visibility = View.VISIBLE
    }

    override fun stopNavigating() {
        super.stopNavigating()
        voice.visibility = View.GONE
        voiceStatusTextView.visibility = View.GONE
    }

    private fun toggleVoice() {
        voiceEnabled = !voiceEnabled
        if (voiceEnabled) {
            voice.setImageResource(R.drawable.ic_unmuted)
            voiceStatusTextView.setText(R.string.demo_voice_prompt_unmuted)
            navigator?.let { navigator ->
                currentManeuverPosition?.apply {
                    textToVoice(getTextForPosition(navigator, this))
                }
            }
        } else {
            voice.setImageResource(R.drawable.ic_muted)
            voiceStatusTextView.setText(R.string.demo_voice_prompt_muted)
            tts?.stop()
        }
    }

    private fun getTextForPosition(navigator: Navigator, position: Int): String {
        val pair = navOverlay.getManeuverPair()
        var text = displayHelper.stringForDirection(this, pair.mainManeuver)
        text += " ${displayHelper.distanceForDirection(this, pair.mainManeuver,
                getString(R.string.demo_voice_prompt_prep_for))}"


        val turnManeuver = pair.turnManeuver
        var turnable = false
        if (turnManeuver != null) {
            turnable = turnManeuver.isTurnManeuver
        }

        if (turnManeuver != null && position < navigator.maneuvers.size - 1 && turnable) {
            text += " ${getString(R.string.demo_voice_prompt_then)} "
            text += displayHelper.stringForDirection(this, turnManeuver)
        } else {
            text += " ${getString(R.string.demo_voice_prompt_arrive_at_destination)}"
        }

        return text
    }

    private fun textToVoice(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    companion object {
        private val TAG = VoicePromptActivity::class.java.simpleName
        private const val PREF_VOICE_ENABLED = "pref_voice"
    }
}