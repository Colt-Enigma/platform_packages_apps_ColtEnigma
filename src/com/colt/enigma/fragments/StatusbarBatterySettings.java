/*
 * Copyright (C) 2020-21 The Project-Xtended
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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.colt.enigma.preference.SystemSettingSwitchPreference;
import com.colt.enigma.preference.SystemSettingListPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class StatusbarBatterySettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String LEFT_BATTERY_TEXT = "do_left_battery_text";
    private static final String TEXT_CHARGING_SYMBOL = "text_charging_symbol";

    private ListPreference mBatteryPercent;
    private ListPreference mBatteryStyle;
    private SystemSettingListPreference mChargingSymbol;

    private int mBatteryPercentValue;

    private SystemSettingSwitchPreference mLeftBatteryText;

    private static final int BATTERY_STYLE_PORTRAIT = 0;
    private static final int BATTERY_STYLE_TEXT = 8;
    private static final int BATTERY_STYLE_HIDDEN = 9;
    private static final int BATTERY_PERCENT_HIDDEN = 0;
    //private static final int BATTERY_PERCENT_SHOW_INSIDE = 1;
    //private static final int BATTERY_PERCENT_SHOW_OUTSIDE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar_battery);
	final ContentResolver resolver = getActivity().getContentResolver();

        int batterystyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_PORTRAIT, UserHandle.USER_CURRENT);
        mBatteryStyle = findPreference("status_bar_battery_style");
        mBatteryStyle.setValue(String.valueOf(batterystyle));
        mBatteryStyle.setSummary(mBatteryStyle.getEntry());
        mBatteryStyle.setOnPreferenceChangeListener(this);

	mLeftBatteryText = (SystemSettingSwitchPreference) findPreference(LEFT_BATTERY_TEXT);
        mLeftBatteryText.setChecked((Settings.System.getInt(resolver,
                Settings.System.DO_LEFT_BATTERY_TEXT, 0) == 1));
        mLeftBatteryText.setOnPreferenceChangeListener(this);

        mBatteryPercentValue = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, BATTERY_PERCENT_HIDDEN, UserHandle.USER_CURRENT);
        mBatteryPercent = (ListPreference) findPreference("status_bar_show_battery_percent");
        mBatteryPercent.setValue(String.valueOf(mBatteryPercentValue));
        mBatteryPercent.setSummary(mBatteryPercent.getEntry());
        mBatteryPercent.setOnPreferenceChangeListener(this);
        mBatteryPercent.setEnabled(
                batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);

        mChargingSymbol = (SystemSettingListPreference) findPreference("text_charging_symbol");
        mChargingSymbol.setEnabled(
                batterystyle != BATTERY_STYLE_PORTRAIT && batterystyle != BATTERY_STYLE_HIDDEN);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryStyle) {
            int batterystyle = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, batterystyle,
                UserHandle.USER_CURRENT);
            int index = mBatteryStyle.findIndexOfValue((String) newValue);
            mBatteryStyle.setSummary(mBatteryStyle.getEntries()[index]);
            mBatteryPercent.setEnabled(
                    batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);
	    mLeftBatteryText.setEnabled(
                    batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);
            mChargingSymbol.setEnabled(
                    batterystyle != BATTERY_STYLE_PORTRAIT && batterystyle != BATTERY_STYLE_HIDDEN);
            return true;
        } else if (preference == mBatteryPercent) {
            mBatteryPercentValue = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, mBatteryPercentValue,
                    UserHandle.USER_CURRENT);
            int index = mBatteryPercent.findIndexOfValue((String) newValue);
            mBatteryPercent.setSummary(mBatteryPercent.getEntries()[index]);
            return true;
	} else if (preference == mLeftBatteryText) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.DO_LEFT_BATTERY_TEXT, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.ENIGMA;
    }

    /**
     * For Search.
     */

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.statusbar_battery;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
}
