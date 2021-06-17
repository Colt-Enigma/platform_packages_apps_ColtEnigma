/*
 * Copyright (C) 2014-2015 The CyanogenMod Project
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
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.ArrayList;

public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String QS_BATTERY_PERCENTAGE = "qs_battery_percentage";

    private SwitchPreference mQsBatteryPercent;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.colt_enigma_quicksettings);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mQsBatteryPercent = (SwitchPreference) findPreference(QS_BATTERY_PERCENTAGE);
        mQsBatteryPercent.setChecked((Settings.System.getInt(
                getActivity().getApplicationContext().getContentResolver(),
                Settings.System.QS_SHOW_BATTERY_PERCENT, 0) == 1));
        mQsBatteryPercent.setOnPreferenceChangeListener(this);
        }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mQsBatteryPercent) {
            Settings.System.putInt(resolver,
                    Settings.System.QS_SHOW_BATTERY_PERCENT,
                    (Boolean) newValue ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ENIGMA;
    }

}
