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

public class EncryptFragment extends Fragment {
    private static final String TAG = "EncryptFragment";
    private TextInputEditText etOriginalContent;
    private TextInputEditText etEncryptKey;
    private Button btnEncrypt;
    private Button btnCopy;
    private TextView tvEncryptProcessTitle;
    private TextView tvEncryptProcess;
    private TextView tvResultTitle;
    private TextView tvResult;
    private Handler handler;
    private boolean animationEnabled = true;
    private String lastEncryptedContent = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_encrypt, container, false);
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
        etOriginalContent = view.findViewById(R.id.et_original_content);
        etEncryptKey = view.findViewById(R.id.et_encrypt_key);
        btnEncrypt = view.findViewById(R.id.btn_encrypt);
        btnCopy = view.findViewById(R.id.btn_copy);
        tvEncryptProcessTitle = view.findViewById(R.id.tv_encrypt_process_title);
        tvEncryptProcess = view.findViewById(R.id.tv_encrypt_process);
        tvResultTitle = view.findViewById(R.id.tv_result_title);
        tvResult = view.findViewById(R.id.tv_result);
        handler = new Handler(Looper.getMainLooper());
    }

    private void loadPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        animationEnabled = prefs.getBoolean("animation_enabled", true);
    }

    private void setupListeners() {
        btnEncrypt.setOnClickListener(v -> startEncryption());
        btnCopy.setOnClickListener(v -> copyResultToClipboard());
    }

    private void startEncryption() {
        String originalContent = Objects.requireNonNull(etOriginalContent.getText()).toString().trim();
        String encryptKey = Objects.requireNonNull(etEncryptKey.getText()).toString().trim();

        if (TextUtils.isEmpty(originalContent)) {
            etOriginalContent.setError("请输入需要加密的内容");
            return;
        }

        if (TextUtils.isEmpty(encryptKey)) {
            etEncryptKey.setError("请输入加密凭证");
            return;
        }

        Log.d(TAG, "开始加密: 原文=" + originalContent + ", 凭证=" + encryptKey);

        // 执行加密
        if (animationEnabled) {
            showEncryptionAnimation(originalContent, encryptKey);
        } else {
            String encryptedContent = EncryptionUtil.encrypt(originalContent, encryptKey);
            Log.d(TAG, "加密结果: " + encryptedContent);
            lastEncryptedContent = encryptedContent;
            showResult(encryptedContent);
        }
    }

    private void showEncryptionAnimation(String content, String key) {
        // 显示加密过程
        tvEncryptProcessTitle.setVisibility(View.VISIBLE);
        tvEncryptProcess.setVisibility(View.VISIBLE);
        tvEncryptProcess.setText("");

        final String[] steps = {
                "1. 读取文本内容: " + content,
                "2. 使用SHA-256算法对原文和凭证 '" + key + "' 进行哈希...",
                "3. 将'只因你太美'五个字设定为编码基础...",
                "4. 字符'只': 11100111 10101001",
                "5. 字符'因': 10111001 10000101",
                "6. 字符'你': 10101101 10000001",
                "7. 字符'太': 10111101 10010001",
                "8. 字符'美': 10110011 10001101",
                "9. 根据SHA-256哈希值将每个字符映射为'只因你太美'中的一个字...",
                "10. 生成基于'只因你太美'的加密文本并按五字分组...",
                "11. 添加加密凭证...",
                "12. 加密完成！"
        };

        for (int i = 0; i < steps.length; i++) {
            final int stepIndex = i;
            handler.postDelayed(() -> {
                tvEncryptProcess.append(steps[stepIndex] + "\n");
                // 最后一步完成后显示结果
                if (stepIndex == steps.length - 1) {
                    String encryptedContent = EncryptionUtil.encrypt(content, key);
                    Log.d(TAG, "动画结束后加密结果: " + encryptedContent);
                    lastEncryptedContent = encryptedContent;
                    showResult(encryptedContent);
                }
            }, i * 300); // 每步延迟300毫秒
        }
    }

    private void showResult(String result) {
        tvResultTitle.setVisibility(View.VISIBLE);
        tvResult.setVisibility(View.VISIBLE);
        tvResult.setText(result);
        btnCopy.setVisibility(View.VISIBLE);
        Log.d(TAG, "显示加密结果: " + result);
    }

    private void copyResultToClipboard() {
        String result = tvResult.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("加密结果", result);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(requireContext(), "已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }
} 