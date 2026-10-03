package org.openarabicsign.fusion;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

/** Egyptian Arabic conversation; user-curated, locally stored sign clips, never generated or mislabeled. */
public final class EgyptianConversationActivity extends ComponentActivity {
  private static final int FG=Color.rgb(228,241,250),MINT=Color.rgb(84,233,202);
  private static final long MAX_CLIP=80L*1024*1024;
  private TextView history,status,videoLabel;
  private EditText entry;
  private VideoView video;
  private TextToSpeech tts;
  private boolean ttsReady=false;
  private String pending="";
  private final ActivityResultLauncher<Intent> voice=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result->{
    if(result.getResultCode()!=RESULT_OK||result.getData()==null)return;
    ArrayList<String> words=result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
    if(words!=null&&!words.isEmpty()){String s=words.get(0);entry.setText(s);addLine("الطرف التاني (تعرف صوتي): ",s);status.setText("راجع دقة الكلام المتعرّف عليه قبل الرد.");}
  });
  private final ActivityResultLauncher<String[]> videoPicker=registerForActivityResult(new ActivityResultContracts.OpenDocument(),uri->{if(uri!=null)saveClip(uri);});
  private final ActivityResultLauncher<Intent> camera=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result->{
    if(result.getResultCode()==RESULT_OK&&result.getData()!=null&&result.getData().getData()!=null)saveClip(result.getData().getData());
    else status.setText("الكاميرا لم تُرجع فيديو. تقدر تختاره من ملفات الهاتف.");
  });
  private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
  private TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER);t.setPadding(dp(7),dp(9),dp(7),dp(9));return t;}
  private Button button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(14);b.setOnClickListener(v->r.run());return b;}
  private void pair(LinearLayout l,String a,Runnable ar,String b,Runnable br){LinearLayout r=new LinearLayout(this);r.addView(button(a,ar),new LinearLayout.LayoutParams(0,dp(53),1));r.addView(button(b,br),new LinearLayout.LayoutParams(0,dp(53),1));l.addView(r);}
  @Override public void onCreate(Bundle saved){
    super.onCreate(saved);
    LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setBackgroundColor(Color.rgb(10,23,39));setContentView(root);
    root.addView(label("محادثة مصري • إسمعني",23,MINT));
    root.addView(label("اتكلم، اكتب، اسمع الرد، أو افتح فيديو إشارة مصرية من مكتبتك الخاصة.",13,FG));
    ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout body=new LinearLayout(this);body.setOrientation(1);body.setPadding(dp(8),0,dp(8),dp(12));scroll.addView(body);
    history=label("ابدأ المحادثة هنا",17,FG);history.setGravity(Gravity.RIGHT);history.setTextIsSelectable(true);history.setMinHeight(dp(90));body.addView(history);
    String old=getSharedPreferences("egypt_chat",MODE_PRIVATE).getString("history","");
    if(!old.isEmpty())history.setText(old);
    String alphabet=getIntent().getStringExtra("sign_text");
    if(alphabet!=null&&!alphabet.trim().isEmpty())addLine("نص التعرف على الحروف (ليس ترجمة مصرية آلية): ",alphabet);
    entry=new EditText(this);entry.setTextSize(20);entry.setTextColor(Color.rgb(10,23,39));entry.setHintTextColor(Color.DKGRAY);
    entry.setHint("اكتب جملة بالعامية المصرية...");entry.setBackgroundColor(Color.rgb(244,249,250));entry.setMinLines(2);entry.setMaxLines(5);
    entry.setPadding(dp(12),dp(8),dp(12),dp(8));body.addView(entry);
    pair(body,"🎙 اسمع الكلام",this::listen,"🔊 قول الكلام",this::speak);
    pair(body,"أضف للمحادثة",()->{if(!current().isEmpty())addLine("أنا: ",current());},"نسخ",()->{((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Egyptian Arabic",current()));Toast.makeText(this,"تم النسخ",Toast.LENGTH_SHORT).show();});
    body.addView(label("ردود مصرية سريعة — نصوص وليست إشارات مولّدة",14,MINT));
    String[] quick=EgyptianPhrasebook.QUICK;
    for(int i=0;i<quick.length;i+=2){final String a=quick[i],b=i+1<quick.length?quick[i+1]:"";
      if(!b.isEmpty())pair(body,a,()->entry.setText(a),b,()->entry.setText(b));else body.addView(button(a,()->entry.setText(a)));}
    body.addView(label("نص ← إشارة مصرية • مكتبة الفيديوهات الشخصية",18,MINT));
    body.addView(label("الفيديو يظهر فقط بعد ما تسجّله أو تختاره وتراجع صحته مع شخص مُتقن للإشارة المصرية؛ لا نولّد إشارات غير مؤكدة.",13,FG));
    pair(body,"اعرض الإشارة",this::showSign,"أضف تسجيل",this::chooseClip);
    pair(body,"مكتبتي",this::showLibrary,"احذف تسجيل",this::deleteClip);
    videoLabel=label("مفيش فيديو شغال",14,FG);body.addView(videoLabel);
    video=new VideoView(this);video.setVisibility(View.GONE);body.addView(video,new LinearLayout.LayoutParams(-1,dp(210)));
    status=label("المحادثة والفيديوهات محفوظة محليًا. التعرف الصوتي قد يحتاج إنترنت حسب المحرك المثبّت.",12,FG);body.addView(status);
    pair(body,"مسح المحادثة",()->new AlertDialog.Builder(this).setTitle("تمسح سجل المحادثة؟").setNegativeButton("إلغاء",null).setPositiveButton("مسح",(d,w)->{
      history.setText("ابدأ المحادثة هنا");getSharedPreferences("egypt_chat",MODE_PRIVATE).edit().remove("history").apply();}).show(),"رجوع للكاميرا",this::finish);
    tts=new TextToSpeech(this,code->{if(code==TextToSpeech.SUCCESS&&tts!=null){
      int available=tts.setLanguage(new Locale("ar","EG"));
      ttsReady=available!=TextToSpeech.LANG_MISSING_DATA&&available!=TextToSpeech.LANG_NOT_SUPPORTED;
      if(!ttsReady)status.setText("صوت ar-EG مش موجود على الجهاز، ثبّت صوت عربي من إعدادات Text-to-Speech.");}});
  }
  private String current(){return entry.getText().toString().trim();}
  private void addLine(String who,String content){String old=history.getText().toString();if(old.equals("ابدأ المحادثة هنا"))old="";
    String next=(old.isEmpty()?"":old+"\n\n")+who+content;if(next.length()>8000)next=next.substring(next.length()-8000);
    history.setText(next);getSharedPreferences("egypt_chat",MODE_PRIVATE).edit().putString("history",next).apply();}
  private void listen(){Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ar-EG");intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"ar-EG");
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"اتكلم بالمصري");
    try{voice.launch(intent);}catch(ActivityNotFoundException|SecurityException ex){status.setText("خدمة تحويل الصوت لنص غير متاحة؛ استخدم الكتابة.");}}
  private void speak(){String s=current();if(s.isEmpty()){status.setText("اكتب جملة الأول.");return;}
    if(!ttsReady||tts==null){status.setText("صوت عربي مش متاح على جهازك حاليًا.");return;}
    tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"egyptian-speech");addLine("أنا (صوت): ",s);}
  private JSONArray clips(){try{return new JSONArray(getSharedPreferences("egypt_chat",MODE_PRIVATE).getString("clips","[]"));}catch(Exception e){return new JSONArray();}}
  private void store(JSONArray a){getSharedPreferences("egypt_chat",MODE_PRIVATE).edit().putString("clips",a.toString()).apply();}
  private void chooseClip(){EditText field=new EditText(this);field.setText(current());field.setHint("الكلمة أو الجملة المصرية");
    new AlertDialog.Builder(this).setTitle("تسجيل إشارة مصرية موثّقة").setMessage("احصل على موافقة صاحب التسجيل، وتأكد من صحة الإشارة. كل شيء هيتحفظ محليًا.")
      .setView(field).setNegativeButton("إلغاء",null).setNeutralButton("صوّر",(d,w)->startClip(field.getText().toString(),true))
      .setPositiveButton("اختر فيديو",(d,w)->startClip(field.getText().toString(),false)).show();}
  private void startClip(String phrase,boolean record){pending=phrase.trim();if(pending.isEmpty()){status.setText("اكتب اسم الإشارة الأول.");return;}
    try{if(record){Intent i=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);i.putExtra(MediaStore.EXTRA_DURATION_LIMIT,30);camera.launch(i);}
      else videoPicker.launch(new String[]{"video/*"});}
    catch(ActivityNotFoundException|SecurityException e){status.setText("مفيش تطبيق كاميرا/ملفات متاح.");}}
  private void saveClip(Uri uri){if(pending.isEmpty())return;File file=new File(getFilesDir(),"egypt_sign_"+UUID.randomUUID()+".mp4");
    try(InputStream in=getContentResolver().openInputStream(uri);FileOutputStream out=new FileOutputStream(file)){
      if(in==null)throw new IllegalStateException("الفيديو مش قابل للقراءة");
      byte[] buffer=new byte[8192];long size=0;int n;
      while((n=in.read(buffer))!=-1){size+=n;if(size>MAX_CLIP)throw new IllegalStateException("الحد الأقصى للفيديو ٨٠ ميجابايت");out.write(buffer,0,n);}
      if(size==0)throw new IllegalStateException("الفيديو فاضي");
      JSONArray a=clips();String key=EgyptianPhrasebook.key(pending);
      for(int i=a.length()-1;i>=0;i--){JSONObject o=a.optJSONObject(i);if(o!=null&&key.equals(EgyptianPhrasebook.key(o.optString("phrase")))){
        String name=o.optString("file");File old=new File(getFilesDir(),name);if(old.getName().equals(name))old.delete();a.remove(i);}}
      JSONObject o=new JSONObject();o.put("phrase",pending);o.put("file",file.getName());a.put(o);store(a);
      status.setText("تم حفظ الفيديو محليًا: "+pending);entry.setText(pending);pending="";showSign();
    }catch(Exception e){file.delete();status.setText("فشل حفظ الفيديو: "+e.getMessage());}}
  private void showSign(){String s=current();if(s.isEmpty()){status.setText("اكتب كلمة أو جملة الأول.");return;}
    JSONArray a=clips();for(int i=a.length()-1;i>=0;i--){JSONObject o=a.optJSONObject(i);
      if(o!=null&&EgyptianPhrasebook.match(s,o.optString("phrase"))){String name=o.optString("file");File file=new File(getFilesDir(),name);
        if(!file.getName().equals(name)||!file.isFile()){status.setText("فيديو الإشارة مفقود.");return;}
        video.setVisibility(View.VISIBLE);video.setVideoPath(file.getAbsolutePath());video.setOnPreparedListener(mp->{mp.setLooping(false);video.start();});
        videoLabel.setText("فيديو من مكتبتك: "+o.optString("phrase"));status.setText("فيديو شخصي وليس ترجمة إشارة آلية.");return;
      }}
    video.stopPlayback();video.setVisibility(View.GONE);videoLabel.setText("مفيش فيديو مطابق");
    status.setText("مفيش تسجيل موثّق للجملة دي. تقدر تضيف فيديو صحيح من زر إضافة تسجيل.");}
  private void showLibrary(){JSONArray a=clips();if(a.length()==0){status.setText("المكتبة فاضية. ابدأ بإضافة فيديو إشارة.");return;}
    String[] names=new String[a.length()];for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);names[i]=o==null?"":o.optString("phrase");}
    new AlertDialog.Builder(this).setTitle("إشاراتي المحلية: "+a.length()).setItems(names,(d,index)->{entry.setText(names[index]);showSign();}).setNegativeButton("قفل",null).show();}
  private void deleteClip(){JSONArray a=clips();if(a.length()==0){status.setText("المكتبة فاضية.");return;}
    String[] names=new String[a.length()];for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);names[i]=o==null?"":o.optString("phrase");}
    new AlertDialog.Builder(this).setTitle("اختار فيديو للحذف").setItems(names,(d,index)->
      new AlertDialog.Builder(this).setMessage("تحذف تسجيل «"+names[index]+"» نهائيًا؟").setNegativeButton("إلغاء",null).setPositiveButton("حذف",(q,w)->{
        JSONObject o=a.optJSONObject(index);if(o!=null){String name=o.optString("file");File file=new File(getFilesDir(),name);if(file.getName().equals(name))file.delete();}
        a.remove(index);store(a);video.stopPlayback();video.setVisibility(View.GONE);status.setText("تم الحذف.");}).show()).show();}
  @Override protected void onDestroy(){if(video!=null)video.stopPlayback();if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
