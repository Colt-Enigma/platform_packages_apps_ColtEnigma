/*
 * Copyright (C) 2017 ColtOS Project
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

package com.colt.settings.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.UserHandle;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;


import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

public class StatusBarSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String KEY_USE_OLD_MOBILETYPE = "use_old_mobiletype";

    private SwitchPreference mUseOldMobileType;
    private boolean mConfigUseOldMobileType;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.statusbar_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
	final ContentResolver resolver = getActivity().getContentResolver();

	mConfigUseOldMobileType = getResources().getBoolean(com.android.internal.R.bool.config_useOldMobileIcons);
        int useOldMobileIcons = (!mConfigUseOldMobileType ? 1 : 0);
        mUseOldMobileType = (SwitchPreference) findPreference(KEY_USE_OLD_MOBILETYPE);
        mUseOldMobileType.setChecked((Settings.System.getInt(resolver,
                Settings.System.USE_OLD_MOBILETYPE, useOldMobileIcons) == 1));
        mUseOldMobileType.setOnPreferenceChangeListener(this);
   }

 @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
	if (preference == mUseOldMobileType) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.USE_OLD_MOBILETYPE, value ? 1 : 0);
            return true;
        }
        return false;
    }

 @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }
 }
