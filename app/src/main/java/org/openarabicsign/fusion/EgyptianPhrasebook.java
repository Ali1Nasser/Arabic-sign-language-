package org.openarabicsign.fusion;
import java.util.Locale;
/** Exact normalized phrase lookup only; never translates or fabricates an Egyptian sign. */
public final class EgyptianPhrasebook {
 public static final String[] QUICK={"إزيك؟","الحمد لله تمام","ممكن تساعدني؟","بكام ده؟","عايز أروح البيت","فين أقرب محطة؟","مش فاهم","ممكن تعيد تاني؟","من فضلك","شكراً","مع السلامة","محتاج مترجم إشارة"};
 public static String key(String value){if(value==null)return "";String s=value.toLowerCase(Locale.ROOT).replaceAll("[\\u064b-\\u065f\\u0670\\u0640]","").replaceAll("[أإآٱ]","ا").replace('ى','ي');return s.replaceAll("[\\p{Punct}؟،؛«»…]"," ").replaceAll("\\s+"," ").trim();}
 public static boolean match(String asked,String stored){String a=key(asked);return !a.isEmpty()&&a.equals(key(stored));}
 private EgyptianPhrasebook(){}
}
