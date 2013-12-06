package com.wz.gif.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

public class FileUtil {
    
    public static String DIR;
    
    public enum FileType{
        GIF,JPG
    }
/**
 * 我们需要新建两个文件夹，一个用来保存gif图，另外一个用来保存gif图片的第一帧，这样做的好处是减少了每次打开welcomeActivity时解析
   页面的速度，用空间来换时间。
 * @param paramContext
 * @param type 0 表示建/GIF胶卷， 1 表示建GIF胶卷碎片
 */
    public FileUtil(Context paramContext,FileType type){
        switch(type){
            case GIF:
                DIR = Environment.getExternalStorageDirectory()+"/GIFjiaojuan/";
                break;
            case JPG:
                DIR = Environment.getExternalStorageDirectory()+"/GIF胶卷碎片/";
                break;
        }
        File dir = new File(DIR);
        if(!dir.exists()){
            Log.d("no such dir:"+DIR +" will be made");
            new File(DIR).mkdir();
        }else{
            Log.d("have such DIR:"+DIR);
        }
    }
    
    public FileUtil(Context paramContext){
        DIR = Environment.getExternalStorageDirectory()+"/GIFjiaojuan/";
        File dir = new File(DIR);
        if(!dir.exists()){
            Log.d("no such dir:"+DIR +" will be made");
            new File(DIR).mkdir();
        }else{
            Log.d("have such DIR:"+DIR);
        }
    }
    
    /**
     * 得到文件夹里的文件和文件名，执行完该方法后就可以getFileNames
     */
    public static ArrayList<InputStream> getGIfStream()
    {
        File baseFile = new File(DIR);
        File[] files = baseFile.listFiles();
        ArrayList<InputStream> gifList = new ArrayList<InputStream>();
        fileNames = new ArrayList<String>();
        InputStream stream = null;
        if(files != null){
            Log.d("there are "+files.length+ " files");
            for(File f:files){
                try {
                    Log.d("file is " +f.getName() );
                    stream = new FileInputStream(f);
                } catch (FileNotFoundException e) {
                    Log.e("error from getGif:"+e.toString());
                }
                    fileNames.add(f.getName());
                    gifList.add(stream);
                }
        }
        return gifList;
    }

    private static ArrayList<String> fileNames;
    
    public void setFileNames(ArrayList<String> fileNames) {
        FileUtil.fileNames = fileNames;
    }


    public ArrayList<String> getFileNames() {
        return fileNames;
    }


    public static String encodeUrl(Bundle params){
        if (params == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String key : params.keySet()) {
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(key + "=" + URLEncoder.encode(params.getString(key)));
        }
        return sb.toString();
    }
    
    /**
     * 删除文件
     * @param string
     */
    public String deleteFile(String fileName) {
        Log.d("will delete file "+fileName);
        if( !TextUtils.isEmpty(fileName)){
            File dir = new File(DIR);
            File[] files = dir.listFiles();
            for(File f:files){
                if(f.getName().endsWith(fileName))
                    if(f.delete()){
                        Log.d("delete success "+ f.getName());
                        return "ok";
                    }
            }
        }
        return null;
    }

    /**
     * 得到本次发送的图片的大小
     * @param index
     * @return
     */
    public long getFileSize(int index) {
        File dir = new File(DIR);
        File[] files = dir.listFiles();
        return files[index].length();
    }

    /*
     * 单独代开一个图像文件
     */
    public static InputStream getFileStream(String fileName) {
        Log.d("要打开的文件是："+fileName);
        File file = new File(fileName);
        try {
            InputStream is = new FileInputStream(file);
            return is;
        } catch (Exception e) {
            Log.e("error from getFileStream "+e.toString());
        }
        return null;
    }
    
}
