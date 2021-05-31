/*
 *  Copyright (C) 2020 ColtOS Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.colt.enigma.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.format.DateFormat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.content.SharedPreferences;
import android.provider.Settings;
import com.android.settings.R;
import android.util.Log;

import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.android.settings.SettingsPreferenceFragment;

public class MiscSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String RINGTONE_FOCUS_MODE = "ringtone_focus_mode";

    private Preference mSleepMode;
    private ListPreference mHeadsetRingtoneFocus;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.colt_enigma_misc);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        mHeadsetRingtoneFocus = (ListPreference) findPreference(RINGTONE_FOCUS_MODE);
        int mHeadsetRingtoneFocusValue = Settings.Global.getInt(resolver,
                Settings.Global.RINGTONE_FOCUS_MODE, 0);
        mHeadsetRingtoneFocus.setValue(Integer.toString(mHeadsetRingtoneFocusValue));
        mHeadsetRingtoneFocus.setSummary(mHeadsetRingtoneFocus.getEntry());
        mHeadsetRingtoneFocus.setOnPreferenceChangeListener(this);

        mSleepMode = findPreference("sleep_mode");

        updateSleepModeSummary();
    }

    private void updateSleepModeSummary() {
        if (mSleepMode == null) return;
        boolean enabled = Settings.Secure.getIntForUser(getActivity().getContentResolver(),
                Settings.Secure.SLEEP_MODE_ENABLED, 0, UserHandle.USER_CURRENT) == 1;
        int mode = Settings.Secure.getIntForUser(getActivity().getContentResolver(),
                Settings.Secure.SLEEP_MODE_AUTO_MODE, 0, UserHandle.USER_CURRENT);
        String timeValue = Settings.Secure.getStringForUser(getActivity().getContentResolver(),
                Settings.Secure.SLEEP_MODE_AUTO_TIME, UserHandle.USER_CURRENT);
        if (timeValue == null || timeValue.equals("")) timeValue = "20:00,07:00";
        String[] time = timeValue.split(",", 0);
        String outputFormat = DateFormat.is24HourFormat(getContext()) ? "HH:mm" : "h:mm a";
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime sinceValue = LocalTime.parse(time[0], formatter);
        LocalTime tillValue = LocalTime.parse(time[1], formatter);
        String detail;
        switch (mode) {
            default:
            case 0:
                detail = getActivity().getString(enabled
                        ? R.string.night_display_summary_on_auto_mode_never
                        : R.string.night_display_summary_off_auto_mode_never);
                break;
            case 1:
                detail = getActivity().getString(enabled
                        ? R.string.night_display_summary_on_auto_mode_twilight
                        : R.string.night_display_summary_off_auto_mode_twilight);
                break;
            case 2:
                if (enabled) {
                    detail = getActivity().getString(R.string.night_display_summary_on_auto_mode_custom, tillValue.format(outputFormatter));
                } else {
                    detail = getActivity().getString(R.string.night_display_summary_off_auto_mode_custom, sinceValue.format(outputFormatter));
                }
                break;
            case 3:
                if (enabled) {
                    detail = getActivity().getString(R.string.night_display_summary_on_auto_mode_custom, tillValue.format(outputFormatter));
                } else {
                    detail = getActivity().getString(R.string.night_display_summary_off_auto_mode_twilight);
                }
                break;
            case 4:
                if (enabled) {
                    detail = getActivity().getString(R.string.night_display_summary_on_auto_mode_twilight);
                } else {
                    detail = getActivity().getString(R.string.night_display_summary_off_auto_mode_custom, sinceValue.format(outputFormatter));
                }
                break;
        }
        String summary = getActivity().getString(enabled
                ? R.string.night_display_summary_on
                : R.string.night_display_summary_off, detail);
        mSleepMode.setSummary(summary);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mHeadsetRingtoneFocus) {
            int mHeadsetRingtoneFocusValue = Integer.valueOf((String) newValue);
            int index = mHeadsetRingtoneFocus.findIndexOfValue((String) newValue);
            mHeadsetRingtoneFocus.setSummary(
                    mHeadsetRingtoneFocus.getEntries()[index]);
            Settings.Global.putInt(getContentResolver(), Settings.Global.RINGTONE_FOCUS_MODE,
                    mHeadsetRingtoneFocusValue);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSleepModeSummary();
    }

    @Override
    public void onPause() {
        super.onPause();
        updateSleepModeSummary();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ENIGMA;
    }
}
