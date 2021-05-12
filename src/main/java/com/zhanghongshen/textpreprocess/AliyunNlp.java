package com.zhanghongshen.textpreprocess;

/**
 * @author Zhang Hongshen
 * @description AliyunNLP Configuration
 * @date 2021/5/3
 */
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class AliyunNlp {
    public  static final int MAX_TEXT_LENGTH = 1024;
    private static final String HTTP_METHOD = "GET";
    /**
     * 阿里云NLP公共请求参数，最新参数值请查看网址https://help.aliyun.com/document_detail/177203.html
     */
    private final String format = "json";
    private final String version = "2020-06-29";
    private final String signatureMethod = "HMAC-SHA1";
    private final String signatureVersion = "1.0";
    private final String signatureNonce = UUID.randomUUID().toString();
    private String timestamp = "";
    private String signature = "";
    /**
     * 个人参数
     */
    private String resourceOwnerAccount = "";
    private String AccessKeyId = "";
    private String AccessKeySecret = "";
    /**
     * 接口请求参数
     */
    private final String action = "GetWsChGeneral";
    private final String serviceCode = "alinlp";
    private final String tokenizerld = "GENERAL_CHN";
    private final String outType = "1";
    /**
     * 请求aliyunNLP的url
     */
    private StringBuffer url = new StringBuffer("http://alinlp.cn-hangzhou.aliyuncs.com/");
    public AliyunNlp(String text){
        //阿里云Nlp规定最大查询字节：1024
        text = text.length() >= MAX_TEXT_LENGTH ? text.substring(0,MAX_TEXT_LENGTH) : text;
        /**
         * 获取0时区UTC时间（ISO8601标准）
         */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(new SimpleTimeZone(0,"GMT"));
        timestamp = sdf.format(Calendar.getInstance().getTime());
        //参数配置
        HashMap<String,String> param = new HashMap<>();
        param.put("Format",format);
        param.put("Version",version);
        param.put("AccessKeyId",AccessKeyId);
        param.put("SignatureMethod",signatureMethod);
        param.put("Timestamp",timestamp);
        param.put("SignatureVersion",signatureVersion);
        param.put("SignatureNonce", signatureNonce);
        param.put("ResourceOwnerAccount", resourceOwnerAccount);
        param.put("Action",action);
        param.put("ServiceCode", serviceCode);
        param.put("Text",text);
        param.put("Tokenizerld",tokenizerld);
        param.put("OutType",outType);

        /**
         * 按字典顺序对参数排序
         */
        if(param.containsKey("Signature")){
            param.remove("Signature");
        }
        TreeMap<String,String> sortedParam = new TreeMap<>();
        sortedParam.putAll(param);
        /**
         * 生成规范化请求字符串canonicalizedQueryString
         */
        StringBuilder canonicalizedQueryString = new StringBuilder();
        for(Map.Entry<String,String> entry : sortedParam.entrySet()){
            canonicalizedQueryString.append("&")
                    .append(percentEncode(entry.getKey()))
                    .append("=")
                    .append(percentEncode(entry.getValue()));
        }
        /**
         * 生成待签名字符串
         */
        StringBuffer stringToSign = new StringBuffer();
        stringToSign.append(HTTP_METHOD)
                .append("&")
                .append(percentEncode("/"))
                .append("&")
                .append(percentEncode(canonicalizedQueryString.substring(1)));
        /**
         * 采用HMACSHA1哈希加密算法计算Signature
         */
        signature = getHmacSign(stringToSign.toString(),AccessKeySecret + "&",StandardCharsets.UTF_8,"HmacSHA1");
        url.append("?")
                .append(canonicalizedQueryString.substring(1))
                .append("&Signature=").append(signature);
    }
    public String getUrl() {
        return url.toString();
    }

    /**
     * @return Json字符串
     */
    private String getJsonString(){
        StringBuffer json = new StringBuffer();
        try{
            URL url = new URL(this.url.toString());
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String s;
            while((s = bufferedReader.readLine()) != null){
                json.append(s);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * @return List<String> 分词结果
     */
    public List<String> getAliyunNlpResult(){
        //分词结果储存
        List<String> words = new ArrayList<>();
        //获取json格式返回
        String jsonString = getJsonString();
        //处理json结果获得分词结果
        JSONObject object = JSONObject.parseObject(jsonString);
        JSONArray array = object.getJSONObject("Data").getJSONArray("result");
        for(int i = 0; i < array.size(); i++){
            words.add(array.getJSONObject(i).get("word").toString().trim());
        }
        return words;
    }

    /**
     * @param value 待编码字的符串
     * @return 编码后的字符串
     */
    private String percentEncode(String value){
        return value != null ? URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~") : null;
    }

    /**
     * @param stringToSign 待加密字符串
     * @param encryptKey 密钥
     * @param charset 字符集
     * @param hmacAlgorithm hmac算法名称
     * @return String 签名后的字符串
     */
    private String getHmacSign(String stringToSign, String encryptKey, Charset charset, String hmacAlgorithm) {
        byte[] signData = new byte[0];
        try{
            Mac mac = Mac.getInstance(hmacAlgorithm);
            mac.init(new SecretKeySpec(encryptKey.getBytes(charset), hmacAlgorithm));
            signData = mac.doFinal(stringToSign.getBytes(charset));
        }catch (Exception e){
            e.printStackTrace();
        }
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(signData);
    }
}

