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
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.app.WallpaperManager;
import android.net.Uri;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.android.settings.R;
import com.colt.settings.preference.CustomSeekBarPreference;
import com.colt.settings.preference.SecureSettingMasterSwitchPreference;

public class LockScreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_AMBIENT_DISPLAY_CUSTOM = "ambient_display_custom";
    private static final String LOCKSCREEN_MAX_NOTIF_CONFIG = "lockscreen_max_notif_cofig";
    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";
    private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker_category";
    private static final String FOD_ANIMATION = "fod_anim";
    private static final String KEY_BATT_BAR_COLOR = "sysui_keyguard_battery_bar_color";

    private static final int DEFAULT_COLOR = 0xffffffff;

    private CustomSeekBarPreference mMaxKeyguardNotifConfig;
    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;
    private PreferenceCategory mFODIconPickerCategory;
    private Preference mFODAnimation;
    private ColorPickerPreference mBatteryBarColor;

    private Preference mCustomDoze;
    private static final String LOCKSCREEN_VISUALIZER_ENABLED = "lockscreen_visualizer_enabled";

    private SecureSettingMasterSwitchPreference mVisualizerEnabled;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.colt_lockscreen_settings);

        mCustomDoze = (Preference) findPreference(KEY_AMBIENT_DISPLAY_CUSTOM);
        if (!getResources().getBoolean(com.android.internal.R.bool.config_alt_ambient_display)) {
            getPreferenceScreen().removePreference(mCustomDoze);
        }

        final PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Resources resources = getResources();
        Context mContext = getContext();

        mMaxKeyguardNotifConfig = (CustomSeekBarPreference) findPreference(LOCKSCREEN_MAX_NOTIF_CONFIG);
        int kgconf = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, 3);
        mMaxKeyguardNotifConfig.setValue(kgconf);
        mMaxKeyguardNotifConfig.setOnPreferenceChangeListener(this);

	mVisualizerEnabled = (SecureSettingMasterSwitchPreference) findPreference(LOCKSCREEN_VISUALIZER_ENABLED);
        mVisualizerEnabled.setOnPreferenceChangeListener(this);
        int visualizerEnabled = Settings.Secure.getInt(resolver,
                LOCKSCREEN_VISUALIZER_ENABLED, 0);
        mVisualizerEnabled.setChecked(visualizerEnabled != 0);

	mBatteryBarColor = (ColorPickerPreference) findPreference(KEY_BATT_BAR_COLOR);
        mBatteryBarColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.SYSUI_KEYGUARD_BATTERY_BAR_COLOR, DEFAULT_COLOR);
	String hexColor = String.format("#%08x", (DEFAULT_COLOR & intColor));
        mBatteryBarColor.setSummary(hexColor);
        mBatteryBarColor.setNewPreviewColor(intColor);

	mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SwitchPreference) findPreference(FINGERPRINT_VIB);
        if (mFingerprintManager == null){
            prefScreen.removePreference(mFingerprintVib);
        } else {
            mFingerprintVib.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.FINGERPRINT_SUCCESS_VIB, 1) == 1));
            mFingerprintVib.setOnPreferenceChangeListener(this);
        }

        mFODIconPickerCategory = (PreferenceCategory) findPreference(FOD_ICON_PICKER_CATEGORY);
        if (mFODIconPickerCategory != null
                && !getResources().getBoolean(com.android.internal.R.bool.config_needCustomFODView)) {
           prefScreen.removePreference(mFODIconPickerCategory);
        }

        boolean showFODAnimationPicker = mContext.getResources().getBoolean(R.bool.showFODAnimationPicker);
        mFODAnimation = (Preference) findPreference(FOD_ANIMATION);
        if ((mFODIconPickerCategory != null && mFODAnimation != null &&
             !getResources().getBoolean(com.android.internal.R.bool.config_needCustomFODView)) ||
                (mFODIconPickerCategory != null && mFODAnimation != null && !showFODAnimationPicker)) {
            mFODIconPickerCategory.removePreference(mFODAnimation);
	}
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mMaxKeyguardNotifConfig) {
            int kgconf = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, kgconf);
            return true;
       } else if (preference == mVisualizerEnabled) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    LOCKSCREEN_VISUALIZER_ENABLED, value ? 1 : 0);
            return true;
       } else if (preference == mFingerprintVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FINGERPRINT_SUCCESS_VIB, value ? 1 : 0);
            return true;
	} else if (preference == mBatteryBarColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.SYSUI_KEYGUARD_BATTERY_BAR_COLOR, intHex);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
	return MetricsProto.MetricsEvent.COLT;
    }
}
