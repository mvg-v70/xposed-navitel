package com.mvgv70.xposed_navitel;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import android.util.Log;
import android.annotation.SuppressLint;

public class Main implements IXposedHookLoadPackage {
	
  final static String TAG = "xposed-navitel";
  @SuppressLint("SdCardPath")
  final static String NAVITEL_INI_FILE = "/data/data/com.navitel/settings.001.ini";
  	
  @Override
  public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
    if (!lpparam.packageName.equals("com.navitel")) return;
    Log.d(TAG,"package com.navitel");
    //
    // сбросим признак некорректного выхода
    setSafeExit();
    //
    // Location.getAccuracy()
    XC_MethodHook getAccuracy = new XC_MethodHook() {
        
      @Override
      protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        if ((float)param.getResult() == 0)
          param.setResult(10.0f);
      }
    };
    findAndHookMethod("android.location.Location", lpparam.classLoader, "getAccuracy", getAccuracy);
    //
    Log.d(TAG,"Navitel hook OK");
  }
  
  private void setSafeExit()
  {
    int pos = 0;
    //
    try
    {
      File f = new File(NAVITEL_INI_FILE);
      // создаем буфер нужного размера для содержимого файла
      char[] charbuf = new char[(int)f.length()];
      BufferedReader fr = new BufferedReader(new FileReader(NAVITEL_INI_FILE));
      try
      {
    	// читаем все содержимое файла
        fr.read(charbuf);
        String content = new String(charbuf);
        pos = content.indexOf("SafeExit = 0");
        Log.d(TAG,"pos="+pos);
      }
      finally
      {
        fr.close();
      }
      // если SafeExit = 0
      if (pos >= 0)
      {
        // меняем 0 на 1
    	  charbuf[pos+11] = '1';
        BufferedWriter fw =  new BufferedWriter(new FileWriter(NAVITEL_INI_FILE));
        try
        {
          // записывем в файл
          fw.write(charbuf);
        }
        finally
        {
          fw.close();
        }
      }
      Log.d(TAG,"setSafeExit OK");
    }
    catch (Exception e) 
    {
      Log.d(TAG,"error: "+e.getMessage());
    }
  }

}
	