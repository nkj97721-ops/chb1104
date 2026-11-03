package com.example.minimalbrowser;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import android.text.InputType;

public class MainActivity extends Activity {
    private static final int BLACK=Color.BLACK, WHITE=Color.WHITE, GREY=0xFF888888;
    private static final String HOME="https://www.google.com";
    private WebView web;
    private EditText address;
    private ProgressBar progress;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BLACK);
        getWindow().setNavigationBarColor(BLACK);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildUi();
        configureWebView();
        if(state==null) web.loadUrl(HOME); else web.restoreState(state);
    }

    private void buildUi() {
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BLACK);

        // Row 1: native Android status bar. Row 2: browser address bar.
        LinearLayout addressRow=new LinearLayout(this);
        addressRow.setOrientation(LinearLayout.HORIZONTAL);
        addressRow.setGravity(Gravity.CENTER_VERTICAL);
        addressRow.setPadding(dp(8),dp(6),dp(8),dp(6));
        addressRow.setBackgroundColor(BLACK);

        TextView back=control("‹");
        addressRow.addView(back,new LinearLayout.LayoutParams(dp(38),dp(44)));
        back.setOnClickListener(v->{if(web.canGoBack()) web.goBack();});

        address=new EditText(this);
        address.setSingleLine(true);
        address.setTextSize(14);
        address.setTextColor(WHITE);
        address.setHintTextColor(GREY);
        address.setHint("Google 搜索 / 输入网址");
        address.setPadding(dp(12),0,dp(8),0);
        address.setImeOptions(EditorInfo.IME_ACTION_GO);
        address.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
        address.setBackground(round(0xFF171717,dp(24)));
        addressRow.addView(address,new LinearLayout.LayoutParams(0,dp(44),1));

        TextView go=control("↵");
        addressRow.addView(go,new LinearLayout.LayoutParams(dp(38),dp(44)));
        go.setOnClickListener(v->navigate());
        TextView reload=control("↻");
        addressRow.addView(reload,new LinearLayout.LayoutParams(dp(38),dp(44)));
        reload.setOnClickListener(v->web.reload());
        address.setOnEditorActionListener((v,action,event)->{
            if(action==EditorInfo.IME_ACTION_GO ||
                (event!=null&&event.getKeyCode()==KeyEvent.KEYCODE_ENTER)) {
                navigate(); return true;
            }
            return false;
        });
        root.addView(addressRow);

        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(WHITE));
        progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(BLACK));
        progress.setVisibility(View.GONE);
        root.addView(progress,new LinearLayout.LayoutParams(-1,dp(2)));

        web=new WebView(this);
        web.setBackgroundColor(BLACK);
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }

    private TextView control(String label) {
        TextView t=new TextView(this);
        t.setText(label); t.setTextSize(24); t.setTextColor(WHITE);
        t.setGravity(Gravity.CENTER); return t;
    }

    private void configureWebView() {
        WebSettings s=web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        if(android.os.Build.VERSION.SDK_INT>=29) s.setAlgorithmicDarkeningAllowed(true);
        else if(android.os.Build.VERSION.SDK_INT>=21) web.setForceDark(WebSettings.FORCE_DARK_ON);
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView view,String url,android.graphics.Bitmap icon) {
                address.setText(url); progress.setVisibility(View.VISIBLE);
            }
            @Override public void onPageFinished(WebView view,String url) {
                address.setText(url); progress.setVisibility(View.GONE);
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view,int value) {
                progress.setProgress(value);
                progress.setVisibility(value>=100?View.GONE:View.VISIBLE);
            }
        });
    }

    private void navigate() {
        String value=address.getText().toString().trim();
        if(value.isEmpty()) return;
        String url;
        if(value.matches("(?i)^https?://.*")) url=value;
        else if(value.matches("(?i)^[a-z0-9.-]+\\.[a-z]{2,}(:[0-9]+)?(/.*)?$")) url="https://"+value;
        else url="https://www.google.com/search?q="+android.net.Uri.encode(value);
        web.loadUrl(url);
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(address.getWindowToken(),0);
        address.clearFocus();
    }

    private GradientDrawable round(int color,int radius) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radius); return d;
    }
    private int dp(int n) {
        return (int)(n*getResources().getDisplayMetrics().density+0.5f);
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if(web!=null) web.saveState(out);
    }
    @Override public void onBackPressed() {
        if(web!=null&&web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}