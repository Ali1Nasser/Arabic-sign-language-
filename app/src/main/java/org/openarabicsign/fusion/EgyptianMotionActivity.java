package org.openarabicsign.fusion;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.graphics.Color;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.activity.ComponentActivity;

/** Actual embedded H.264 motion clips created from procedural, animated stickman rigs.
 * Concept sketches: neither Egyptian Sign Language recognition nor certified instruction.
 */
public final class EgyptianMotionActivity extends ComponentActivity {
  private static final int FG=Color.rgb(235,245,250),MINT=Color.rgb(84,233,202);
  private static final String[] NAMES={"إزيك؟","تمام","عايز","شكراً","مياه","يلا"};
  private static final String[] GLOSS={"How are you?","Fine / OK","I want","Thank you","Water","Let's go"};
  private static final int[] RES={
    R.raw.motion_ezayak,R.raw.motion_tamam,R.raw.motion_ayez,
    R.raw.motion_shokran,R.raw.motion_mayya,R.raw.motion_yalla
  };
  private VideoView video;
  private MediaPlayer player;
  private TextView selected,feedback,playButtonLabel;
  private Button play,speedButton;
  private int index=0;
  private float rate=1f;
  private boolean prepared=false,shouldPlay=true;

  private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
  private TextView label(String s,int size,int color){
    TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);
    t.setGravity(Gravity.CENTER);t.setPadding(dp(7),dp(9),dp(7),dp(9));return t;
  }
  private Button button(String s,Runnable task){
    Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(14);
    b.setOnClickListener(v->task.run());return b;
  }
  @Override public void onCreate(Bundle saved){
    super.onCreate(saved);
    LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
    root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setBackgroundColor(Color.rgb(10,23,39));setContentView(root);
    root.addView(label("قاموس الحركة • إسمعني",23,MINT));
    root.addView(label("فيديوهات MP4 متحركة فعلًا — حركة Stickman سلسة ومتكررة، مش صور متتابعة.",13,FG));
    root.addView(label("نماذج بصرية توضيحية فقط؛ لسه محتاجة مراجعة متخصص في لغة الإشارة المصرية قبل استخدامها كتعليم أو ترجمة معتمدة.",12,Color.rgb(255,195,121)));
    ScrollView scroll=new ScrollView(this);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(8),dp(2),dp(8),dp(12));
    scroll.addView(body);
    selected=label("",23,MINT);body.addView(selected);
    video=new VideoView(this);video.setBackgroundColor(Color.rgb(250,252,253));
    body.addView(video,new LinearLayout.LayoutParams(-1,dp(280)));
    MediaController controller=new MediaController(this);controller.setAnchorView(video);video.setMediaController(controller);
    LinearLayout controls=new LinearLayout(this);
    play=button("⏸ إيقاف مؤقت",this::toggle);
    speedButton=button("السرعة 1×",this::toggleSpeed);
    controls.addView(play,new LinearLayout.LayoutParams(0,dp(54),1));
    controls.addView(button("↺ إعادة",()->{if(prepared){video.seekTo(0);video.start();shouldPlay=true;play.setText("⏸ إيقاف مؤقت");}}),new LinearLayout.LayoutParams(0,dp(54),1));
    controls.addView(speedButton,new LinearLayout.LayoutParams(0,dp(54),1));
    body.addView(controls);
    body.addView(label("اختار كلمة — كل واحدة فيديو منفصل بحركة مستمرة و24 إطار/ثانية",13,FG));
    for(int i=0;i<NAMES.length;i+=2){
      final int a=i,b=i+1;
      LinearLayout row=new LinearLayout(this);
      row.addView(button(NAMES[a],()->choose(a)),new LinearLayout.LayoutParams(0,dp(57),1));
      row.addView(button(NAMES[b],()->choose(b)),new LinearLayout.LayoutParams(0,dp(57),1));
      body.addView(row);
    }
    feedback=label("كل المقاطع مدمجة في التطبيق وممكن تشغيلها بدون إنترنت.",12,FG);body.addView(feedback);
    body.addView(button("رجوع لمحادثة مصري",this::finish));
    choose(0);
  }
  private void choose(int newIndex){
    index=newIndex;prepared=false;shouldPlay=true;rate=1f;
    if(player!=null){video.stopPlayback();player=null;}
    play.setText("⏸ إيقاف مؤقت");speedButton.setText("السرعة 1×");
    selected.setText(NAMES[index]+"  •  "+GLOSS[index]);
    video.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+RES[index]));
    video.setOnPreparedListener(mp->{
      player=mp;prepared=true;mp.setLooping(true);
      video.setBackgroundColor(Color.TRANSPARENT);
      video.start();
      feedback.setText("الفيديو شغّال في حلقة مستمرة. اضغط على الفيديو لتحريك شريط الوقت.");
    });
    video.setOnErrorListener((mp,what,extra)->{prepared=false;feedback.setText("تعذّر تشغيل الفيديو على هذا الهاتف ("+what+"/"+extra+").");return true;});
    video.requestFocus();
  }
  private void toggle(){
    if(!prepared){Toast.makeText(this,"جاري تجهيز الفيديو",Toast.LENGTH_SHORT).show();return;}
    if(video.isPlaying()){video.pause();shouldPlay=false;play.setText("▶ تشغيل");}
    else{video.start();shouldPlay=true;play.setText("⏸ إيقاف مؤقت");}
  }
  private void toggleSpeed(){
    rate=rate<.75f?1f:rate<1.25f?1.5f:.5f;
    speedButton.setText("السرعة "+(rate==.5f?"0.5×":rate==1f?"1×":"1.5×"));
    if(!prepared||player==null)return;
    try{player.setPlaybackParams(player.getPlaybackParams().setSpeed(rate));}
    catch(IllegalStateException|IllegalArgumentException e){feedback.setText("تغيير السرعة غير مدعوم على هذا الهاتف.");}
  }
  @Override protected void onPause(){super.onPause();if(video!=null&&video.isPlaying())video.pause();}
  @Override protected void onResume(){super.onResume();if(prepared&&shouldPlay)video.start();}
  @Override protected void onDestroy(){if(video!=null)video.stopPlayback();player=null;super.onDestroy();}
}
