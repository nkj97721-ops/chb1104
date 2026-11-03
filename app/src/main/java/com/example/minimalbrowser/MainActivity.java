package com.example.minimalbrowser;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(15, 17, 21);
    private static final int PANEL = Color.rgb(27, 30, 37);
    private static final int TEXT = Color.rgb(238, 240, 245);
    private static final int MUTED = Color.rgb(155, 163, 178);
    private static final int ACCENT = Color.rgb(130, 170, 255);

    private WebView web;
    private EditText address;
    private ProgressBar progress;
    private Button modeButton;
    private boolean darkPages = true;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(8), dp(9), dp(8), dp(8));
        top.setBackgroundColor(BG);

        Button back = navButton("‹");
        Button forward = navButton("›");
        Button refresh = navButton("↻");
        Button home = navButton("⌂");
        modeButton = navButton("☀");
        modeButton.setTextSize(18);

        address = new EditText(this);
        address.setSingleLine(true);
        address.setTextSize(14);
        address.setTextColor(TEXT);
        address.setHintTextColor(MUTED);
        address.setHint("搜索或输入网址");
        address.setPadding(dp(14), 0, dp(10), 0);
        address.setBackground(roundBackground(PANEL, dp(18)));
        address.setImeOptions(EditorInfo.IME_ACTION_GO);
        address.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);

        LinearLayout.LayoutParams addressParams = new LinearLayout.LayoutParams(0, dp(46), 1);
        addressParams.setMargins(dp(4), 0, dp(4), 0);
        top.addView(back);
        top.addView(forward);
        top.addView(address, addressParams);
        top.addView(refresh);
        top.addView(home);
        top.addView(modeButton);
        root.addView(top);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(PANEL));
        root.addView(progress, new LinearLayout.LayoutParams(-1, dp(2)));

        web = new WebView(this);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        applyPageTheme();
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap icon) {
                address.setText(url);
            }
            @Override public void onPageFinished(WebView view, String url) {
                address.setText(url);
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int value) {
                progress.setProgress(value);
                progress.setVisibility(value >= 100 ? android.view.View.GONE : android.view.View.VISIBLE);
            }
        });
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        address.setOnEditorActionListener((view, action, event) -> {
            boolean enter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            if (action == EditorInfo.IME_ACTION_GO || enter) {
                openInput();
                return true;
            }
            return false;
        });
        back.setOnClickListener(v -> { if (web.canGoBack()) web.goBack(); });
        forward.setOnClickListener(v -> { if (web.canGoForward()) web.goForward(); });
        refresh.setOnClickListener(v -> web.reload());
        home.setOnClickListener(v -> web.loadUrl("https://www.google.com"));
        modeButton.setOnClickListener(v -> {
            darkPages = !darkPages;
            applyPageTheme();
            modeButton.setText(darkPages ? "☀" : "☾");
            web.reload();
        });
        modeButton.setText("☀");
        web.loadUrl("https://www.google.com");
    }

    private void applyPageTheme() {
        if (web == null) return;
        WebSettings settings = web.getSettings();
        if (Build.VERSION.SDK_INT >= 33) {
            settings.setAlgorithmicDarkeningAllowed(darkPages);
        } else if (Build.VERSION.SDK_INT >= 29) {
            settings.setForceDark(darkPages ? WebSettings.FORCE_DARK_ON : WebSettings.FORCE_DARK_OFF);
        }
        web.setBackgroundColor(darkPages ? BG : Color.WHITE);
    }

    private void openInput() {
        String value = address.getText().toString().trim();
        if (value.isEmpty()) return;
        String url;
        if (value.matches("(?i)^https?://.*")) {
            url = value;
        } else if (value.matches("(?i)^[a-z0-9.-]+\\.[a-z]{2,}(:[0-9]+)?(/.*)?$")) {
            url = "https://" + value;
        } else {
            url = "https://www.google.com/search?q=" + android.net.Uri.encode(value);
        }
        web.loadUrl(url);
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(address.getWindowToken(), 0);
        address.clearFocus();
    }

    private Button navButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(23);
        button.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        button.setTextColor(TEXT);
        button.setAllCaps(false);
        button.setMinWidth(dp(36));
        button.setMinimumWidth(dp(36));
        button.setMinHeight(dp(44));
        button.setPadding(0, 0, 0, 0);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(BG));
        return button;
    }

    private android.graphics.drawable.GradientDrawable roundBackground(int color, int radius) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        return shape;
    }

    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}
