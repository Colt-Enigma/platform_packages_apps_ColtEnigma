/*
 * Copyright (C) 2020 ColtOS Project
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

package com.colt.enigma.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.colt.enigma.preference.SystemSettingListPreference;
import com.android.internal.util.colt.ColtUtils;


public class StatusBarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String VOLTE_ICON_STYLE = "volte_icon_style";
    private static final String VOWIFI_ICON_STYLE = "vowifi_icon_style";

    private SystemSettingListPreference mVolteIconStyle;
    private SystemSettingListPreference mVowifiIconStyle;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.colt_enigma_statusbar);

        PreferenceScreen prefSet = getPreferenceScreen();
	mVolteIconStyle = (SystemSettingListPreference) findPreference(VOLTE_ICON_STYLE);
        int volteIconStyle = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.VOLTE_ICON_STYLE, 0);
        mVolteIconStyle.setValue(String.valueOf(volteIconStyle));
        mVolteIconStyle.setOnPreferenceChangeListener(this);

        mVowifiIconStyle = (SystemSettingListPreference) findPreference(VOWIFI_ICON_STYLE);
        int vowifiIconStyle = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.VOWIFI_ICON_STYLE, 0);
        mVowifiIconStyle.setValue(String.valueOf(vowifiIconStyle));
        mVowifiIconStyle.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mVolteIconStyle) {
            int volteIconStyle = Integer.parseInt(((String) newValue).toString());
            Settings.System.putInt(resolver,
                  Settings.System.VOLTE_ICON_STYLE, volteIconStyle);
            mVolteIconStyle.setValue(String.valueOf(volteIconStyle));
            ColtUtils.showSystemUiRestartDialog(getContext());
            return true;
        } else if (preference == mVowifiIconStyle) {
            int vowifiIconStyle = Integer.parseInt(((String) newValue).toString());
            Settings.System.putInt(resolver,
                  Settings.System.VOWIFI_ICON_STYLE, vowifiIconStyle);
            mVowifiIconStyle.setValue(String.valueOf(vowifiIconStyle));
            ColtUtils.showSystemUiRestartDialog(getContext());
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ENIGMA;
    }

}
