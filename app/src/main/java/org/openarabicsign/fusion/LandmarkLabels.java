package org.openarabicsign.fusion;
/** HF LabelEncoder order verified against pinned encoder.pkl using pickletools, never pickle.load. */
public final class LandmarkLabels {
  public static final String[] ORIGINAL={"0","1","10","2","3","4","5","6","7","8","9","ain","al","aleff","bb","dal","dha","dhad","fa","gaaf","ghain","ha","haa","jeem","kaaf","khaa","laam","meem","nun","ra","saad","seen","sheen","space","ta","taa","thaa","thal","toot","waw","ya","yaa","zay"};
  public static final String[] GLYPHS={"0","1","10","2","3","4","5","6","7","8","9","ع","ال","أ","ب","د","ط","ض","ف","ج","غ","ه","ه","ج","ك","خ","ل","م","ن","ر","ص","س","ش"," ","ت","ط","ث","ذ","ت","و","ى","ي","ز"};
  static {if(ORIGINAL.length!=43||GLYPHS.length!=43)throw new AssertionError("43-class model mapping mismatch");}
  private LandmarkLabels(){}
}
