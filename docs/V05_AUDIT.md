# v0.5 evidence-driven integration decision

**Baseline**: GPL-3.0 pavlyhalim RGB CNN, original 32 output classes; multiple classes map to same glyph. Summation yields a glyph-level distribution before testing margin.

**Second candidate**: katyy2000/arabic-sign-language-recognition (MIT), 63 MediaPipe hand XYZ values, TFLite + encoder.pkl. Probe its shape and ordered labels without unpickling. Its model-card examples do not fully document training coordinate preprocessing or signer-independent test performance. Hence do not ensemble, weight, or represent this as validated until an exact numeric parity fixture and held-out test exist.

**No unsupported claim**: v0.5 changes decoding and repetition behaviour, not the trained RGB CNN's measured test accuracy. Word/sentence models remain out of scope without matched assets and dialect-specific evaluation.

**Acceptance checks**: stable 3 frames, one-frame tracking dropout cannot rearm, two missing-hand observations rearm, low-confidence/ambiguous cannot rearm, duplicate glyph scores aggregate, nonfinite rejected, GitHub CI builds APK and checks model conversion.
