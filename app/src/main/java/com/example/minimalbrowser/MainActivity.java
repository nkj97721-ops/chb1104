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
import android.webkit.*;
import android.widget.*;
import android.text.InputType;

public class MainActivity extends Activity {
    private static final int BG=0xFF0B0D12, SURFACE=0xFF151923;
    private static final int FIELD=0xFF202633, TEXT=0xFFF4F6FA;
    private static final int MUTED=0xFF929BAD, ACCENT=0xFF9BBEFF;
    private static final String HOME="https://duckduckgo.com";
    private WebView web;
    private EditText address;
    private ProgressBar progress;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        createLayout();
        configureBrowser();
        if (state == null) web.loadUrl(HOME);
        else web.restoreState(state);
    }

    private void createLayout() {
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14),dp(12),dp(14),dp(10));
        header.setBackgroundColor(BG);

        TextView logo=new TextView(this);
        logo.setText("N");
        logo.setTextColor(BG);
        logo.setTextSize(18);
        logo.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(circle(ACCENT));
        header.addView(logo,new LinearLayout.LayoutParams(dp(36),dp(36)));

        TextView brand=new TextView(this);
        brand.setText("  NIGHTBIRD");
        brand.setTextColor(TEXT);
        brand.setTextSize(13);
        brand.setLetterSpacing(0.12f);
        brand.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        header.addView(brand);
        root.addView(header);

        LinearLayout searchRow=new LinearLayout(this);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        searchRow.setPadding(dp(12),0,dp(12),dp(10));

        address=new EditText(this);
        address.setSingleLine(true);
        address.setTextSize(14);
        address.setTextColor(TEXT);
        address.setHintTextColor(MUTED);
        address.setHint("搜索内容或输入网址");
        address.setPadding(dp(16),0,dp(12),0);
        address.setSelectAllOnFocus(true);
        address.setImeOptions(EditorInfo.IME_ACTION_GO);
        address.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
        address.setBackground(round(FIELD,18));
        searchRow.addView(address,new LinearLayout.LayoutParams(0,dp(48),1));

        TextView go=new TextView(this);
        go.setText("→");
        go.setTextSize(22);
        go.setTextColor(ACCENT);
        go.setGravity(Gravity.CENTER);
        searchRow.addView(go,new LinearLayout.LayoutParams(dp(48),dp(48)));
        go.setOnClickListener(v->navigate());
        address.setOnEditorActionListener((v,a,e)->{
            if(a==EditorInfo.IME_ACTION_GO || (e!=null&&e.getKeyCode()==KeyEvent.KEYCODE_ENTER)) {
                navigate(); return true;
            }
            return false;
        });
        root.addView(searchRow);

        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(SURFACE));
        progress.setVisibility(View.GONE);
        root.addView(progress,new LinearLayout.LayoutParams(-1,dp(2)));

        web=new WebView(this);
        web.setBackgroundColor(BG);
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout bottom=new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER);
        bottom.setPadding(dp(8),dp(7),dp(8),dp(7));
        bottom.setBackgroundColor(SURFACE);
        String[] icons={"‹","›","⌂","↻","＋"};
        for(int i=0;i<icons.length;i++) {
            final int action=i;
            TextView item=new TextView(this);
            item.setText(icons[i]);
            item.setTextColor(i==2?ACCENT:TEXT);
            item.setTextSize(25);
            item.setGravity(Gravity.CENTER);
            bottom.addView(item,new LinearLayout.LayoutParams(0,dp(48),1));
            item.setOnClickListener(v->{
                switch(action) {
                    case 0: if(web.canGoBack()) web.goBack(); break;
                    case 1: if(web.canGoForward()) web.goForward(); break;
                    case 2: web.loadUrl(HOME); break;
                    case 3: web.reload(); break;
                    case 4:
                        address.setText("");
                        address.requestFocus();
                        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
                            .showSoftInput(address,InputMethodManager.SHOW_IMPLICIT);
                        break;
                }
            });
        }
        root.addView(bottom);
        setContentView(root);
    }

    private void configureBrowser() {
        WebSettings settings=web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        if(android.os.Build.VERSION.SDK_INT>=29) settings.setAlgorithmicDarkeningAllowed(true);

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
        web.setDownloadListener((url,ua,disposition,mime,size)->{
            try { startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(url))); } catch(Exception ignored) {}
        });
    }

    private void navigate() {
        String input=address.getText().toString().trim();
        if(input.isEmpty()) return;
        String target;
        if(input.matches("(?i)^https?://.*")) target=input;
        else if(input.matches("(?i)^[a-z0-9.-]+\\.[a-z]{2,}(:[0-9]+)?(/.*)?$"))
            target="https://"+input;
        else target="https://duckduckgo.com/?q="+android.net.Uri.encode(input);
        web.loadUrl(target);
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(address.getWindowToken(),0);
    }

    private GradientDrawable round(int color,int radius) {
        GradientDrawable shape=new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        return shape;
    }
    private GradientDrawable circle(int color) {
        GradientDrawable shape=round(color,50);
        return shape;
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
