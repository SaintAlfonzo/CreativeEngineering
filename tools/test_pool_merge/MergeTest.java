package com.SaintAlfonzo.creativeengineering.worldgen;
import java.util.*;
public class MergeTest {
  static int fails=0;
  static void eq(String n,double a,double b){ if(a!=b){fails++;System.out.println("FAIL "+n+" got "+a+" want "+b);} else System.out.println("ok   "+n);}
  public static void main(String[] a){
    List<String> w=new ArrayList<>();
    eq("same original keeps current", PoolTuner.mergeCurrent("p","k #2",5,5,7,w),7);
    eq("no stored original keeps current", PoolTuner.mergeCurrent("p","k",5,Double.NaN,7,w),7);
    eq("reordered unedited follows live", PoolTuner.mergeCurrent("p","k #2",100,5,5,w),100);
    int before=w.size();
    eq("reordered edited keeps", PoolTuner.mergeCurrent("p","k #2",100,5,9,w),9);
    if(w.size()!=before+1||!w.get(w.size()-1).contains("edited duplicate")){fails++;System.out.println("FAIL warn "+w);} else System.out.println("ok   warns: "+w.get(w.size()-1));
    PoolTuner.mergeCurrent("p","k",3,2,1,w);
    System.out.println(w.get(w.size()-1).contains("mod changed")?"ok   non-duplicate warning":"FAIL "+w);
    System.out.println(fails==0?"ALL PASS":fails+" FAILED"); System.exit(fails);
  }
}
