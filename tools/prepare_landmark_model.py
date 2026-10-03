"""Fetch and verify pinned MIT landmark engine; inspect encoder pickle without executing it."""
import hashlib,pickletools,urllib.request
from pathlib import Path
import numpy as np,tensorflow as tf
REPO="katyy2000/arabic-sign-language-recognition"
REV="dc7db37c218a6172f832eaf5eb890fbe7ec8e479"
FILES={"encoder.pkl":("88ee7638cfb47bcf7ca2e4d7fde226fac66789662ee7884dca1d2018c2cc08af",492),"asl_mediapipe_new_version.tflite":("64c468abbc3a6c9bf8a5faf6ece4daed2cedd8a9ebd5135ddc3ec9d7b28e1f7a",41472)}
EXPECTED=["0","1","10","2","3","4","5","6","7","8","9","ain","al","aleff","bb","dal","dha","dhad","fa","gaaf","ghain","ha","haa","jeem","kaaf","khaa","laam","meem","nun","ra","saad","seen","sheen","space","ta","taa","thaa","thal","toot","waw","ya","yaa","zay"]
assets=Path("app/src/main/assets");assets.mkdir(parents=True,exist_ok=True)
def download(filename):
 digest,size=FILES[filename]
 url=f"https://huggingface.co/{REPO}/resolve/{REV}/{filename}"
 with urllib.request.urlopen(urllib.request.Request(url,headers={"User-Agent":"ArabicSignFusion-v0.5"}),timeout=120) as f: b=f.read(size+1)
 assert len(b)==size and hashlib.sha256(b).hexdigest()==digest,filename+" changed unexpectedly"
 return b
encoder=download("encoder.pkl")
unistr=[arg for op,arg,pos in pickletools.genops(encoder) if op.name=="SHORT_BINUNICODE"]
start=unistr.index("0")
assert unistr[start:start+43]==EXPECTED,"Unverified output class order"
blob=download("asl_mediapipe_new_version.tflite")
(assets/"hf_landmarks.tflite").write_bytes(blob)
t=tf.lite.Interpreter(model_content=blob);t.allocate_tensors()
i=t.get_input_details()[0];o=t.get_output_details()[0]
assert i["shape"].tolist()==[1,63] and i["dtype"]==np.float32
assert o["shape"].tolist()==[1,43] and o["dtype"]==np.float32
fixture=np.zeros((1,63),dtype=np.float32);fixture[0,0]=.2;fixture[0,1]=.3
t.set_tensor(i["index"],fixture);t.invoke();z=t.get_tensor(o["index"])
assert z.shape==(1,43) and np.isfinite(z).all()
print("HF model, exact label order and TFLite fixture PASS",len(blob),"bytes",flush=True)
