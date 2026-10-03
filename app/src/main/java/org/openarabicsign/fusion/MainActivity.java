package org.openarabicsign.fusion;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;
import org.tensorflow.lite.Interpreter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends ComponentActivity {
  private static final String[] LABELS={"ع","ال","أ","ب","د","ط","ض","ف","ج","غ","ه","ه","ج","ك","خ","لا","ل","م","ن","ر","ص","س","ش","ت","ط","ث","ذ","ت","و","ى","ي","ز"};
  private final ExecutorService worker=Executors.newSingleThreadExecutor();
  private final PredictionGate gate=new PredictionGate();
  private Interpreter model;
  private HandLandmarker handTracker;
  private ProcessCameraProvider provider;
  private PreviewView preview;
  private TextView message,recognized,transcript;
  private String sentence="",lastSign="";
  private int streak=0, cameraFacing=CameraSelector.LENS_FACING_FRONT;
  private long lastInference=0,lastCommit=0;
  private boolean alive=true;
  private TextToSpeech speaker;

  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    setUpLayout();
    sentence=getPreferences(MODE_PRIVATE).getString("sentence","");
    updateTranscript();
    speaker=new TextToSpeech(this, result->{if(result==TextToSpeech.SUCCESS && speaker!=null) speaker.setLanguage(new Locale("ar","EG"));});
    worker.execute(()->{
      try {
        byte[] bytes=readAsset("github_alphabet.tflite");
        ByteBuffer direct=ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
        direct.put(bytes);direct.rewind();
        model=new Interpreter(direct,new Interpreter.Options().setNumThreads(2));
        int[] a=model.getInputTensor(0).shape(),b=model.getOutputTensor(0).shape();
        if(a.length!=4||a[0]!=1||a[1]!=64||a[2]!=64||a[3]!=3||b.length!=2||b[0]!=1||b[1]!=32)
          throw new IllegalStateException("Incompatible trained CNN model.");
        BaseOptions base=BaseOptions.builder().setModelAssetPath("hand_landmarker.task").build();
        HandLandmarker.HandLandmarkerOptions opts=HandLandmarker.HandLandmarkerOptions.builder()
          .setBaseOptions(base).setRunningMode(RunningMode.IMAGE).setNumHands(1)
          .setMinHandDetectionConfidence(.5f).setMinHandPresenceConfidence(.5f).setMinTrackingConfidence(.5f).build();
        handTracker=HandLandmarker.createFromOptions(this,opts);
        tell("نموذج الحروف جاهز • ٣٢ تصنيفاً • يعمل بدون إنترنت");
      } catch(Exception e){tell("تعذر تحميل النموذج: "+e.getMessage());}
    });
    if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) openCamera();
    else ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},21);
  }

  private int dp(int a){return Math.round(a*getResources().getDisplayMetrics().density);}
  private TextView label(String s,int size,int color) {
    TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);
    t.setGravity(Gravity.CENTER);t.setPadding(dp(8),dp(7),dp(8),dp(7));return t;
  }
  private Button button(String title,Runnable action) {
    Button b=new Button(this);b.setAllCaps(false);b.setText(title);b.setTextSize(14);b.setOnClickListener(v->action.run());return b;
  }
  private void setUpLayout(){
    LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.rgb(9,19,32));root.setLayoutDirection(1);
    setContentView(root);
    root.addView(label("إسمعني | Arabic Sign Fusion",22,Color.rgb(67,224,196)));
    message=label("جارٍ تجهيز نموذج GitHub ...",13,Color.WHITE);root.addView(message);
    preview=new PreviewView(this);preview.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
    root.addView(preview,new LinearLayout.LayoutParams(-1,0,1f));
    recognized=label("اعرض إشارة حرف واحدة أمام الكاميرا",17,Color.rgb(67,224,196));root.addView(recognized);
    ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,dp(250)));
    LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);scroll.addView(content);
    transcript=label("",22,Color.WHITE);transcript.setMinHeight(dp(60));transcript.setTextIsSelectable(true);content.addView(transcript);
    LinearLayout buttons=new LinearLayout(this);
    buttons.addView(button("تراجع",()->{if(!sentence.isEmpty()){int cut=sentence.offsetByCodePoints(sentence.length(),-1);sentence=sentence.substring(0,cut);updateTranscript();}}),new LinearLayout.LayoutParams(0,dp(50),1));
    buttons.addView(button("مسح",()->new AlertDialog.Builder(this).setTitle("مسح النص؟").setMessage("سيُحذف النص المحفوظ بالكامل.").setNegativeButton("إلغاء",null).setPositiveButton("مسح",(d,w)->{sentence="";gate.reset();updateTranscript();}).show()),new LinearLayout.LayoutParams(0,dp(50),1));
    buttons.addView(button("مسافة",()->{sentence+=" ";updateTranscript();}),new LinearLayout.LayoutParams(0,dp(50),1));
    content.addView(buttons);
    LinearLayout tools=new LinearLayout(this);
    tools.addView(button("استماع 🔊",()->{if(speaker!=null&&!sentence.isEmpty())speaker.speak(sentence,TextToSpeech.QUEUE_FLUSH,null,"arabic");}),new LinearLayout.LayoutParams(0,dp(50),1));
    tools.addView(button("نسخ",()->{((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("ArabicSignFusion",sentence));Toast.makeText(this,"تم النسخ",Toast.LENGTH_SHORT).show();}),new LinearLayout.LayoutParams(0,dp(50),1));
    tools.addView(button("تبديل الكاميرا",()->{cameraFacing=cameraFacing==CameraSelector.LENS_FACING_FRONT?CameraSelector.LENS_FACING_BACK:CameraSelector.LENS_FACING_FRONT;gate.reset();bindCamera();}),new LinearLayout.LayoutParams(0,dp(50),1));
    content.addView(tools);
    LinearLayout extra=new LinearLayout(this);
    extra.addView(button("تصحيح / إضافة",this::editTranscript),new LinearLayout.LayoutParams(0,dp(50),1));
    extra.addView(button("مشاركة",()->{Intent intent=new Intent(Intent.ACTION_SEND);intent.setType("text/plain");intent.putExtra(Intent.EXTRA_TEXT,sentence);startActivity(Intent.createChooser(intent,"مشاركة النص العربي"));}),new LinearLayout.LayoutParams(0,dp(50),1));
    extra.addView(button("الأوضاع",()->new AlertDialog.Builder(this).setTitle("Recognition modes").setMessage("النموذج المدمج: ٣٢ تصنيفاً للحروف العربية يعمل دون إنترنت.\n\nEsm3ny ONNX: النموذج الأصلي best_final_2.onnx غير منشور؛ مرجعه محفوظ في المشروع.\n\nوضع الكلمات ٨٩ تصنيفاً: يتطلب conv1_lstm.keras غير المنشور، لذلك ليس مفعّلاً في APK.").setPositiveButton("موافق",null).show()),new LinearLayout.LayoutParams(0,dp(50),1));
    content.addView(extra);
    content.addView(label("تعرف تجريبي على الحروف المنفردة فقط. كَوِّن النص حرفاً بحرف؛ ليس مترجماً طبياً أو فورياً للجمل.",12,Color.LTGRAY));
  }
  private void editTranscript(){
    EditText field=new EditText(this);field.setSingleLine(false);field.setText(sentence);field.setSelectAllOnFocus(false);field.setHint("اكتب أو صحح النص هنا");
    new AlertDialog.Builder(this).setTitle("تعديل النص الناتج").setView(field).setNegativeButton("إلغاء",null).setPositiveButton("حفظ",(d,w)->{sentence=field.getText().toString();updateTranscript();}).show();
  }
  private void tell(String s){runOnUiThread(()->{if(alive)message.setText(s);});}
  private void updateTranscript(){if(transcript!=null)transcript.setText(sentence.isEmpty()?"—":sentence);getPreferences(MODE_PRIVATE).edit().putString("sentence",sentence).apply();}
  private byte[] readAsset(String path)throws Exception{
    try(InputStream in=getAssets().open(path);ByteArrayOutputStream out=new ByteArrayOutputStream()){
      byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toByteArray();
    }
  }
  private void openCamera() {
    ListenableFuture<ProcessCameraProvider> f=ProcessCameraProvider.getInstance(this);
    f.addListener(()->{try{provider=f.get();bindCamera();}catch(Exception e){tell("الكاميرا: "+e.getMessage());}},ContextCompat.getMainExecutor(this));
  }
  private void bindCamera(){
    if(provider==null || !alive)return;
    try{
      provider.unbindAll();
      Preview p=new Preview.Builder().build();p.setSurfaceProvider(preview.getSurfaceProvider());
      ImageAnalysis a=new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
      a.setAnalyzer(worker,frame->{
        try{
          long now=SystemClock.elapsedRealtime();
          if(!alive||model==null||handTracker==null||now-lastInference<700)return;
          lastInference=now;
          Result result=infer(frame.toBitmap(),frame.getImageInfo().getRotationDegrees(),cameraFacing==CameraSelector.LENS_FACING_FRONT);
          if(result==null){gate.update(null);runOnUiThread(()->{if(alive)recognized.setText("لا توجد إشارة واضحة");});return;}
          final String s=result.sign;final int percent=Math.round(result.confidence*100);
          String stable=gate.update(s);
          runOnUiThread(()->{if(alive)recognized.setText(s+" • "+percent+"%");});
          if(stable!=null)runOnUiThread(()->{if(alive){sentence+=stable;updateTranscript();}});
        }catch(Exception e){tell("مشكلة في التعرّف: "+e.getMessage());}
        finally{frame.close();}
      });
      CameraSelector selector=new CameraSelector.Builder().requireLensFacing(cameraFacing).build();
      provider.bindToLifecycle(this,selector,p,a);
      tell("وجّه يدك نحو الكاميرا");
    }catch(Exception e){tell("ربط الكاميرا: "+e.getMessage());}
  }
  private static final class Result {final String sign;final float confidence;Result(String s,float p){sign=s;confidence=p;}}
  private Result infer(Bitmap frame,int rotation,boolean selfie){
    Matrix m=new Matrix();m.postRotate(rotation);
    Bitmap upright=Bitmap.createBitmap(frame,0,0,frame.getWidth(),frame.getHeight(),m,true);
    Bitmap image=upright;
    if(selfie){Matrix flip=new Matrix();flip.setScale(-1,1);image=Bitmap.createBitmap(upright,0,0,upright.getWidth(),upright.getHeight(),flip,true);}
    try{
      HandLandmarkerResult detected=handTracker.detect(new BitmapImageBuilder(image).build());
      if(detected.landmarks().isEmpty())return null;
      List<NormalizedLandmark> points=detected.landmarks().get(0);
      float lx=1,ly=1,rx=0,ry=0;
      for(NormalizedLandmark v:points){lx=Math.min(lx,v.x());ly=Math.min(ly,v.y());rx=Math.max(rx,v.x());ry=Math.max(ry,v.y());}
      float centerX=(lx+rx)*image.getWidth()*.5f,centerY=(ly+ry)*image.getHeight()*.5f;
      float side=Math.max((rx-lx)*image.getWidth(),(ry-ly)*image.getHeight())*1.7f;
      int x1=Math.max(0,(int)(centerX-side/2)),y1=Math.max(0,(int)(centerY-side/2));
      int x2=Math.min(image.getWidth(),(int)(centerX+side/2)),y2=Math.min(image.getHeight(),(int)(centerY+side/2));
      if(x2-x1<20||y2-y1<20)return null;
      Bitmap crop=Bitmap.createBitmap(image,x1,y1,x2-x1,y2-y1);
      Bitmap small=Bitmap.createScaledBitmap(crop,64,64,true);
      int[] pixels=new int[64*64];small.getPixels(pixels,0,64,0,0,64,64);
      float[][][][] data=new float[1][64][64][3];
      for(int i=0;i<pixels.length;i++){int value=pixels[i],y=i/64,x=i%64;data[0][y][x][0]=((value>>>16)&255)/255f;data[0][y][x][1]=((value>>>8)&255)/255f;data[0][y][x][2]=(value&255)/255f;}
      float[][] output=new float[1][32];model.run(data,output);
      int best=-1;float conf=.67f;
      for(int i=0;i<32;i++)if(Float.isFinite(output[0][i])&&output[0][i]>conf){best=i;conf=output[0][i];}
      if(small!=crop)small.recycle();crop.recycle();
      return best<0?null:new Result(LABELS[best],conf);
    }finally{if(image!=upright)image.recycle();if(upright!=frame)upright.recycle();}
  }
  @Override public void onRequestPermissionsResult(int req,String[] perms,int[] grants){
    super.onRequestPermissionsResult(req,perms,grants);
    if(req==21){if(grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED)openCamera();else tell("يلزم إذن الكاميرا");}
  }
  @Override protected void onDestroy(){
    alive=false;if(provider!=null)provider.unbindAll();
    worker.execute(()->{try{if(handTracker!=null)handTracker.close();if(model!=null)model.close();}catch(Exception ignored){}});
    worker.shutdown();
    if(speaker!=null){speaker.stop();speaker.shutdown();}
    super.onDestroy();
  }
}
