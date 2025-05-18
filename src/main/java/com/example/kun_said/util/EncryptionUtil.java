package com.example.kun_said.util;

import android.util.LruCache;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 坤曰解密工具类
 */
public class EncryptionUtil {
    private static final String TAG = "EncryptionUtil";
    
    // "只因你太美"五个字及其二进制表示（示例）
    private static final String[] KUN_CHARS = {"只", "因", "你", "太", "美"};
    private static final String[] KUN_BINARY = {
            "11100111 10101001", // 只
            "10111001 10000101", // 因 
            "10101101 10000001", // 你
            "10111101 10010001", // 太
            "10110011 10001101"  // 美
    };
    
    // 使用静态HashMap存储对应关系，确保应用生命周期内可用
    private static final Map<String, String> encryptionMap = new HashMap<>();

    /**
     * 加密文本
     * @param originalContent 原始内容
     * @param key 加密凭证
     * @return 加密后的内容，格式为基于"只因你太美"五个字的加密文本，最后加上加密凭证
     */
    public static String encrypt(String originalContent, String key) {
        try {
            // 使用SHA-256哈希算法加密原文和凭证组合
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encryptedHash = digest.digest((originalContent + key).getBytes(StandardCharsets.UTF_8));
            
            // 将哈希值转为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : encryptedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            String hashText = hexString.toString();
            
            // 基于哈希值和"只因你太美"进行编码
            StringBuilder encodedText = new StringBuilder();
            
            // 限制哈希值最大使用长度，确保不会过长
            int maxHashLength = Math.min(hashText.length(), 60);
            
            // 对每个哈希字符进行编码
            for (int i = 0; i < maxHashLength; i++) {
                // 根据哈希值的每个字符选择一个"只因你太美"中的字符
                int charIndex = (hashText.charAt(i) & 0x7) % 5; // 取模确保在0-4范围内
                encodedText.append(KUN_CHARS[charIndex]);
            }
            
            // 对结果进行分组，每5个字符一组
            StringBuilder formattedResult = new StringBuilder();
            String resultText = encodedText.toString();
            
            for (int i = 0; i < resultText.length(); i++) {
                formattedResult.append(resultText.charAt(i));
                if ((i + 1) % 5 == 0 && i < resultText.length() - 1) {
                    formattedResult.append(" ");
                }
            }
            
            // 添加加密凭证
            String encryptedResult = formattedResult.toString() + "，" + key;
            
            // 生成用于存储的键
            String encryptionKey = formattedResult.toString().replaceAll("\\s", "") + ":" + key;
            
            // 存储原始内容，用于后续解密
            encryptionMap.put(encryptionKey, originalContent);
            Log.d(TAG, "保存映射: " + encryptionKey + " -> " + originalContent);
            
            // 添加前缀
            return "坤曰：只因你太美，你我美积极，" + encryptedResult;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "加密失败：" + e.getMessage();
        }
    }

    /**
     * 解密文本
     * @param encryptedContent 加密内容
     * @param key 加密凭证
     * @return 解密后的原文
     */
    public static String decrypt(String encryptedContent, String key) {
        Log.d(TAG, "尝试解密: " + encryptedContent);
        
        // 提取加密部分和凭证
        Pattern pattern = Pattern.compile("坤曰：只因你太美，你我美积极，(.*?)，(.*)$");
        Matcher matcher = pattern.matcher(encryptedContent);
        
        if (matcher.find()) {
            String encryptedPart = matcher.group(1);
            String extractedKey = matcher.group(2);
            
            Log.d(TAG, "提取的加密部分: " + encryptedPart);
            Log.d(TAG, "提取的凭证: " + extractedKey);
            
            // 验证凭证是否匹配
            if (!extractedKey.equals(key)) {
                return "无法解密：凭证不匹配";
            }
            
            // 移除空格
            String cleanEncryptedPart = encryptedPart.replaceAll("\\s", "");
            
            // 从HashMap中直接查询
            String lookupKey = cleanEncryptedPart + ":" + key;
            Log.d(TAG, "查询键: " + lookupKey);
            
            if (encryptionMap.containsKey(lookupKey)) {
                String originalContent = encryptionMap.get(lookupKey);
                Log.d(TAG, "找到原始内容: " + originalContent);
                return originalContent;
            } else {
                Log.d(TAG, "未找到匹配的原始内容");
                // 查看所有键
                for (String mapKey : encryptionMap.keySet()) {
                    Log.d(TAG, "现有键: " + mapKey);
                }
            }
            
            // 如果无法找到原始内容，返回默认消息
            return "无法解密：未找到原始内容（当前会话中可能未加密过此内容）";
        }
        
        return "无法解密：格式不正确";
    }

    /**
     * 从加密文本中提取加密凭证
     * @param encryptedContent 加密内容
     * @return 加密凭证，如果格式不匹配则返回null
     */
    public static String extractKey(String encryptedContent) {
        Pattern pattern = Pattern.compile("坤曰：只因你太美，你我美积极，.*?，(.*)$");
        Matcher matcher = pattern.matcher(encryptedContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
} 