package com.example.readnoveldemo.util;

/**
 * Created by jcman on 16-8-19.
 */
public class StringUtil {

    public static boolean contains(String a,String b){
        String t1 = a.replaceAll(" ","");
        String t2 = b.replaceAll(" ","");
        if (t1.contains(t2)||t2.contains(t1))
            return true;
        return false;
    }
}
