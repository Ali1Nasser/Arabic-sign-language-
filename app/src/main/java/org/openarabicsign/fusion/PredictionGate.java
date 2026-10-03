package org.openarabicsign.fusion;
/** Stateful transcription gate. Confidence is a heuristic (not calibrated probability).
 * An unclear prediction does NOT re-arm a held sign. Release hand twice to re-arm. */
public final class PredictionGate {
  private String candidate="",emitted="",lastAccepted="";
  private int count=0,absent=0;
  public synchronized void reset(){candidate="";emitted="";lastAccepted="";count=0;absent=0;}
  public synchronized String lastAccepted(){return lastAccepted;}
  public synchronized String update(String sign,float confidence,float margin,boolean handVisible){
    if(!handVisible){
      candidate="";count=0;if(++absent>=2)emitted="";return null;
    }
    absent=0;
    if(sign==null||sign.isEmpty()||!Float.isFinite(confidence)||!Float.isFinite(margin)||confidence<.66f||margin<.13f){
      candidate="";count=0;return null;
    }
    if(!sign.equals(candidate)){candidate=sign;count=1;}else count++;
    if(count>=3&&!sign.equals(emitted)){emitted=sign;lastAccepted=sign;return sign;}
    return null;
  }
}
