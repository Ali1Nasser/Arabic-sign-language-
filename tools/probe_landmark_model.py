"""Audit the optional Hugging Face TFLite model without deserializing untrusted pickle."""
import hashlib,json,pathlib,pickletools,urllib.request
import numpy as np,tensorflow as tf
name="katyy2000/arabic-sign-language-recognition"
def get(url,cap):
 with urllib.request.urlopen(urllib.request.Request(url,headers={"User-Agent":"ArabicSignFusion/0.5"}),timeout=120) as h: b=h.read(cap+1)
 assert len(b)<=cap,(url,len(b))
 return b
info=json.loads(get(f"https://huggingface.co/api/models/{name}",300000))
sha=info["sha"]
assert sha.startswith("dc7db37"),"HF revision changed; verify and pin new commit"
siblings={x["rfilename"]:x for x in info["siblings"]}
p=pathlib.Path("model-probe");p.mkdir(exist_ok=True)
for filename,cap in [("encoder.pkl",10000),("asl_mediapipe_new_version.tflite",400000)]:
 b=get(f"https://huggingface.co/{name}/resolve/{sha}/{filename}",cap)
 digest=hashlib.sha256(b).hexdigest()
 expected=siblings[filename].get("lfs",{}).get("sha256")
 if expected: assert digest==expected,(filename,digest,expected)
 (p/filename).write_bytes(b);print("FILE",filename,"REV",sha,"BYTES",len(b),"SHA256",digest,flush=True)
print("ENCODER OPCODES (no pickle execution)",flush=True)
for op,arg,pos in pickletools.genops((p/"encoder.pkl").read_bytes()):
 if op.name in ("GLOBAL","STACK_GLOBAL","REDUCE","BUILD","NEWOBJ","SHORT_BINUNICODE","BINUNICODE","SHORT_BINBYTES","BINBYTES"):
  v=repr(arg) if not isinstance(arg,bytes) else repr(arg[:256])
  print(pos,op.name,v,flush=True)
interp=tf.lite.Interpreter(model_path=str(p/"asl_mediapipe_new_version.tflite"));interp.allocate_tensors()
a=interp.get_input_details()[0];b=interp.get_output_details()[0]
print("TENSOR",a["shape"].tolist(),str(a["dtype"]),b["shape"].tolist(),str(b["dtype"]),flush=True)
assert int(np.prod(a["shape"]))==63 and a["dtype"]==np.float32 and b["dtype"]==np.float32
interp.set_tensor(a["index"],np.zeros(a["shape"],dtype=np.float32));interp.invoke()
z=interp.get_tensor(b["index"]);assert np.isfinite(z).all()
print("MODEL VALIDATED",list(z.shape),flush=True)
