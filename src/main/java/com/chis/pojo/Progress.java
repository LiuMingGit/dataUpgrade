package com.chis.pojo;

import java.util.HashMap;

public class Progress {

    private static boolean isInit = false;

    private static HashMap<String,Progresz> progreszs = new HashMap<>();

    public static HashMap<String,Progresz> getProgreszs() {
        return progreszs;
    }

    public static boolean getIsInit() {
        return isInit;
    }

    public static void setIsInit(boolean isInit) {
        Progress.isInit = isInit;
    }
}
