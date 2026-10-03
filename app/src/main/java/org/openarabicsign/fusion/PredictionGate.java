package org.openarabicsign.fusion;
public final class PredictionGate {
  private String candidate="",emitted="";
  private int count=0,neutral=0;
  public synchronized void reset(){candidate="";emitted="";count=0;neutral=0;}
  public synchronized String update(String sign){
    if(sign==null||sign.isEmpty()){candidate="";count=0;if(++neutral>=3)emitted="";return null;}
    neutral=0;
    if(!sign.equals(candidate)){candidate=sign;count=1;}else count++;
    if(count>=3&&!sign.equals(emitted)){emitted=sign;return sign;}
    return null;
  }
}
