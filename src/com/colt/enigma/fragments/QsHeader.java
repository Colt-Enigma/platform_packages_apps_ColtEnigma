/*
 * Copyright (C) 2018 Havoc-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.colt.enigma.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;

import com.android.settingslib.search.Indexable;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;

public class QsHeader extends SettingsPreferenceFragment 
             implements Preference.OnPreferenceChangeListener{

    private static final String ANCIENT_UI_HEADERIMG_TINT_CUSTOM = "ANCIENT_UI_HEADERIMG_TINT_CUSTOM";

    private ColorPickerPreference mTitit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.qs_header);
        ContentResolver resolver = getActivity().getContentResolver();

        mTitit = (ColorPickerPreference) findPreference(ANCIENT_UI_HEADERIMG_TINT_CUSTOM);
        int mTititColor = Settings.System.getInt(getContentResolver(),
                "ANCIENT_UI_HEADERIMG_TINT_CUSTOM", 0xffffffff);
        mTitit.setNewPreviewColor(mTititColor);
        mTitit.setAlphaSliderEnabled(true);
        String mTititColorHex = String.format("#%08x", (0xffffffff & mTititColor));
        if (mTititColorHex.equals("#ffffffff")) {
            mTitit.setSummary(R.string.color_default);
        } else {
            mTitit.setSummary(mTititColorHex);
        }
        mTitit.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mTitit) {
            String hexTitit = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hexTitit.equals("#ffffffff")) {
                preference.setSummary(R.string.color_default);
            } else {
                preference.setSummary(hexTitit);
            }
            int intHexTitit = ColorPickerPreference.convertToColorInt(hexTitit);
            Settings.System.putInt(getContentResolver(),
                    "ANCIENT_UI_HEADERIMG_TINT_CUSTOM", intHexTitit);
            return true;
        } 
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }
}
