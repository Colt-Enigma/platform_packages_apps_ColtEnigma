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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import androidx.preference.*;
import androidx.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.dashboard.DashboardFragment;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.colt.ColtUtils;
import com.colt.enigma.preference.PackageListAdapter;
import com.colt.enigma.preference.PackageListAdapter.PackageItem;
import com.colt.enigma.preference.CustomSeekBarPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import android.os.Bundle;
import com.android.settings.R;

import com.android.settings.SettingsPreferenceFragment;

public class NotificationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String FLASHLIGHT_CATEGORY = "flashlight_category";
    private static final String FLASHLIGHT_CALL_PREF = "flashlight_on_call";
    private static final String FLASHLIGHT_DND_PREF = "flashlight_on_call_ignore_dnd";
    private static final String FLASHLIGHT_RATE_PREF = "flashlight_on_call_rate";

    private ListPreference mFlashOnCall;
    private SwitchPreference mFlashOnCallIgnoreDND;
    private CustomSeekBarPreference mFlashOnCallRate;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.colt_enigma_notifications);

	final PreferenceScreen prefScreen = getPreferenceScreen();
        final Context mContext = getActivity().getApplicationContext();
        final ContentResolver resolver = mContext.getContentResolver();
        final Resources res = mContext.getResources();

	if (!ColtUtils.deviceHasFlashlight(mContext)) {
            final PreferenceCategory flashlightCategory =
                    (PreferenceCategory) prefScreen.findPreference(FLASHLIGHT_CATEGORY);
            prefScreen.removePreference(flashlightCategory);
	} else {
            mFlashOnCall = (ListPreference)
                    prefScreen.findPreference(FLASHLIGHT_CALL_PREF);
            mFlashOnCall.setOnPreferenceChangeListener(this);

            mFlashOnCallIgnoreDND = (SwitchPreference)
                    prefScreen.findPreference(FLASHLIGHT_DND_PREF);
            int value = Settings.System.getInt(resolver,
                    Settings.System.FLASHLIGHT_ON_CALL, 0);

            mFlashOnCallRate = (CustomSeekBarPreference)
                    prefScreen.findPreference(FLASHLIGHT_RATE_PREF);

            mFlashOnCallIgnoreDND.setEnabled(value > 1);
            mFlashOnCallRate.setEnabled(value > 0);
        }

    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();

	Settings.System.putIntForUser(resolver,
                Settings.System.FLASHLIGHT_ON_CALL, 0, UserHandle.USER_CURRENT);
	Settings.System.putIntForUser(resolver,
                Settings.System.FLASHLIGHT_ON_CALL_IGNORE_DND, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.FLASHLIGHT_ON_CALL_RATE, 1, UserHandle.USER_CURRENT);

     }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFlashOnCall) {
            int value = Integer.parseInt((String) newValue);
            mFlashOnCallIgnoreDND.setEnabled(value > 1);
            mFlashOnCallRate.setEnabled(value > 0);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }
}
