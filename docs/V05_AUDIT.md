# v0.5 evidence-driven integration decision

**Baseline**: GPL-3.0 pavlyhalim RGB CNN, original 32 output classes; multiple classes map to same glyph. Summation yields a glyph-level distribution before testing margin.

**Second candidate**: katyy2000/arabic-sign-language-recognition (MIT), 63 MediaPipe hand XYZ values, TFLite + encoder.pkl. Probe its shape and ordered labels without unpickling. Its model-card examples do not fully document training coordinate preprocessing or signer-independent test performance. Hence do not ensemble, weight, or represent this as validated until an exact numeric parity fixture and held-out test exist.

**No unsupported claim**: v0.5 changes decoding and repetition behaviour, not the trained RGB CNN's measured test accuracy. Word/sentence models remain out of scope without matched assets and dialect-specific evaluation.

**Acceptance checks**: stable 3 frames, one-frame tracking dropout cannot rearm, two missing-hand observations rearm, low-confidence/ambiguous cannot rearm, duplicate glyph scores aggregate, nonfinite rejected, GitHub CI builds APK and checks model conversion.

Optional HF model: input [1,63], output [1,43]; pinned encoder SHA256 88ee7638cfb47bcf7ca2e4d7fde226fac66789662ee7884dca1d2018c2cc08af, TFLite SHA256 64c468abbc3a6c9bf8a5faf6ece4daed2cedd8a9ebd5135ddc3ec9d7b28e1f7a; labels order verified via safe pickle opcode inspection. Experimental on-device mode only; do not mix unmatched confidence scores or dialects before validated benchmarking.
