/*
 * Copyright (C) 2020 The exTHmUI Open Source Project
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

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.util.hwkeys.ActionUtils;

import java.util.ArrayList;

import com.colt.enigma.preference.PackageListPreference;

import com.colt.enigma.preference.SystemSettingSwitchPreference;

public class GamingModeSettings extends SettingsPreferenceFragment {

    private static final String GAMING_MODE_DISABLE_HW_KEYS = "gaming_mode_disable_hw_keys";
    private static final String GAMING_MODE_DISABLE_GESTURE = "gaming_mode_disable_gesture";

    private PackageListPreference mGamingPrefList;

    private SystemSettingSwitchPreference mHardwareKeysDisable;
    private SystemSettingSwitchPreference mGestureDisable;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.x_settings_gaming);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        final boolean hasNavbar = ActionUtils.hasNavbarByDefault(getActivity());

        mGamingPrefList = (PackageListPreference) findPreference("gaming_mode_app_list");
        mGamingPrefList.setRemovedListKey(Settings.System.GAMING_MODE_REMOVED_APP_LIST);

        mHardwareKeysDisable = (SystemSettingSwitchPreference) findPreference(GAMING_MODE_DISABLE_HW_KEYS);
        mGestureDisable = (SystemSettingSwitchPreference) findPreference(GAMING_MODE_DISABLE_GESTURE);

        if (hasNavbar) {
            prefScreen.removePreference(mHardwareKeysDisable);
        } else {
            prefScreen.removePreference(mGestureDisable);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ENIGMA;
    }
}
