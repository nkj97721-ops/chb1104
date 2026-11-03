package com.example.minimalbrowser;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
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
    private static final int BG=0xFF090B10, BAR=0xFF151A24, FIELD=0xFF222A38;
    private static final int FG=0xFFF2F5FA, MUTED=0xFF9AA6B8, ACCENT=0xFF8AB4F8;
    private static final String HOME="https://www.google.com";
    private WebView web;
    private EditText address;
    private ProgressBar progress;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildUi();
        configureWebView();
        if (state == null) web.loadUrl(HOME);
        else web.restoreState(state);
    }

    private void buildUi() {
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(BAR));
        progress.setVisibility(View.GONE);
        root.addView(progress,new LinearLayout.LayoutParams(-1,dp(2)));

        web=new WebView(this);
        web.setBackgroundColor(BG);
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1));

        // Bottom navigation and address bar
        LinearLayout bottom=new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setPadding(dp(8),dp(8),dp(8),dp(8));
        bottom.setBackgroundColor(BAR);

        LinearLayout controls=new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.setPadding(0,0,0,dp(7));
        addControl(controls,"‹",()->{ if(web.canGoBack()) web.goBack(); });
        addControl(controls,"›",()->{ if(web.canGoForward()) web.goForward(); });
        addControl(controls,"↻",()->web.reload());

        address=new EditText(this);
        address.setSingleLine(true);
        address.setTextSize(14);
        address.setTextColor(FG);
        address.setHintTextColor(MUTED);
        address.setHint("搜索 Google 或输入网址");
        address.setPadding(dp(14),0,dp(10),0);
        address.setSelectAllOnFocus(true);
        address.setImeOptions(EditorInfo.IME_ACTION_GO);
        address.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
        address.setBackground(round(FIELD,22));
        controls.addView(address,new LinearLayout.LayoutParams(0,dp(46),1));

        TextView go=new TextView(this);
        go.setText("→");
        go.setTextColor(ACCENT);
        go.setTextSize(22);
        go.setGravity(Gravity.CENTER);
        controls.addView(go,new LinearLayout.LayoutParams(dp(42),dp(46)));
        go.setOnClickListener(v->navigate());
        address.setOnEditorActionListener((v,action,event)->{
            if(action==EditorInfo.IME_ACTION_GO ||
                (event!=null && event.getKeyCode()==KeyEvent.KEYCODE_ENTER)) {
                navigate(); return true;
            }
            return false;
        });
        bottom.addView(controls);
        TextView hint=new TextView(this);
        hint.setText("GOOGLE  ·  PRIVATE DARK BROWSER");
        hint.setTextColor(MUTED);
        hint.setTextSize(9);
        hint.setGravity(Gravity.CENTER);
        hint.setLetterSpacing(0.12f);
        bottom.addView(hint,new LinearLayout.LayoutParams(-1,dp(15)));
        root.addView(bottom);
        setContentView(root);
    }

    private void addControl(LinearLayout row,String label,Runnable action) {
        TextView button=new TextView(this);
        button.setText(label);
        button.setTextSize(25);
        button.setTextColor(FG);
        button.setGravity(Gravity.CENTER);
        row.addView(button,new LinearLayout.LayoutParams(dp(42),dp(46)));
        button.setOnClickListener(v->action.run());
    }

    private void configureWebView() {
        WebSettings s=web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        if(android.os.Build.VERSION.SDK_INT>=29) s.setAlgorithmicDarkeningAllowed(true);
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView view,String url,android.graphics.Bitmap icon) {
                address.setText(url);
                progress.setVisibility(View.VISIBLE);
            }
            @Override public void onPageFinished(WebView view,String url) {
                address.setText(url);
                progress.setVisibility(View.GONE);
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
        String input=address.getText().toString().trim();
        if(input.isEmpty()) return;
        String target;
        if(input.matches("(?i)^https?://.*")) target=input;
        else if(input.matches("(?i)^[a-z0-9.-]+\\.[a-z]{2,}(:[0-9]+)?(/.*)?$"))
            target="https://"+input;
        else target="https://www.google.com/search?q="+android.net.Uri.encode(input);
        web.loadUrl(target);
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(address.getWindowToken(),0);
        address.clearFocus();
    }

    private GradientDrawable round(int color,int radius) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }
    private int dp(int value) {
        return (int)(value*getResources().getDisplayMetrics().density+0.5f);
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if(web!=null) web.saveState(out);
    }
    @Override public void onBackPressed() {
        if(web!=null&&web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}