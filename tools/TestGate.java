import org.openarabicsign.fusion.PredictionGate;
public final class TestGate {
  static void ok(boolean b){if(!b)throw new AssertionError("PredictionGate contract");}
  public static void main(String[] args){
    PredictionGate p=new PredictionGate();
    ok(p.update("ب")==null);ok(p.update("ب")==null);ok("ب".equals(p.update("ب")));
    for(int i=0;i<5;i++)ok(p.update("ب")==null);
    p.update(null);p.update(null);p.update(null);
    ok(p.update("ب")==null);ok(p.update("ب")==null);ok("ب".equals(p.update("ب")));
    p.reset();ok(p.update("ت")==null);ok(p.update("ت")==null);ok("ت".equals(p.update("ت")));
    System.out.println("PASS: debounce, hold, rearm, reset");
  }
}
