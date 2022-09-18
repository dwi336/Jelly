/*
 * Copyright (C) 2020 The LineageOS Project
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
package org.lineageos.jelly;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import org.lineageos.jelly.utils.PrefsUtils;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                getWindow().setStatusBarColor(Color.BLACK);
            }
        }

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstance, String rootKey) {
            // Load the preferences from an XML resource
            setPreferencesFromResource(R.xml.settings, rootKey);

            Preference homepagePreference = findPreference("key_home_page");
            if (homepagePreference != null) {
                bindPreferenceSummaryToValue(homepagePreference,
                        getString(R.string.default_home_page));
            }
            
            if (getResources().getBoolean(R.bool.is_tablet)) {
                SwitchPreference reachModePreference = this.findPreference("key_reach_mode");
                if (reachModePreference != null) {
                    getPreferenceScreen().removePreference(reachModePreference);
                }
            }
        }

        private final void bindPreferenceSummaryToValue(Preference preference, String def) {
            final String key = preference.getKey();
            preference.setOnPreferenceChangeListener(this);

            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(key, def));
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = String.valueOf(value);
            if (preference instanceof ListPreference) {
                int prefIndex = ((ListPreference)preference).findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    preference.setSummary(((ListPreference)preference).getEntries()[prefIndex]);
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            final String key = preference.getKey();
            if (key.equals("key_home_page")) {
                editHomePage(preference);
                return true;
            } else if (key.equals("key_cookie_clear")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().removeAllCookies(null);
                } else {
                    CookieSyncManager cookieSyncMngr=CookieSyncManager.getInstance();
                    cookieSyncMngr.startSync();
                    CookieManager cookieManager=CookieManager.getInstance();
                    cookieManager.removeAllCookie();
                    cookieManager.removeSessionCookie();
                    cookieSyncMngr.stopSync();
                }

                Toast.makeText(preference.getContext(), getString(R.string.pref_cookie_clear_done),
                        Toast.LENGTH_LONG).show();
                return true;
            } else {
                return super.onPreferenceTreeClick(preference);
            }
        }

        private void editHomePage(Preference preference) {
            Context context = preference.getContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            AlertDialog alertDialog = builder.create();
            LayoutInflater inflater = alertDialog.getLayoutInflater();

            View homepageView = inflater.inflate(R.layout.dialog_homepage_edit,
                    new LinearLayout(context));
            EditText editText = homepageView.findViewById(R.id.homepage_edit_url);
            editText.setText(PrefsUtils.getHomePage(context));

            builder.setTitle(R.string.pref_start_page_dialog_title)
                    .setMessage(R.string.pref_start_page_dialog_message)
                    .setView(homepageView)
                    .setPositiveButton(android.R.string.ok,
                            (dialog, which) -> {
                                String url = editText.getText().toString().isEmpty() ?
                                        getString(R.string.default_home_page) :
                                        editText.getText().toString();
                                PrefsUtils.setHomePage(context, url);
                                preference.setSummary(url);
                            })
                    .setNeutralButton(R.string.pref_start_page_dialog_reset,
                            (dialog, which) -> {
                                String url = getString(R.string.default_home_page);
                                PrefsUtils.setHomePage(context, url);
                                preference.setSummary(url);
                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }
}
