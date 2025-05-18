package com.example.kun_said.fragments;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.kun_said.R;

public class SettingsFragment extends Fragment {

    private Switch switchAnimation;
    private TextView tvPrivacyPolicy;
    private TextView tvTermsOfService;
    private TextView tvKunProtocol;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        setupListeners();
        loadSettings();
    }

    private void initViews(View view) {
        switchAnimation = view.findViewById(R.id.switch_animation);
        tvPrivacyPolicy = view.findViewById(R.id.tv_privacy_policy);
        tvTermsOfService = view.findViewById(R.id.tv_terms_of_service);
        tvKunProtocol = view.findViewById(R.id.tv_kun_protocol);
    }

    private void setupListeners() {
        switchAnimation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveAnimationSetting(isChecked);
        });

        tvPrivacyPolicy.setOnClickListener(v -> showPrivacyPolicy());
        tvTermsOfService.setOnClickListener(v -> showTermsOfService());
        tvKunProtocol.setOnClickListener(v -> showKunProtocol());
    }

    private void loadSettings() {
        boolean animationEnabled = sharedPreferences.getBoolean("animation_enabled", true);
        switchAnimation.setChecked(animationEnabled);
    }

    private void saveAnimationSetting(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("animation_enabled", enabled);
        editor.apply();
    }

    private void showPrivacyPolicy() {
        new AlertDialog.Builder(requireContext())
                .setTitle("隐私条款")
                .setMessage("本应用不会收集用户的个人信息，所有加密和解密操作均在本地完成，不会上传到服务器。\n\n" +
                        "我们不会收集您的个人信息，包括但不限于您的姓名、电话号码、地址等。\n\n" +
                        "我们不会将您的信息分享给任何第三方。")
                .setPositiveButton("我已了解", null)
                .show();
    }

    private void showTermsOfService() {
        new AlertDialog.Builder(requireContext())
                .setTitle("使用协议")
                .setMessage("欢迎使用坤曰！\n\n" +
                        "使用本应用表示您同意以下条款：\n\n" +
                        "1. 本应用仅供娱乐使用，不得用于任何违法或不道德的用途。\n" +
                        "2. 用户对使用本应用进行的加密和解密操作负全部责任。\n" +
                        "3. 我们保留随时更改服务条款的权利，更改将在公布后立即生效。")
                .setPositiveButton("我已了解", null)
                .show();
    }

    private void showKunProtocol() {
        new AlertDialog.Builder(requireContext())
                .setTitle("坤协议（加密解密原理）")
                .setMessage("坤曰基于'只因你太美'原理，是一种娱乐性质的加密方式。\n\n" +
                        "加密原理：\n" +
                        "1. 将原文和用户提供的凭证结合，使用SHA-256算法生成哈希值\n" +
                        "2. 将'只因你太美'五个字转换为固定的二进制分组\n" +
                        "3. 根据二进制分组和哈希值生成最终的加密文本\n" +
                        "4. 最终格式为：'坤曰：只因你太美，你我美积极，加密凭证'\n\n" +
                        "注意：这是一种娱乐性质的加密，安全性有限，请勿用于加密敏感数据。")
                .setPositiveButton("我已了解", null)
                .show();
    }
} 