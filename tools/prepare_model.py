"""Pinned public GitHub model assets; verify Git blob SHA before converting to TFLite."""
from pathlib import Path
import hashlib, urllib.request
import numpy as np
import tensorflow as tf
COMMIT='dce7693a8c6abe831d5347ca0326c4615f767862'
REPO='pavlyhalim/Arabic-Sign-Language'
FILES={'models/asl_model.h5':('0015a72a231a6a454e69e17a721ca2f58525e2dd',5231792),'models/hand_landmarker.task':('0d53faf3786146e95c5bc010c48029ff16b7fa59',7819105)}
root=Path('app/src/main/assets');root.mkdir(parents=True,exist_ok=True)
def blob_sha(v): return hashlib.sha1(b'blob '+str(len(v)).encode()+bytes([0])+v).hexdigest()
for path,(expected,size) in FILES.items():
    u=f'https://raw.githubusercontent.com/{REPO}/{COMMIT}/{path}'
    with urllib.request.urlopen(urllib.request.Request(u,headers={'User-Agent':'ArabicSignFusionCI'}),timeout=120) as r: b=r.read(size+1)
    if len(b)!=size or blob_sha(b)!=expected: raise RuntimeError('Model integrity failure: '+path)
    (root/Path(path).name).write_bytes(b)
    print('verified',path,expected)
model=tf.keras.models.load_model(str(root/'asl_model.h5'),compile=False)
assert tuple(model.input_shape[1:])==(64,64,3),model.input_shape
assert model.output_shape[-1]==32,model.output_shape
converted=tf.lite.TFLiteConverter.from_keras_model(model).convert()
t=tf.lite.Interpreter(model_content=converted);t.allocate_tensors();ins=t.get_input_details()[0];outs=t.get_output_details()[0]
assert ins['shape'].tolist()==[1,64,64,3] and ins['dtype']==np.float32
assert outs['shape'].tolist()==[1,32] and outs['dtype']==np.float32
t.set_tensor(ins['index'],np.zeros((1,64,64,3),dtype=np.float32));t.invoke();z=t.get_tensor(outs['index'])
assert z.shape==(1,32) and np.isfinite(z).all()
(root/'github_alphabet.tflite').write_bytes(converted);(root/'asl_model.h5').unlink()
print('TFLite validation OK',len(converted))
