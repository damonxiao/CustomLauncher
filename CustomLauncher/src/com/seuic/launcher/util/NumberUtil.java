package com.seuic.launcher.util;

public class NumberUtil {
    
    public static boolean isPrime(int number) {
        boolean isTrue = true;
        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) {
                isTrue = false;
                break;
            }
        }
        return isTrue;
    }
}
