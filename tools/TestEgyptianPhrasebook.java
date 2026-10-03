import org.openarabicsign.fusion.EgyptianPhrasebook;
public final class TestEgyptianPhrasebook {
 static void check(boolean x){if(!x)throw new AssertionError("Egyptian phrase matching error");}
 public static void main(String[] args){
  check(EgyptianPhrasebook.QUICK.length>=10);
  check(EgyptianPhrasebook.match("إزيك؟","ازيك"));
  check(EgyptianPhrasebook.match("مع السلامة!","مع السلامة"));
  check(!EgyptianPhrasebook.match("",""));
  check(!EgyptianPhrasebook.match("بكام ده؟","عايز أروح البيت"));
  check(EgyptianPhrasebook.key(null).isEmpty());
  check(!EgyptianPhrasebook.match("مع السلامه","مع السلامة")); // do not silently invent equivalences
  System.out.println("Egyptian phrase lookup PASS (7 assertions)");
 }
}
