# Arabic Sign Fusion · Android

The v0.4 unified integration branch creates an Android APK directly in GitHub Actions using a verifiable pretrained 32-class Arabic alphabet CNN from pavlyhalim/Arabic-Sign-Language (GPL-3.0). Features: live front-camera hand tracking, stable single-event recognition, editable and saved Arabic transcript, undo, space, copy, share and Arabic speech. The upload of v0.3 APK and its ZIP were byte-identical to the previously built v0.3 GitHub artifact; no hidden second trained engine could be extracted from those files.

The requested Esm3ny (Apache-2.0) and Assem sign-to-text (MIT) source scripts are preserved under upstream/, but their original weights (`best_final_2.onnx`, `conv1_lstm.keras`) were not published. They are NOT falsely advertised as active engines in this APK. This Android release is alphabet recognition, NOT sentence-level translation. The independent 32-class CNN additionally has several class-to-character aliases.

To build: GitHub Actions → **Android APK – pretrained Arabic alphabet**. On the `feature/arabic-sign-fusion` branch, a push automatically runs CI. Successful build artifact: **ArabicSignFusion-debug-APK** containing `app-debug.apk` (Android 8+). The build downloads pinned GitHub weights, verifies byte lengths and Git blob checksums, converts CNN to TFLite and validates a test inference before packaging.

Licensing: the combined mobile project is GPL-3.0 due to CNN source/model integration; see LICENSES/. Upstream authors are separately credited in source and licenses. Network only required to build; alphabet recognition runs on device. Not clinically validated, not a replacement for human interpreting.

## v0.5 stability changes (candidate)

- Merge duplicate softmax output classes sharing one Arabic glyph before ranking predictions (e.g. a repeated handwritten character label).
- Reject uncertain frames using heuristic aggregate confidence >=0.66 and winner margin >=0.13. These are not calibrated probability estimates; evaluate on unseen signers.
- Emit only after 3 consecutive confident predictions, prevent held-sign duplication, require two explicitly absent-hand sampled frames to re-arm. Low-confidence frames do not re-arm. Manual repeat button for double letters.
- Keep isolated letter recognition independent of absent words/sentence weights; experimental Hugging Face 63-landmark model is audited separately before enabling it.
- On-device inference and privacy unchanged. Note new version's debug signing may differ from a prior local APK.

## Experimental 43-class HF engine (v0.5)

Optional second offline hand-landmark TFLite mode; explicitly switch in app. HF repo katyy2000/arabic-sign-language-recognition MIT, pinned revision dc7db37c218a6172f832eaf5eb890fbe7ec8e479. CI checks the exact 43-label order from the 492-byte encoder without executing pickle, SHA256 verifies both assets, and validates [1,63] → [1,43] tensors. No ensemble, accuracy gain, training-time mirroring parity, or unseen-signer generalization claimed. Original RGB 32-class engine remains the default.

## v0.6 Egyptian conversation experience (Rylo-inspired, no Rylo code copied)
The original two offline Arabic alphabet engines remain intact. New native EgyptianConversationActivity provides:
- Egyptian Arabic typed conversation, common quick replies, Android speech recognition via RecognizerIntent with ar-EG request (availability/network varies by device), and Android ar-EG text-to-speech (requires an installed supporting voice);
- locally saved dialogue and user-curated **Egyptian Sign Language** video library, with optional import/record, playback, exact normalized phrase matching, deletion, and 80 MB file-size cap;
- existing recognized fingerspelling sent to conversation with explicit **alphabet-only/non-Egyptian-model** disclosure.
No universal Egyptian sign-to-text model is claimed or included. Text-to-sign only displays user-provided recordings explicitly mapped to the requested phrase. App retains GPL-3.0 obligations. No Rylo app source, model or branding is copied. Rylo Sign Translate is CC BY-NC-SA for free-use tiers / separate commercial license: https://rylo.com/sign/translate/legal/terms/
Egyptian sign research/dataset candidate (not bundled): Métwalli (2026), 55 classes / 400+ clips, CC BY 4.0: https://data.mendeley.com/datasets/39tbt2jd7r
Petersamy18 DynamicModel.h5 is unlicensed in public GitHub; do not distribute or claim it as deployed without explicit rights and independent model validation.

## v0.7 — genuine video-based Stickman motion dictionary

An independent native Android EgyptianMotionActivity is linked from Home and Egyptian Conversation. Six 720×480, 24 fps H.264 MP4 videos are created at build time using a procedural, continuously interpolated stickman body/arm rig with actual geometrical position changes, not downloaded photos, still-image carousels, or sprite swaps. Videos are included offline in Android res/raw. Player uses looping VideoView with selectable clips, pause/play, restart, .5/1/1.5× speeds and MediaController timeline. CI uses ffprobe to check actual H.264 frame counts and checks distinct poses. Videos also exported as standalone GitHub artifact. **These are visually illustrative gestures developed from conceptual AI-generated storyboards, NOT linguistically validated Egyptian Sign Language translations.** Never use them as authoritative sign-language teaching content until Egyptian Deaf-community/sign-language expert review, with handshape, orientation, motion and facial features validated. No Rylo code, branding, model or video reused.
