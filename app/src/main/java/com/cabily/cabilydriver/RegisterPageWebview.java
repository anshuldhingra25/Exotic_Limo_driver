package com.cabily.cabilydriver;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.Hockeyapp.ActionBarActivityHockeyApp;
import com.app.service.ServiceConstant;
import com.cabily.cabilydriver.Utils.ConnectionDetector;
import com.cabily.cabilydriver.Utils.SessionManager;
import com.cabily.cabilydriver.widgets.PkDialog;

/**
 */
public class RegisterPageWebview extends ActionBarActivityHockeyApp {

    SessionManager session;
    private WebView mWebView;
    private Boolean isInternetPresent = false;
    private ConnectionDetector cd;
    private String weburl = ServiceConstant.Register_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_webview);
        initilize();
    }

    private void initilize() {
        session = new SessionManager(RegisterPageWebview.this);
        mWebView = (WebView) findViewById(R.id.register_webView);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new Callback());
        cd = new ConnectionDetector(RegisterPageWebview.this);
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            mWebView.loadUrl(weburl);
        } else {
            Alert(getResources().getString(R.string.alert_sorry_label_title), getResources().getString(R.string.alert_nointernet));
        }
    }

    private class Callback extends WebViewClient {  //HERE IS THE MAIN CHANGE.

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return (false);
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    //--------------Alert Method-----------
    private void Alert(String title, String message) {
        final PkDialog mDialog = new PkDialog(RegisterPageWebview.this);
        mDialog.setDialogTitle(title);
        mDialog.setDialogMessage(message);
        mDialog.setPositiveButton(getResources().getString(R.string.alert_label_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }


}
