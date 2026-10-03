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
