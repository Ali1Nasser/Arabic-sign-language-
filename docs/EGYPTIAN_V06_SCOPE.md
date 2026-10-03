# Egyptian Sign Conversation v0.6: capability and licensing audit

Inspired by Rylo's separate text/audio vs sign-language paths, without copying proprietary or CC BY-NC-SA code. This release does **not** offer automatic continuous Egyptian Sign Language translation.

Functional features:
1. Keep existing offline RGB 32-class and experimental landmark 43-class alphabet engines; pass their transcript to a separate screen with explicit status.
2. Egyptian Arabic text entry + contextual common replies (not automatically generated signs).
3. Android speech recognizer requested locale ar-EG, graceful unavailability handling; Android TTS locale ar-EG with missing-voice handling. These capabilities depend on device software; speech data may be processed by its provider.
4. Local-only conversation history and private app-storage sign video clips selected/recorded by consenting users. No video upload permissions/service/network used by the app. Phrase matching is exact after narrow Arabic Unicode normalizations; unknown phrases display *no matching video*.
5. Delete individual clips or clear transcript; 80 MB clip import limit; ensure stored clip path resolves only to app-private files.
6. Main Activity can open conversation without destroying camera recognition engine.
7. Pure-Java smoke tests for phrase normalization, empty/mismatched phrase rejection and preservation of semantics.
Research: Métwalli (2026) Egyptian Sign Language, CC BY 4.0 (Mendeley DOI 10.17632/39tbt2jd7r.3). Not bundled. No known rights to redistribute Petersamy18 Egyptian DynamicModel.h5; do not bundle. Benchmark and user testing by fluent Egyptian signers required before deploying any Egyptian recognizer.
