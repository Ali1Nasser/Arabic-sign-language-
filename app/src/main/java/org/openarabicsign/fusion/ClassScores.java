package org.openarabicsign.fusion;
import java.util.LinkedHashMap;
import java.util.Map;
/** Coalesces separately trained output classes that map to the same written Arabic glyph. */
public final class ClassScores {
  public static final class Choice {
    public final String label;public final float confidence,margin;
    Choice(String label,float confidence,float margin){this.label=label;this.confidence=confidence;this.margin=margin;}
  }
  public static Choice select(float[] values,String[] labels){
    if(values==null||labels==null||values.length!=labels.length||values.length==0)return null;
    LinkedHashMap<String,Float> merged=new LinkedHashMap<>();
    float sum=0f;
    for(int i=0;i<values.length;i++){
      float v=values[i];
      if(labels[i]==null||labels[i].isEmpty()||!Float.isFinite(v)||v<0f)return null;
      sum+=v;
      merged.put(labels[i],merged.getOrDefault(labels[i],0f)+v);
    }
    if(!Float.isFinite(sum)||sum<=0f)return null;
    String winner=null;float first=-1f,second=0f;
    for(Map.Entry<String,Float> entry:merged.entrySet()){
      float normalized=entry.getValue()/sum;
      if(normalized>first){second=first<0f?0f:first;first=normalized;winner=entry.getKey();}
      else if(normalized>second)second=normalized;
    }
    return new Choice(winner,first,first-second);
  }
  private ClassScores(){}
}
