## Sample - Voice Prompt
====================

### Overview
- This feature will read route instruction aloud to the user as they swipe through the route instructions or as they traverse them with indoor location.

### Usage

- Need to fill out `applicationId`, `accessKey`, `signatureKey`, and `buildingId` in `strings.xml` and `integers.xml`.

### Sample Code
- [VoicePromptActivity.kt](kotlin/src/main/java/com/phunware/kotlin/sample/routing/VoicePromptActivity.kt)
- [NavigationOverlayView.kt](kotlin/src/main/java/com/phunware/kotlin/sample/routing/view/NavigationOverlayView.kt)
- [ManeuverDisplayHelper.kt](kotlin/src/main/java/com/phunware/kotlin/sample/routing/util/ManeuverDisplayHelper.kt)

**Step 1: Copy the following files to your project**

- VoicePromptActivity.kt
- NavigationOverlayView.kt
- ManeuverDisplayHelper.kt

Note: For text-to-speech, we use text-to-speech API from `android.speech.tts.TextToSpeech` class

**Step 2: Pay attention to this method `onManeuverChanged`**

```
    /**
     * Navigator.OnManeuverChangedListener
     *
     * Updates the current maneuver index.
     * Updates the selected floor when the maneuver floor changes.
     * Plays the text that is associated with the maneuver position
     *
     */
    override fun onManeuverChanged(navigator: Navigator, position: Int) {
        super.onManeuverChanged(navigator, position)

        this.currentManeuverPosition = position

        // Play the text that is associated with the maneuver position
        if (voiceEnabled) {
            textToVoice(getTextForPosition(navigator, position))
        }
    }
```

**Step 3: Play the voice instructions on each maneuver change by calling `textToVoice(getTextForPosition(navigator, position))`**

`pair` (`NavigationOverlayView.getManeuverPair()`) is the object that contains the current maneuver and the next turn maneuver (`val turnManeuver  = pair.turnManeuver`). Check if the `turnManeuver` is an actual turn, i.e. `turnManeuver.isTurnManeuver == true`, then add the text to speech for that turn.

`ManeuverDisplayHelper.stringForDirection()` is a helper method that receives a `String` that provides navigation direction that is ready to be played.

`ManeueverDisplyHelper.distanceForDirection()` is a helper method that builds a `String` that provides the distance for the supplied maneuever.

# Privacy
You understand and consent to Phunware’s Privacy Policy located at www.phunware.com/privacy. If your use of Phunware’s software requires a Privacy Policy of your own, you also agree to include the terms of Phunware’s Privacy Policy in your Privacy Policy to your end users.

# Terms
Use of this software requires review and acceptance of our terms and conditions for developer use located at http://www.phunware.com/terms/