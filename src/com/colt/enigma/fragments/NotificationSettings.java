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

    private static final String FLASH_ON_CALL_OPTIONS = "on_call_flashlight_category";
    private static final String FLASH_ON_NOTIFICATION_OPTIONS = "notification_flashlight_category";
    private static final String FLASHLIGHT_CALL_PREF = "flashlight_on_call";
    private static final String FLASHLIGHT_DND_PREF = "flashlight_on_call_ignore_dnd";
    private static final String FLASHLIGHT_RATE_PREF = "flashlight_on_call_rate";

    private static final String PREF_FLASH_ON_NOTIFY = "default_notification_torch";
    private static final String PREF_FLASH_ON_NOTIFY_TIMES = "default_notification_torch1";
    private static final String PREF_FLASH_ON_NOTIFY_RATE = "default_notification_torch2";

    private ListPreference mFlashOnCall;
    private SwitchPreference mFlashOnCallIgnoreDND;
    private CustomSeekBarPreference mFlashOnCallRate;
    private SwitchPreference mFlashOnNotify;
    private CustomSeekBarPreference mFlashOnNotifyTimes;
    private CustomSeekBarPreference mFlashOnNotifyRate;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.colt_enigma_notifications);

	final PreferenceScreen prefScreen = getPreferenceScreen();
        final Context mContext = getActivity().getApplicationContext();
        final ContentResolver resolver = mContext.getContentResolver();
        final Resources res = mContext.getResources();

	if (!ColtUtils.deviceHasFlashlight(mContext)) {
            PreferenceCategory flashOnCallCategory = (PreferenceCategory)
                    findPreference(FLASH_ON_CALL_OPTIONS);
            PreferenceCategory flashOnNotifCategory = (PreferenceCategory)
                    findPreference(FLASH_ON_NOTIFICATION_OPTIONS);
            prefScreen.removePreference(flashOnCallCategory);
            prefScreen.removePreference(flashOnNotifCategory);
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

            mFlashOnNotifyTimes = (CustomSeekBarPreference)
                    findPreference(PREF_FLASH_ON_NOTIFY_TIMES);
            mFlashOnNotifyRate = (CustomSeekBarPreference)
                    findPreference(PREF_FLASH_ON_NOTIFY_RATE);
            mFlashOnNotify = (SwitchPreference)
                    findPreference(PREF_FLASH_ON_NOTIFY);
            String strVal = Settings.System.getStringForUser(resolver,
                    PREF_FLASH_ON_NOTIFY, UserHandle.USER_CURRENT);
            final boolean enabled = strVal != null && !strVal.isEmpty();
            mFlashOnNotify.setChecked(enabled);
            updateFlashOnNotifyValues(enabled, strVal);
            mFlashOnNotify.setOnPreferenceChangeListener(this);
            mFlashOnNotifyTimes.setOnPreferenceChangeListener(this);
            mFlashOnNotifyRate.setOnPreferenceChangeListener(this);
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
	 } else if (preference == mFlashOnNotify) {
            boolean value = (Boolean) newValue;
            if (!value) setFlashOnNotifyValues(0, 0);
            else setFlashOnNotifyValues(2, 2);
            return true;
        } else if (preference == mFlashOnNotifyTimes) {
            int value = (Integer) newValue;
            setFlashOnNotifyValues(value, mFlashOnNotifyRate.getValue());
            return true;
        } else if (preference == mFlashOnNotifyRate) {
            int value = (Integer) newValue;
            setFlashOnNotifyValues(mFlashOnNotifyTimes.getValue(), value);
            return true;
        }
        return false;
    }

    private void updateFlashOnNotifyValues(boolean enabled) {
        final String val = Settings.System.getStringForUser(
                getActivity().getContentResolver(),
                PREF_FLASH_ON_NOTIFY, UserHandle.USER_CURRENT);
        updateFlashOnNotifyValues(enabled, val);
    }

    private void updateFlashOnNotifyValues(boolean enabled, String val) {
        if (enabled) {
            if (val.equals("1")) {
                mFlashOnNotifyTimes.setValue(2);
                mFlashOnNotifyRate.setValue(2);
            } else {
                String[] vals = val.split(",");
                mFlashOnNotifyTimes.setValue(Integer.valueOf(vals[0]));
                mFlashOnNotifyRate.setValue(Integer.valueOf(vals[1]));
            }
        }
        mFlashOnNotifyTimes.setVisible(enabled);
        mFlashOnNotifyRate.setVisible(enabled);
    }

    private void setFlashOnNotifyValues(int times, int rate) {
        final boolean enabled = times != 0 && rate != 0;
        String val = String.valueOf(times) + "," + String.valueOf(rate);
        if (times == 2 && rate == 2) val = "1";
        Settings.System.putStringForUser(getActivity().getContentResolver(),
                PREF_FLASH_ON_NOTIFY, enabled ? val : null, UserHandle.USER_CURRENT);
        updateFlashOnNotifyValues(enabled, val);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }
}
