# v0.7 Motion Video Design & Audit

- Six independent procedural moving-vector animations, 720x480 pixels at 24fps for 3.6 seconds, looped in an Android VideoView, NOT PNG sequences or a static slideshow.
- Slugs: ezayak, tamam, ayez, shokran, mayya, yalla. Draws continuous arm and hand motion interpolated from key poses and marks motion direction from frame-to-frame displacement.
- Build tool `tools/generate_motion_videos.py` requires Pillow and ffmpeg on GitHub runner; outputs real H.264 with yuv420p, +faststart. Automated tests require two different frames for every phrase and >=80 encoded frames verified by ffprobe.
- Android controls: six phrase buttons, play/pause, restart, speed 0.5/1/1.5, loop and scrub timeline via MediaController. All videos included offline. Existing two ML recognizers and personal sign-video library unchanged.
- CRITICAL CONTENT STATUS: illustrative design hypotheses based on previously generated graphics, NOT verified Egyptian Sign Language handshape, palm orientation, facial expressions, semantics or grammar. Visual dictionary clearly discloses this. Human review and consented source recordings required before educational/canonical use.
- Later authoritative generation should be retargeted to a licensed, reviewed motion dataset (21/33/face landmarks + metadata), avoiding AI hallucinated lexical signs.
