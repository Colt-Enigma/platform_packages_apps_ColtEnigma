package com.colt.settings.fragments;
import com.android.internal.logging.nano.MetricsProto;
import android.os.Bundle;
import com.android.settings.R;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.settings.SettingsPreferenceFragment;
import com.colt.settings.preference.AmbientLightSettingsPreview;
import android.provider.Settings;
import com.colt.settings.preference.CustomSeekBarPreference;

import com.colt.settings.utils.Utils;

public class NotificationSettings extends SettingsPreferenceFragment {

    private static final String INCALL_VIB_OPTIONS = "incall_vib_options";


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.colt_settings_notifications);
        PreferenceScreen prefScreen = getPreferenceScreen();

        PreferenceCategory incallVibCategory = (PreferenceCategory) findPreference(INCALL_VIB_OPTIONS);
        if (!Utils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(incallVibCategory);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }
}
