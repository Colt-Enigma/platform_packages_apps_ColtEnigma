package com.colt.settings.fragments;
import com.android.internal.logging.nano.MetricsProto;
import android.os.Bundle;
import com.android.settings.R;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.settings.SettingsPreferenceFragment;
import com.colt.settings.preference.AmbientLightSettingsPreview;
import android.provider.Settings;
import com.colt.settings.preference.CustomSeekBarPreference;
import com.colt.settings.preference.SystemSettingMasterSwitchPreference;

import com.colt.settings.utils.Utils;

public class NotificationSettings extends SettingsPreferenceFragment {

    private static final String INCALL_VIB_OPTIONS = "incall_vib_options";

    private static final String FORCE_EXPANDED_NOTIFICATIONS = "force_expanded_notifications";

    private static final String LIGHTS_CATEGORY = "notification_lights";
    private static final String BATTERY_LIGHT_ENABLED = "battery_light_enabled";

    private SwitchPreference mForceExpanded;
    private PreferenceCategory mLightsCategory;
    private SystemSettingMasterSwitchPreference mBatteryLightEnabled;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.colt_settings_notifications);
        PreferenceScreen prefScreen = getPreferenceScreen();

        PreferenceCategory incallVibCategory = (PreferenceCategory) findPreference(INCALL_VIB_OPTIONS);
        if (!Utils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(incallVibCategory);
        }

	mForceExpanded = (SwitchPreference) findPreference(FORCE_EXPANDED_NOTIFICATIONS);
        mForceExpanded.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.FORCE_EXPANDED_NOTIFICATIONS, 0) == 1));

	mBatteryLightEnabled = (SystemSettingMasterSwitchPreference) findPreference(BATTERY_LIGHT_ENABLED);
        mBatteryLightEnabled.setOnPreferenceChangeListener(this);
        int batteryLightEnabled = Settings.System.getInt(getContentResolver(),
                BATTERY_LIGHT_ENABLED, 1);
        mBatteryLightEnabled.setChecked(batteryLightEnabled != 0);

        mLightsCategory = (PreferenceCategory) findPreference(LIGHTS_CATEGORY);
        if (!getResources().getBoolean(com.android.internal.R.bool.config_hasNotificationLed)) {
            getPreferenceScreen().removePreference(mLightsCategory);
        }

    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (preference == mForceExpanded) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FORCE_EXPANDED_NOTIFICATIONS, checked ? 1 : 0);
            return true;
        } else if (preference == mBatteryLightEnabled) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getContentResolver(),
		            BATTERY_LIGHT_ENABLED, value ? 1 : 0);
            return true;
        }
        return true;
    }

}

