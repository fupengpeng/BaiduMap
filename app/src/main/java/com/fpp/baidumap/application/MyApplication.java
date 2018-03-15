package com.fpp.baidumap.application;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * @author fupengpeng
 * @description 描述
 * @data 2018/3/14 0014 16:11
 */

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());


    }
}
