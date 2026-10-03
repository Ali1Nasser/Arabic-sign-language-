import org.openarabicsign.fusion.PredictionGate;
import org.openarabicsign.fusion.ClassScores;
public final class TestGate {
  static void ok(boolean b){if(!b)throw new AssertionError("Recognition gate contract");}
  public static void main(String[] args){
    PredictionGate p=new PredictionGate();
    ok(p.update("ب",.90f,.20f,true)==null);
    ok(p.update("ب",.90f,.20f,true)==null);
    ok("ب".equals(p.update("ب",.90f,.20f,true)));
    for(int i=0;i<5;i++)ok(p.update("ب",.90f,.20f,true)==null);
    ok(p.update(null,0,0,false)==null);
    ok(p.update("ب",.90f,.20f,true)==null); // single missed hand frame cannot re-arm
    ok(p.update(null,0,0,false)==null);ok(p.update(null,0,0,false)==null);
    ok(p.update("ب",.90f,.20f,true)==null);ok(p.update("ب",.90f,.20f,true)==null);
    ok("ب".equals(p.update("ب",.90f,.20f,true)));
    ok("ب".equals(p.lastAccepted()));
    for(int i=0;i<3;i++)ok(p.update("ب",.99f,.01f,true)==null); // ambiguous frame must not duplicate
    for(int i=0;i<3;i++)ok(p.update("ب",.20f,.60f,true)==null); // low confidence
    for(int i=0;i<3;i++)ok(p.update("ت",.90f,.22f,true)==(i==2?"ت":null));
    p.reset();ok(p.lastAccepted().isEmpty());
    ClassScores.Choice c=ClassScores.select(new float[]{.27f,.25f,.30f,.18f},new String[]{"ه","ه","ب","ت"});
    ok(c!=null&&"ه".equals(c.label)&&Math.abs(c.confidence-.52f)<.0001f&&Math.abs(c.margin-.22f)<.0001f);
    ok(ClassScores.select(new float[]{Float.NaN,1f},new String[]{"أ","ب"})==null);
    ok(ClassScores.select(new float[]{0f,0f},new String[]{"أ","ب"})==null);
    ok(ClassScores.select(new float[]{1f},new String[]{"أ","ب"})==null);
    ok(org.openarabicsign.fusion.LandmarkLabels.ORIGINAL.length==43);
    ok(org.openarabicsign.fusion.LandmarkLabels.ORIGINAL[2].equals("10"));
    ok(org.openarabicsign.fusion.LandmarkLabels.ORIGINAL[33].equals("space"));
    ok(org.openarabicsign.fusion.LandmarkLabels.GLYPHS[33].equals(" "));
    System.out.println("PASS: stable, held, release, low margin, low confidence, duplicate labels, invalid scores");
  }
}
