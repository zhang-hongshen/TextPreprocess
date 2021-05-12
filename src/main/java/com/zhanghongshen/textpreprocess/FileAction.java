package com.zhanghongshen.textpreprocess;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author Zhang Hongshen
 * @description Some file actions
 * @date 2021/5/12
 */
public class FileAction {
    public static String readFile(String filePath){
        StringBuffer res = new StringBuffer();
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                res.append(s);
            }
            bufferedReader.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return res.toString();
    }

    public static  boolean writeFile(String content,String filePath){
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
            bufferedWriter.flush();
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
