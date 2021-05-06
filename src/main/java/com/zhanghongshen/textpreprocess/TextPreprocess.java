package main.java.com.zhanghongshen.textpreprocess;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

    public String readFile(String filePath){
        StringBuffer res = new StringBuffer();
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,StandardCharsets.UTF_8);
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

    public boolean writeFile(String content,String filePath){
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

    public String cnProcess(String text){
        text = deleteHtmlTag(text);
        //删除中文特殊字符
        text = deleteChineseSepcialChar(text);
        StringBuffer temp = new StringBuffer(text);
        //储存分词结果
        var words = new ArrayList<String>();
        //调用阿里云中文分词功能
        while(true){
            if(temp.length() > AliyunNlp.MAX_TEXT_LENGTH){
                var result = new AliyunNlp(text.substring(0,AliyunNlp.MAX_TEXT_LENGTH)).getAliyunNlpResult();
                words.addAll(result);
                temp.delete(0,AliyunNlp.MAX_TEXT_LENGTH);
            }else{
                var result = new AliyunNlp(text).getAliyunNlpResult();
                words.addAll(result);
                break;
            }
        }
        //过滤空白词
        var wordsList = new ArrayList<String>();
        for(String word : words){
            if(word.length() != 0){ wordsList.add(word); }
        }
        //读取中文停用词
        String[] chineseStopwords = readFile("stopwords/cn_stopwords.txt").split(" ");
        var chineseStopwordList = new ArrayList<>(Arrays.asList(chineseStopwords));
        //删除中文停用词
        wordsList.removeAll(chineseStopwordList);
        StringBuffer res = new StringBuffer();
        for(String word : wordsList){
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
        String text = readFile(file.getPath());
        //处理
        String res = cnProcess(text);
        return writeFile(res,file.getAbsolutePath()+"_Processed.txt");
    }

    public String enProcess(String text){
        //转换为小写字母
        text = text.toLowerCase();
        text = deleteHtmlTag(text);
        //删除英文特殊字符
        text = deleteEnglishSepcialChar(text);
        //英文分词
        String[] words = text.split(" ");
        //过滤空白词
        var wordsList = new ArrayList<String>();
        for(String word : words){
            if(word.length() != 0){ wordsList.add(word); }
        }
        //读取英文停用词
        String[] englishStopwords = readFile("stopwords/en_stopwords.txt").split(" ");
        var englishStopwordList = new ArrayList<>(Arrays.asList(englishStopwords));
        //删除英文停用词
        wordsList.removeAll(englishStopwordList);
        words = wordsList.toArray(new String[wordsList.size()]);
        //Porter Stemming方法提取词干
        Stemmer s = new Stemmer();
        StringBuffer temp =  new StringBuffer();
        for(String word : words){
            s.add(word);
            s.stem();
            temp.append(s).append(" ");
        }
        String[] stemmingWords = temp.toString().split(" ");
        temp.delete(0,temp.length());
        for(String stemmingWord : stemmingWords){
            temp.append(stemmingWord).append(" ");
        }
        //输出结果
        return temp.toString();
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
        String text = readFile(file.getPath());
        //处理
        String res = enProcess(text);
        //输出结果
        return writeFile(res,file.getAbsolutePath()+"_Processed.txt");
    }

    public static void main(String[] args){
        /**
         * 中文文档预处理，由于阿里云Nlp频率限制会存在问题
         */
//        String cnFileDirectory = "src/test/resources/chinese/";
//        File cnFileFolder = new File(cnFileDirectory);
//        if(!cnFileFolder.exists()){
//            System.out.println("File doesn't exist!");
//            return;
//        }
//        File[] cnFiles = cnFileFolder.listFiles();
//        int cnFileNum = 0;
//        System.out.println("处理中...");
//        for(File cnFile : cnFiles){
//            if(cnFile.isFile()){
//                if(!new TextPreprocess().cnProcess(cnFile)){
//                    System.out.println(cnFile.getPath()+" failed.");
//                    continue;
//                }
//                cnFileNum++;
//            } else if(cnFile.isDirectory()){
//                System.out.println(cnFile.getPath()+" is a directory.");
//            }
//        }
//        System.out.println("处理完成！");
//        System.out.println(cnFileNum+"个文件被处理。");
//        /**
//         * 英文文档预处理
//         */
//        String enFileDirectory = "src/test/resources/english";
//        File enFileFolder = new File(enFileDirectory);
//        if(!enFileFolder.exists()){
//            System.out.println("File doesn't exist!");
//            return;
//        }
//        int enFileNum = 0;
//        File[] enFiles = enFileFolder.listFiles();
//        System.out.println("处理中...");
//        for(File enFile : enFiles){
//            if(enFile.isFile()){
//                if(!new TextPreprocess().enProcess(enFile)){
//                    System.out.println(enFile.getPath()+"failed");
//                    continue;
//                }
//                enFileNum++;
//            }else if(enFile.isDirectory()){
//                System.out.println(enFile.getPath()+" is a directory.");
//            }
//        }
//        System.out.println("处理完成！");
//        System.out.println(enFileNum+"个文件被处理。");
        /**
         * 测试
         */
        new TextPreprocess().cnProcess(new File("src/test/resources/chinese/1.html"));
        new TextPreprocess().enProcess(new File("src/test/resources/english/Attributes.Name.html"));
    }
}

