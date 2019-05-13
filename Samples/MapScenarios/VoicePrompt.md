## Sample - Voice Prompt
====================

### Overview
- This feature will read route instruction aloud to the user as they swipe through the route instructions or as they traverse them with indoor location.

### Usage

- Need to fill out `applicationId`, `accessKey`, `signatureKey`, and `buildingId` in `strings.xml` and `integers.xml`.

### Sample Code
- [VoicePromptActivity.kt](https://github.com/phunware/maas-mapping-android-sdk/blob/sample_code_updates/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/VoicePromptActivity.kt)
- [NavigationOverlayView.kt](https://github.com/phunware/maas-mapping-android-sdk/blob/sample_code_updates/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/NavigationOverlayView.kt)
- [ManeuverDisplayHelper.kt](https://github.com/phunware/maas-mapping-android-sdk/blob/sample_code_updates/Samples/kotlin/src/main/java/com/phunware/kotlin/sample/ManeuverDisplayHelper.kt)

**Step 1: Copy the following files to your project**

- VoicePromptActivity.kt
- NavigationOverlayView.kt
- ManeuverDisplayHelper.kt

Note: For text-to-speech, we use text-to-speech API from `android.speech.tts.TextToSpeech` class

**Step 2: Pay attention to this method `onManeuverChanged`**

```
override fun onManeuverChanged(navigator: Navigator, position: Int) {
        // Update the selected floor when the maneuver floor changes
        val maneuver = navigator.maneuvers[position]
        val selectedPosition = floorSpinner.selectedItemPosition
        for (i in 0 until floorSpinnerAdapter.count) {
            val floor = floorSpinnerAdapter.getItem(i)
            if (selectedPosition != i && floor != null && floor.id == maneuver.floorId) {
                floorSpinner.setSelection(i)
            }
        }

        //if voice is enabled 
        if (voiceEnabled) {
            val pair = navOverlay.getManeuverPair()
            var text = displayHelper.stringForDirection(this, pair.mainManeuver)

            val turnManeuver  = pair.turnManeuver
            var turnable : Boolean  = false
            if(turnManeuver != null) {
                turnable = turnManeuver.isTurnManeuver
            }

            if(turnManeuver != null && position < navigator.maneuvers.size - 1 && turnable) {
                text += " then "
                text += displayHelper.stringForDirection(this, turnManeuver)
            }
            else {
                text += " to arrive at the destination."
            }
            textToVoice(text)
        }
    }
```
`pair` (`NavigationOverlayView.getManeuverPair()`) is the object that contains the current maneuver and the next turn maneuver (`val turnManeuver  = pair.turnManeuver`). Check if the `turnManeuver` is an actual turn, i.e. `turnManeuver.isTurnManeuver == true`, then add the text to speech for that turn.

`ManeuverDisplayHelper.stringForDirection()` is a helper method that receives a `String` and play the speech of that `String`     

# Privacy
You understand and consent to Phunware’s Privacy Policy located at www.phunware.com/privacy. If your use of Phunware’s software requires a Privacy Policy of your own, you also agree to include the terms of Phunware’s Privacy Policy in your Privacy Policy to your end users.

# Terms
Use of this software requires review and acceptance of our terms and conditions for developer use located at http://www.phunware.com/terms/