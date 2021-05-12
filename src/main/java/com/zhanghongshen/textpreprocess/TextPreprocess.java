package com.zhanghongshen.textpreprocess;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zhang Hongshen
 * @description 文本预处理
 * @date 2021/5/3
 */
public class TextPreprocess {

    public static String deleteHtmlTag(String htmlStr){
        //style标签的正则表达式
        String styleTagRegex = "<style[^>]*?>[\\s\\S]*?<\\/style>";
        //script标签的正则表达式
        String scriptTagRegex = "<script[^>]*?>[\\s\\S]*?<\\/script>";
        //html标签的正则表达式
        String htmlTagRegex = "<[^>]+>";
        Pattern pattern = Pattern.compile(styleTagRegex,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(htmlStr);
        htmlStr = matcher.replaceAll(" ");
        pattern = Pattern.compile(scriptTagRegex,Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(htmlStr);
        htmlStr = matcher.replaceAll(" ");
        pattern = Pattern.compile(htmlTagRegex,Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(htmlStr);
        htmlStr = matcher.replaceAll(" ");
        return htmlStr.replaceAll("&nbsp;"," ")
                .replaceAll("&amp;"," ")
                .trim(); //返回文本字符串
    }

    public String deleteChineseSepcialChar(String text){
        String regex = "[_`~!@#$%^&*()+=|{}':;,\\\\[\\\\].<>/?！￥…（）—【】‘；：”“’。，、？]|\\n|\\r|\\t";
        return  text.replaceAll(regex,"")
                .replaceAll(" ","")
                .replaceAll("<a>\\\\s*|\\t|\\r|\\n</a>", "")
                .replaceAll(" ","")
                .trim();
    }

    public String deleteEnglishSepcialChar(String text){
        String regex = "[_`~!@#$%^&*()+=|{}':;,\\\\[\\\\].<>/?！￥…（）—【】‘；：”“’。，、？]|\\n|\\r|\\t";
        return  text.replaceAll(regex," ")
                .trim();
    }



    public String cnProcess(String text){
        text = deleteHtmlTag(text);
        //删除中文特殊字符
        text = deleteChineseSepcialChar(text);
        StringBuffer temp = new StringBuffer(text);
        //储存分词结果
        List<String> words = new ArrayList<>();
        //调用阿里云中文分词功能
        while(true){
            if(temp.length() > AliyunNlp.MAX_TEXT_LENGTH){
                List<String> result = new AliyunNlp(text.substring(0, AliyunNlp.MAX_TEXT_LENGTH)).getAliyunNlpResult();
                words.addAll(result);
                temp.delete(0, AliyunNlp.MAX_TEXT_LENGTH);
            }else{
                List<String> result = new AliyunNlp(text).getAliyunNlpResult();
                words.addAll(result);
                break;
            }
        }
        //过滤空白词
        words.removeIf(e -> e.length() == 0);
        //读取中文停用词
        List<String> chineseStopwords = new ArrayList<>(Arrays.asList(FileAction.readFile("stopwords/cn_stopwords.txt").split(" ")));
        //删除中文停用词
        words.removeAll(chineseStopwords);
        StringBuffer res = new StringBuffer();
        for(String word : words){
            res.append(word).append(" ");
        }
        return res.toString();
    }

    /**
     * @param file 需要处理的文件
     * @return return true while process succeed,otherwise failed
     */

    public boolean cnProcess(File file) {
        if(!file.exists()){
            System.out.println("File doesn't exist.");
            return false;
        } else if(file.isDirectory()){
            System.out.println("File is a directory.");
            return false;
        }
        //读取文件
        String text = FileAction.readFile(file.getPath());
        //处理
        String res = cnProcess(text);
        return FileAction.writeFile(res,file.getAbsolutePath()+"_Processed.txt");
    }

    public String enProcess(String text){
        //转换为小写字母
        text = text.toLowerCase();
        text = deleteHtmlTag(text);
        //删除英文特殊字符
        text = deleteEnglishSepcialChar(text);
        //英文分词
        List<String> words = new ArrayList<>(Arrays.asList(text.split(" ")));
        //过滤空白词
        words.removeIf(e -> e.length() == 0);
        //读取英文停用词
        List<String> englishStopwords = new ArrayList<>(Arrays.asList(FileAction.readFile("stopwords/en_stopwords.txt").split(" ")));
        //删除英文停用词
        words.removeAll(englishStopwords);
        //Porter Stemming方法提取词干
        Stemmer s = new Stemmer();
        StringBuffer res =  new StringBuffer();
        for(String word : words){
            s.add(word);
            s.stem();
            res.append(s).append(" ");
        }
        String[] stemmingWords = res.toString().split(" ");
        res.delete(0,res.length());
        for(String stemmingWord : stemmingWords){
            res.append(stemmingWord).append(" ");
        }
        //输出结果
        return res.toString();
    }

    /**
     * @param file 需要处理的文件
     * @return return true while process succeed,otherwise failed
     */
    public boolean enProcess(File file){
        if(!file.exists()){
            System.out.println("File doesn't exist.");
            return false;
        } else if(file.isDirectory()){
            System.out.println("File is a directory.");
            return false;
        }
        //读取文件
        String text = FileAction.readFile(file.getPath());
        //处理
        String res = enProcess(text);
        //输出结果
        return FileAction.writeFile(res,file.getAbsolutePath()+"_Processed.txt");
    }

    public static void main(String[] args){
        /**
         * 中文文档预处理，由于阿里云Nlp频率限制会存在问题
         */
        new TextPreprocess().cnProcess(new File("src/test/resources/chinese/1_C_Org.html"));
        /**
         * 英文文档预处理
         */
        new TextPreprocess().enProcess(new File("src/test/resources/english/1_E_Org.html"));
    }
}

