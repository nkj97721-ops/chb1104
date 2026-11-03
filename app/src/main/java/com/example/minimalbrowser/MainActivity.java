package com.example.minimalbrowser;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.webkit.*;
import android.widget.*;
import android.text.InputType;

public class MainActivity extends Activity {
    private static final int BG=0xFF101114, PANEL=0xFF1B1D22, FIELD=0xFF252830;
    private static final int TEXT=0xFFF2F3F5, MUTED=0xFF9BA1AD, ACCENT=0xFF8AB4F8;
    private WebView web;
    private EditText address;
    private ProgressBar progress;
    private TextView title;
    private LinearLayout root;
    private final String HOME="https://www.google.com";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildUi();
        configureWeb();
        if (state==null) web.loadUrl(HOME); else web.restoreState(state);
    }

    private void buildUi() {
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        LinearLayout top=new LinearLayout(this); top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(10),dp(10),dp(10),dp(8));
        top.setBackgroundColor(BG);

        TextView mark=new TextView(this); mark.setText("◉"); mark.setTextColor(ACCENT);
        mark.setTextSize(23); mark.setGravity(Gravity.CENTER); mark.setWidth(dp(34));
        top.addView(mark);

        address=new EditText(this); address.setSingleLine(true); address.setTextSize(14);
        address.setTextColor(TEXT); address.setHintTextColor(MUTED); address.setHint("搜索或输入网址");
        address.setPadding(dp(13),0,dp(10),0); address.setSelectAllOnFocus(true);
        address.setImeOptions(EditorInfo.IME_ACTION_GO);
        address.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
        address.setBackground(round(FIELD,24));
        top.addView(address,new LinearLayout.LayoutParams(0,dp(46),1));

        TextView go=action("前往"); go.setTextColor(ACCENT);
        top.addView(go,new LinearLayout.LayoutParams(dp(54),dp(44)));
        go.setOnClickListener(v->navigate(address.getText().toString()));
        address.setOnEditorActionListener((v,a,e)->{
            if(a==EditorInfo.IME_ACTION_GO || (e!=null && e.getKeyCode()==KeyEvent.KEYCODE_ENTER)) {
                navigate(address.getText().toString()); return true;
            } return false;
        });
        root.addView(top);

        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100); progress.setProgressTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(PANEL));
        progress.setVisibility(ProgressBar.GONE);
        root.addView(progress,new LinearLayout.LayoutParams(-1,dp(2)));

        web=new WebView(this);
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout nav=new LinearLayout(this); nav.setGravity(Gravity.CENTER);
        nav.setBackgroundColor(PANEL); nav.setPadding(dp(5),dp(5),dp(5),dp(5));
        String[] labels={"‹","›","⌂","↻","＋"};
        for(int i=0;i<labels.length;i++){
            final int k=i; TextView b=action(labels[i]); b.setTextSize(22);
            nav.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));
            b.setOnClickListener(v->{
                switch(k){
                    case 0: if(web.canGoBack())web.goBack(); break;
                    case 1: if(web.canGoForward())web.goForward(); break;
                    case 2: web.loadUrl(HOME); break;
                    case 3: web.reload(); break;
                    case 4: address.setText(""); address.requestFocus();
                        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(address,InputMethodManager.SHOW_IMPLICIT); break;
                }
            });
        }
        root.addView(nav); setContentView(root);
    }

    private void configureWeb() {
        WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true);
        s.setSupportZoom(true); s.setBuiltInZoomControls(true); s.setDisplayZoomControls(false);
        if(Build.VERSION.SDK_INT>=29) s.setAlgorithmicDarkeningAllowed(true);
        web.setBackgroundColor(BG);
        web.setWebViewClient(new WebViewClient(){
            @Override public void onPageStarted(WebView v,String url,android.graphics.Bitmap icon){
                address.setText(url); progress.setVisibility(ProgressBar.VISIBLE);
            }
            @Override public void onPageFinished(WebView v,String url){
                address.setText(url); progress.setVisibility(ProgressBar.GONE);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest req){
                return false;
            }
        });
        web.setWebChromeClient(new WebChromeClient(){
            @Override public void onProgressChanged(WebView v,int value){
                progress.setProgress(value);
                if(value>=100) progress.setVisibility(ProgressBar.GONE);
            }
            @Override public void onReceivedTitle(WebView v,String t){ if(t!=null) title=t==null?null:title; }
        });
        web.setDownloadListener((url,ua,disposition,mime,size)->{
            android.content.Intent i=new android.content.Intent(android.content.Intent.ACTION_VIEW,android.net.Uri.parse(url));
            try{startActivity(i);}catch(Exception ignored){}
        });
    }

    private void navigate(String input) {
        String q=input.trim(); if(q.isEmpty())return;
        String url;
        if(q.matches("(?i)^https?://.*")) url=q;
        else if(q.matches("(?i)^[a-z0-9.-]+\\.[a-z]{2,}(:[0-9]+)?(/.*)?$")) url="https://"+q;
        else url="https://www.google.com/search?q="+android.net.Uri.encode(q);
        web.loadUrl(url);
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(address.getWindowToken(),0);
        web.requestFocus();
    }
    private TextView action(String text){
        TextView v=new TextView(this); v.setText(text); v.setTextColor(TEXT);
        v.setTextSize(15); v.setGravity(Gravity.CENTER); v.setTypeface(Typeface.DEFAULT,Typeface.MEDIUM);
        return v;
    }
    private GradientDrawable round(int color,int radius){
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out); if(web!=null)web.saveState(out);}
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}
