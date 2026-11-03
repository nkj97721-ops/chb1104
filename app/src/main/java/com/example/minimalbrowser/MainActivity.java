package com.example.minimalbrowser;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private static final String HOME = "https://www.google.com";
    private WebView webView;
    private EditText addressBar;
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);
        window.getDecorView().setSystemUiVisibility(0);
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);

        // The Android status bar is row one.
        // The address row expands so both top rows occupy about half the screen.
        addressBar = new EditText(this);
        addressBar.setSingleLine(true);
        addressBar.setTextSize(15);
        addressBar.setTextColor(Color.WHITE);
        addressBar.setHintTextColor(0xFF888888);
        addressBar.setHint("搜索 Google 或输入网址");
        addressBar.setPadding(dp(14), 0, dp(14), 0);
        addressBar.setGravity(Gravity.CENTER_VERTICAL);
        addressBar.setImeOptions(EditorInfo.IME_ACTION_GO);
        addressBar.setBackgroundColor(Color.BLACK);
        root.addView(addressBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url,
                                      android.graphics.Bitmap favicon) {
                addressBar.setText(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                addressBar.setText(url);
            }
        });
        root.addView(webView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        addressBar.setOnEditorActionListener((view, actionId, event) -> {
            boolean enterPressed = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_UP;
            if (actionId == EditorInfo.IME_ACTION_GO || enterPressed) {
                openAddress(addressBar.getText().toString());
                return true;
            }
            return false;
        });

        setContentView(root);
        // Measure after Android applies system status/navigation bar insets.
        root.post(() -> {
            int halfHeight = root.getHeight() / 2;
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) addressBar.getLayoutParams();
            params.height = halfHeight;
            addressBar.setLayoutParams(params);
        });

        if (savedInstanceState == null || webView.restoreState(savedInstanceState) == null) {
            webView.loadUrl(HOME);
        }
    }

    private void openAddress(String input) {
        String value = input == null ? "" : input.trim();
        if (value.isEmpty()) return;

        String url;
        if (value.matches("(?i)^https?://.*")) {
            url = value;
        } else if (value.matches("(?i)^[a-z0-9.-]+\\.[a-z]{2,}(:[0-9]+)?(/.*)?$")) {
            url = "https://" + value;
        } else {
            url = "https://www.google.com/search?q="
                    + android.net.Uri.encode(value);
        }

        webView.loadUrl(url);
        InputMethodManager keyboard = (InputMethodManager)
                getSystemService(INPUT_METHOD_SERVICE);
        if (keyboard != null) {
            keyboard.hideSoftInputFromWindow(addressBar.getWindowToken(), 0);
        }
        addressBar.clearFocus();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) webView.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}