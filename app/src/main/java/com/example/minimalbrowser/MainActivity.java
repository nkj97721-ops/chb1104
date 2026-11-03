package com.example.minimalbrowser;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.graphics.Color;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;

public class MainActivity extends Activity {
    private WebView web;
    private EditText address;
    private final String HOME = "https://www.google.com";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(Color.WHITE);

        LinearLayout bar = new LinearLayout(this);
        bar.setPadding(8, 8, 8, 8);
        bar.setGravity(android.view.Gravity.CENTER_VERTICAL);

        Button back = button("‹"), forward = button("›"), refresh = button("↻"), home = button("⌂");
        address = new EditText(this);
        address.setSingleLine(true);
        address.setTextSize(14);
        address.setHint("输入网址或搜索内容");
        address.setImeOptions(EditorInfo.IME_ACTION_GO);
        address.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(0, 44, 1);
        bar.addView(back); bar.addView(forward); bar.addView(address, editParams);
        bar.addView(refresh); bar.addView(home);
        page.addView(bar);

        web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setSupportZoom(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setDisplayZoomControls(false);
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView v, String url, android.graphics.Bitmap icon) { address.setText(url); }
            @Override public void onPageFinished(WebView v, String url) { address.setText(url); }
        });
        web.setWebChromeClient(new WebChromeClient());
        page.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(page);

        address.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String s = address.getText().toString().trim();
                if (!s.isEmpty()) {
                    String url = s.matches("(?i)^https?://.*") ? s :
                        (s.matches("(?i)^[a-z0-9.-]+\\.[a-z]{2,}(:[0-9]+)?(/.*)?$") ? "https://" + s :
                        "https://www.google.com/search?q=" + android.net.Uri.encode(s));
                    web.loadUrl(url);
                    ((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(address.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
        back.setOnClickListener(v -> { if (web.canGoBack()) web.goBack(); });
        forward.setOnClickListener(v -> { if (web.canGoForward()) web.goForward(); });
        refresh.setOnClickListener(v -> web.reload());
        home.setOnClickListener(v -> web.loadUrl(HOME));
        web.loadUrl(HOME);
    }
    private Button button(String text) {
        Button b = new Button(this); b.setText(text); b.setTextSize(18);
        b.setMinWidth(40); b.setPadding(4, 0, 4, 0); return b;
    }
    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
