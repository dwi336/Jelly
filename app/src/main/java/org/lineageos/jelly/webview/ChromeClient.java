/*
 * Copyright (C) 2020-2021 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lineageos.jelly.webview;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments;

import java.util.ArrayList;
import java.util.List;

import org.lineageos.jelly.R;
import org.lineageos.jelly.history.HistoryProvider;
import org.lineageos.jelly.ui.UrlBarController;
import org.lineageos.jelly.utils.TabUtils;

class ChromeClient extends WebChromeClient {
    private final WebViewExtActivity mActivity;
    private final boolean mIncognito;

    private final UrlBarController mUrlBarController;
    private final ProgressBar mProgressBar;

    ChromeClient(WebViewExtActivity activity, boolean incognito,
                 UrlBarController urlBarController, ProgressBar progressBar) {
        super();
        mActivity = activity;
        mIncognito = incognito;
        mUrlBarController = urlBarController;
        mProgressBar = progressBar;
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
        mProgressBar.setVisibility(progress == 100 ? View.INVISIBLE : View.VISIBLE);
        mProgressBar.setProgress(progress == 100 ? 0 : progress);
        super.onProgressChanged(view, progress);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        mUrlBarController.onTitleReceived(title);
        String it = view.getUrl();
        if (it != null) {
            if (!mIncognito) {
                HistoryProvider.addOrUpdateItem(mActivity.getContentResolver(), title, it);
            }
        }
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        mActivity.onFaviconLoaded(icon);
    }

    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            mActivity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mActivity, mActivity.getString(R.string.error_no_activity_found),
                    Toast.LENGTH_LONG).show();
        }
    }

    // For Lollipop 5.0+ Devices
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> path,
                                     FileChooserParams params) {
        ActivityResultLauncher<String[]> getContent = mActivity.registerForActivityResult((ActivityResultContract<String[], List<Uri>>) new ActivityResultContracts.OpenMultipleDocuments(), new ActivityResultCallback<List<Uri>>() {
            @Override
            public void onActivityResult(List <Uri> value) {
                if (value != null) {
                    if(path != null){
                        Uri[] it = new Uri[value.size()];
                        it = value.toArray(it);

                        path.onReceiveValue(it);
                    }
                }
            }
        });

        if (getContent != null) {
            try {
                final String[] acceptTypes = params.getAcceptTypes();
                if (acceptTypes != null) {
                    final ArrayList<String> list = new ArrayList<String>(acceptTypes.length);
                    for (int i = 0; i < acceptTypes.length; i++) {
                        list.add(MimeTypeMap.getSingleton().getMimeTypeFromExtension(acceptTypes[i]));
                    }
                    final String[] mimeTypes = list.toArray(new String[0]);
                    if (mimeTypes != null) {
                        getContent.launch(mimeTypes);
                    }
                }
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mActivity, mActivity.getString(R.string.error_no_activity_found),
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin,
                                                   GeolocationPermissions.Callback callback) {
        if (!mActivity.hasLocationPermission()) {
            mActivity.requestLocationPermission();
        } else {
            callback.invoke(origin, true, false);
        }
    }

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        mActivity.onShowCustomView(view, callback);
    }

    @Override
    public void onHideCustomView() {
        mActivity.onHideCustomView();
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog,
                                  boolean isUserGesture, Message resultMsg) {
        WebView.HitTestResult result = view.getHitTestResult();
        String url = result.getExtra();
        TabUtils.openInNewTab(mActivity, url, mIncognito);
        return true;
    }
}
