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
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.os.ServiceManager;
import android.provider.Settings;
import android.content.om.IOverlayManager;
import com.android.internal.util.colt.ThemeUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Locale;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.ArrayList;

import com.android.internal.util.colt.ThemeUtils;
import com.colt.enigma.preference.SystemSettingListPreference;

public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String KEY_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
    private static final String KEY_BRIGHTNESS_SLIDER_POSITION = "qs_brightness_slider_position";
    private static final String KEY_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";
    private static final String KEY_QS_PANEL_STYLE  = "qs_panel_style";
    private static final String KEY_QS_UI_STYLE  = "qs_ui_style";
    private static final String overlayThemeTarget  = "com.android.systemui";
    

    private ListPreference mShowBrightnessSlider;
    private ListPreference mBrightnessSliderPosition;
    private SwitchPreference mShowAutoBrightness;
    private Handler mHandler;
    private ThemeUtils mThemeUtils;
    private IOverlayManager mOverlayManager;
    private IOverlayManager mOverlayService;
    private SystemSettingListPreference mQsStyle;
    private SystemSettingListPreference mQsUI;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.colt_enigma_quicksettings);
        final Context mContext = getActivity().getApplicationContext();
        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = mContext.getContentResolver();
        
        mThemeUtils = new ThemeUtils(getActivity());
        
        mOverlayService = IOverlayManager.Stub
        .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        mQsStyle = (SystemSettingListPreference) findPreference(KEY_QS_PANEL_STYLE);
        mQsUI = (SystemSettingListPreference) findPreference(KEY_QS_UI_STYLE);
        mCustomSettingsObserver.observe();

        mShowBrightnessSlider = findPreference(KEY_SHOW_BRIGHTNESS_SLIDER);
        mShowBrightnessSlider.setOnPreferenceChangeListener(this);
        boolean showSlider = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT) > 0;

        mBrightnessSliderPosition = findPreference(KEY_BRIGHTNESS_SLIDER_POSITION);
        mBrightnessSliderPosition.setEnabled(showSlider);

        mShowAutoBrightness = findPreference(KEY_SHOW_AUTO_BRIGHTNESS);
        boolean automaticAvailable = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);
        if (automaticAvailable) {
            mShowAutoBrightness.setEnabled(showSlider);
        } else {
            prefScreen.removePreference(mShowAutoBrightness);
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mShowBrightnessSlider) {
            int value = Integer.parseInt((String) newValue);
            mBrightnessSliderPosition.setEnabled(value > 0);
            if (mShowAutoBrightness != null)
                mShowAutoBrightness.setEnabled(value > 0);
            return true;
            } else if (preference == mQsStyle) {
            mCustomSettingsObserver.observe();
            return true;
            } else if (preference == mQsStyle || preference == mQsUI) {
            mCustomSettingsObserver.observe();
        }
        return false;
    }
    
    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_PANEL_STYLE),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_UI_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.QS_PANEL_STYLE)) || uri.equals(Settings.System.getUriFor(Settings.System.QS_UI_STYLE))) {
                updateQsStyle();
            }
        }
    }

    private void updateQsStyle() {
        ContentResolver resolver = getActivity().getContentResolver();

        boolean isA11Style = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_UI_STYLE , 1, UserHandle.USER_CURRENT) == 1;

        int qsPanelStyle = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_PANEL_STYLE , 0, UserHandle.USER_CURRENT);

	String qsUIStyleCategory = "android.theme.customization.qs_ui";
	String qsPanelStyleCategory = "android.theme.customization.qs_panel";

	/// reset all overlays before applying
	resetQsOverlays(qsPanelStyleCategory);
	resetQsOverlays(qsUIStyleCategory);

	if (isA11Style) {
	    setQsStyle("com.android.system.qs.ui.A11", qsUIStyleCategory);
	}

	if (qsPanelStyle == 0) return;

        switch (qsPanelStyle) {
            case 1:
              setQsStyle("com.android.system.qs.outline", qsPanelStyleCategory);
              break;
            case 2:
            case 3:
              setQsStyle("com.android.system.qs.twotoneaccent", qsPanelStyleCategory);
              break;
            case 4:
              setQsStyle("com.android.system.qs.shaded", qsPanelStyleCategory);
              break;
            case 5:
              setQsStyle("com.android.system.qs.cyberpunk", qsPanelStyleCategory);
              break;
            case 6:
              setQsStyle("com.android.system.qs.neumorph", qsPanelStyleCategory);
              break;
            case 7:
              setQsStyle("com.android.system.qs.reflected", qsPanelStyleCategory);
              break;
            case 8:
              setQsStyle("com.android.system.qs.surround", qsPanelStyleCategory);
              break;
            case 9:
              setQsStyle("com.android.system.qs.thin", qsPanelStyleCategory);
              break;
            case 10:
              setQsStyle("com.android.system.qs.twotoneaccenttrans", qsPanelStyleCategory);
              break;
            default:
              break;
        }
    }

    public void resetQsOverlays(String category) {
        mThemeUtils.setOverlayEnabled(category, overlayThemeTarget, overlayThemeTarget);
    }

    public void setQsStyle(String overlayName, String category) {
        mThemeUtils.setOverlayEnabled(category, overlayName, overlayThemeTarget);
    }


    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }

}
