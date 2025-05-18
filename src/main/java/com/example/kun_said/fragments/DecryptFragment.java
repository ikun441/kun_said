package com.example.kun_said.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.kun_said.R;
import com.example.kun_said.util.EncryptionUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class DecryptFragment extends Fragment {
    private static final String TAG = "DecryptFragment";
    private TextInputEditText etEncryptedContent;
    private TextInputEditText etDecryptKey;
    private Button btnAutoDetect;
    private Button btnDecrypt;
    private Button btnCopy;
    private TextView tvDecryptProcessTitle;
    private TextView tvDecryptProcess;
    private TextView tvResultTitle;
    private TextView tvResult;
    private Handler handler;
    private boolean animationEnabled = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_decrypt, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPreferences();
        setupListeners();
    }

    private void initViews(View view) {
        etEncryptedContent = view.findViewById(R.id.et_encrypted_content);
        etDecryptKey = view.findViewById(R.id.et_decrypt_key);
        btnAutoDetect = view.findViewById(R.id.btn_auto_detect);
        btnDecrypt = view.findViewById(R.id.btn_decrypt);
        btnCopy = view.findViewById(R.id.btn_copy);
        tvDecryptProcessTitle = view.findViewById(R.id.tv_decrypt_process_title);
        tvDecryptProcess = view.findViewById(R.id.tv_decrypt_process);
        tvResultTitle = view.findViewById(R.id.tv_result_title);
        tvResult = view.findViewById(R.id.tv_result);
        handler = new Handler(Looper.getMainLooper());
    }

    private void loadPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        animationEnabled = prefs.getBoolean("animation_enabled", true);
    }

    private void setupListeners() {
        btnAutoDetect.setOnClickListener(v -> autoDetectKey());
        btnDecrypt.setOnClickListener(v -> startDecryption());
        btnCopy.setOnClickListener(v -> copyResultToClipboard());
    }

    private void autoDetectKey() {
        String encryptedContent = Objects.requireNonNull(etEncryptedContent.getText()).toString().trim();
        if (TextUtils.isEmpty(encryptedContent)) {
            etEncryptedContent.setError("请输入需要解密的内容");
            return;
        }

        // 使用工具类提取加密凭证
        String key = EncryptionUtil.extractKey(encryptedContent);
        Log.d(TAG, "自动检测到的凭证: " + key);
        
        if (key != null) {
            etDecryptKey.setText(key);
            Toast.makeText(requireContext(), "已自动识别加密凭证", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "无法识别加密凭证，请手动输入", Toast.LENGTH_SHORT).show();
        }
    }

    private void startDecryption() {
        String encryptedContent = Objects.requireNonNull(etEncryptedContent.getText()).toString().trim();
        String decryptKey = Objects.requireNonNull(etDecryptKey.getText()).toString().trim();

        if (TextUtils.isEmpty(encryptedContent)) {
            etEncryptedContent.setError("请输入需要解密的内容");
            return;
        }

        if (TextUtils.isEmpty(decryptKey)) {
            etDecryptKey.setError("请输入加密凭证");
            return;
        }

        Log.d(TAG, "开始解密: 内容=" + encryptedContent + ", 凭证=" + decryptKey);
        
        // 执行解密
        if (animationEnabled) {
            showDecryptionAnimation(encryptedContent, decryptKey);
        } else {
            String decryptedContent = EncryptionUtil.decrypt(encryptedContent, decryptKey);
            Log.d(TAG, "解密结果: " + decryptedContent);
            showResult(decryptedContent);
        }
    }

    private void showDecryptionAnimation(String content, String key) {
        // 显示解密过程
        tvDecryptProcessTitle.setVisibility(View.VISIBLE);
        tvDecryptProcess.setVisibility(View.VISIBLE);
        tvDecryptProcess.setText("");

        final String[] steps = {
                "1. 读取加密内容: " + content,
                "2. 提取加密格式...",
                "3. 验证格式是否符合'坤曰：只因你太美，你我美积极，...，凭证'",
                "4. 提取加密文本部分（由'只因你太美'组成的文本）...",
                "5. 提取加密凭证: " + key,
                "6. 验证凭证是否匹配...",
                "7. 分析'只因你太美'编码模式...",
                "8. 字符'只': 11100111 10101001",
                "9. 字符'因': 10111001 10000101",
                "10. 字符'你': 10101101 10000001",
                "11. 字符'太': 10111101 10010001",
                "12. 字符'美': 10110011 10001101",
                "13. 尝试根据'只因你太美'编码还原SHA-256哈希值...",
                "14. 查找对应的原始内容...",
                "15. 解密完成！"
        };

        for (int i = 0; i < steps.length; i++) {
            final int stepIndex = i;
            handler.postDelayed(() -> {
                tvDecryptProcess.append(steps[stepIndex] + "\n");
                // 最后一步完成后显示结果
                if (stepIndex == steps.length - 1) {
                    String decryptedContent = EncryptionUtil.decrypt(content, key);
                    Log.d(TAG, "动画结束后解密结果: " + decryptedContent);
                    showResult(decryptedContent);
                }
            }, i * 300); // 每步延迟300毫秒
        }
    }

    private void showResult(String result) {
        tvResultTitle.setVisibility(View.VISIBLE);
        tvResult.setVisibility(View.VISIBLE);
        tvResult.setText(result);
        btnCopy.setVisibility(View.VISIBLE);
        Log.d(TAG, "显示解密结果: " + result);
    }

    private void copyResultToClipboard() {
        String result = tvResult.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("解密结果", result);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(requireContext(), "已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }
} 