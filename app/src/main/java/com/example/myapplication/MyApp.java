package com.example.myapplication;

// 该文件主要用于在几个java文件之间传值，充当全局变量的作用
public class MyApp{
    static int ColorTips;
    static boolean reset;
    static boolean delete;

    public static int getColorTips(){
        return ColorTips;
    }

    public void setColorTips(int ColorTips){
        this.ColorTips=ColorTips;
    }
}
