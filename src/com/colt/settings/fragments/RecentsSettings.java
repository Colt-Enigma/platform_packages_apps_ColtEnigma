/*
 * Copyright (C) 2018 ColtOS Project
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

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.colt.settings.preference.SystemSettingMasterSwitchPreference;

import java.util.ArrayList;
import java.util.List;

public class RecentsSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_ALTERNATIVE_RECENTS_CATEGORY = "alternative_recents_category";
    private static final String PREF_THREE_BUTTONS_DISABLED = "three_buttons_disabled_warning";
    private static final String PREF_GESTURE_SYSTEM_NAVIGATION = "gesture_system_navigation";
    private static final String KEY_USE_SLIM_RECENTS = "use_slim_recents";

    private PreferenceCategory mAlternativeRecentsCategory;
    private SystemSettingMasterSwitchPreference mSlimRecents;
    private Preference mGestureSystemNavigation;
    private Preference mWarning;
    private Context mContext;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.colt_settings_recents);

        mAlternativeRecentsCategory =
                (PreferenceCategory) findPreference(PREF_ALTERNATIVE_RECENTS_CATEGORY);

	mWarning =
                (Preference) findPreference(PREF_THREE_BUTTONS_DISABLED);
        mGestureSystemNavigation =
                (Preference) findPreference(PREF_GESTURE_SYSTEM_NAVIGATION);
        mSlimRecents =
                (SystemSettingMasterSwitchPreference) findPreference(KEY_USE_SLIM_RECENTS);
        mSlimRecents.setOnPreferenceChangeListener(this);

        // Alternative recents en-/disabling

        updateDependencies();

        // Warning for alternative recents when gesture navigation is enabled,
        // which directly controls quickstep (launcher) recents.
        final int navigationMode = getActivity().getResources()
                .getInteger(com.android.internal.R.integer.config_navBarInteractionMode);
        // config_navBarInteractionMode:
        //  0: 3 button mode (supports slim recents)
        //  1: 2 button mode (currently does not support alternative recents)
        //  2: gesture only (currently does not support alternative recents)
	if (navigationMode == 0) {
            int useSlim = Settings.System.getInt(getActivity().getContentResolver(),
                    KEY_USE_SLIM_RECENTS, 0);
            mSlimRecents.setEnabled(true);
            mSlimRecents.setChecked(useSlim != 0);
            mGestureSystemNavigation.setSummary(getString(R.string.legacy_navigation_title));
            mAlternativeRecentsCategory.removePreference(findPreference(PREF_THREE_BUTTONS_DISABLED));
        } else if (navigationMode == 1) {
            mGestureSystemNavigation.setSummary(getString(R.string.swipe_up_to_switch_apps_title));
            mSlimRecents.setEnabled(false);
        } else {
	    mGestureSystemNavigation.setSummary(getString(R.string.edge_to_edge_navigation_title));
            mSlimRecents.setEnabled(false);
        }
    }

    private void updateDependencies() {
        updateDependencies(null, null);
    }

    private void updateDependencies(Preference updatedPreference, Boolean newValue) {
        // Disable stock recents category if alternative enabled
        boolean alternativeRecentsEnabled = newValue != null && newValue;
        if (!alternativeRecentsEnabled) {
            for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
                Preference preference = mAlternativeRecentsCategory.getPreference(i);
                if (preference == updatedPreference) {
                    // Already used newValue
                    continue;
                }
                if (preference instanceof SystemSettingMasterSwitchPreference
                        && ((SystemSettingMasterSwitchPreference) preference).isChecked()) {
                    alternativeRecentsEnabled = true;
                    break;
                }
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
	if (preference == mSlimRecents) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    KEY_USE_SLIM_RECENTS, value ? 1 : 0);
            updateDependencies(preference, value);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }
}
