package com.colt.settings.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import androidx.preference.PreferenceCategory;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import com.colt.settings.utils.Utils;
import android.text.TextUtils;
import com.colt.settings.preference.AppMultiSelectListPreference;
import com.colt.settings.preference.ScrollAppsViewPreference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.util.colt.PackageUtils;
import com.android.internal.util.colt.ColtUtils;

import com.colt.settings.preference.SystemSettingMasterSwitchPreference;

public class MiscSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String KEY_ASPECT_RATIO_APPS_ENABLED = "aspect_ratio_apps_enabled";
    private static final String KEY_ASPECT_RATIO_APPS_LIST = "aspect_ratio_apps_list";
    private static final String KEY_ASPECT_RATIO_CATEGORY = "aspect_ratio_category";
    private static final String KEY_ASPECT_RATIO_APPS_LIST_SCROLLER = "aspect_ratio_apps_list_scroller";
    private static final String NAVIGATION_BAR_RECENTS_STYLE = "navbar_recents_style";

    private static final String GAMING_MODE_ENABLED = "gaming_mode_enabled";

    private SystemSettingMasterSwitchPreference mGamingMode;

    private AppMultiSelectListPreference mAspectRatioAppsSelect;
    private ScrollAppsViewPreference mAspectRatioApps;
    private ListPreference mNavbarRecentsStyle;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final String KEY_DEVICE_PART = "oneplus_shit";
        final String KEY_DEVICE_PART_PACKAGE_NAME = "com.oneplus.shit.settings";

        addPreferencesFromResource(R.xml.colt_settings_misc);

	ContentResolver resolver = getActivity().getContentResolver();

	mGamingMode = (SystemSettingMasterSwitchPreference) findPreference(GAMING_MODE_ENABLED);
        mGamingMode.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.GAMING_MODE_ENABLED, 0) == 1));
        mGamingMode.setOnPreferenceChangeListener(this);

      final PreferenceCategory aspectRatioCategory =
          (PreferenceCategory) getPreferenceScreen().findPreference(KEY_ASPECT_RATIO_CATEGORY);
      final boolean supportMaxAspectRatio =
          getResources().getBoolean(com.android.internal.R.bool.config_haveHigherAspectRatioScreen);
      if (!supportMaxAspectRatio) {
          getPreferenceScreen().removePreference(aspectRatioCategory);
      } else {
        mAspectRatioAppsSelect =
            (AppMultiSelectListPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST);
        mAspectRatioApps =
            (ScrollAppsViewPreference) findPreference(KEY_ASPECT_RATIO_APPS_LIST_SCROLLER);
        final String valuesString = Settings.System.getString(getContentResolver(),
            Settings.System.OMNI_ASPECT_RATIO_APPS_LIST);
        List<String> valuesList = new ArrayList<String>();
        if (!TextUtils.isEmpty(valuesString)) {
          valuesList.addAll(Arrays.asList(valuesString.split(":")));
          mAspectRatioApps.setVisible(true);
          mAspectRatioApps.setValues(valuesList);
        } else {
          mAspectRatioApps.setVisible(false);
        }
        mAspectRatioAppsSelect.setValues(valuesList);
        mAspectRatioAppsSelect.setOnPreferenceChangeListener(this);
      }

        // DeviceParts
        if (!Utils.isPackageInstalled(getActivity(), KEY_DEVICE_PART_PACKAGE_NAME)) {
            getPreferenceScreen().removePreference(findPreference(KEY_DEVICE_PART));
        }

	// SmartPixels
	boolean enableSmartPixels = getContext().getResources().
                getBoolean(com.android.internal.R.bool.config_enableSmartPixels);
        Preference SmartPixels = findPreference("smart_pixels");
        if (!enableSmartPixels){
            getPreferenceScreen().removePreference(SmartPixels);
        }

        mNavbarRecentsStyle = (ListPreference) findPreference(NAVIGATION_BAR_RECENTS_STYLE);
        int recentsStyle = Settings.System.getInt(resolver,
                Settings.System.OMNI_NAVIGATION_BAR_RECENTS, 0);

        mNavbarRecentsStyle.setValue(Integer.toString(recentsStyle));
        mNavbarRecentsStyle.setSummary(mNavbarRecentsStyle.getEntry());
        mNavbarRecentsStyle.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	if (preference == mAspectRatioAppsSelect) {
        Collection<String> valueList = (Collection<String>) newValue;
        mAspectRatioApps.setVisible(false);
        if (valueList != null) {
          Settings.System.putString(getContentResolver(),
              Settings.System.OMNI_ASPECT_RATIO_APPS_LIST, TextUtils.join(":", valueList));
          mAspectRatioApps.setVisible(true);
          mAspectRatioApps.setValues(valueList);
        } else {
          Settings.System.putString(getContentResolver(),
              Settings.System.OMNI_ASPECT_RATIO_APPS_LIST, "");
        }
        return true;
        } else if (preference == mGamingMode) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.GAMING_MODE_ENABLED, value ? 1 : 0);
            return true;
	} else if (preference == mNavbarRecentsStyle) {
            int value = Integer.valueOf((String) newValue);
            if (value == 1) {
                if (!isOmniSwitchInstalled()){
                    doOmniSwitchUnavail();
                } else if (!ColtUtils.isOmniSwitchRunning(getActivity())) {
                    doOmniSwitchConfig();
                }
            }
            int index = mNavbarRecentsStyle.findIndexOfValue((String) newValue);
            mNavbarRecentsStyle.setSummary(mNavbarRecentsStyle.getEntries()[index]);
            Settings.System.putInt(getContentResolver(), Settings.System.OMNI_NAVIGATION_BAR_RECENTS, value);
            return true;
	}
        return false;
    }

    private void checkForOmniSwitchRecents() {
        if (!isOmniSwitchInstalled()){
            doOmniSwitchUnavail();
        } else if (!ColtUtils.isOmniSwitchRunning(getActivity())) {
            doOmniSwitchConfig();
        }
    }

    private void doOmniSwitchConfig() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.omniswitch_title);
        alertDialogBuilder.setMessage(R.string.omniswitch_dialog_running_new)
            .setPositiveButton(R.string.omniswitch_settings, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    startActivity(ColtUtils.INTENT_LAUNCH_APP);
                }
            });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void doOmniSwitchUnavail() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.omniswitch_title);
        alertDialogBuilder.setMessage(R.string.omniswitch_dialog_unavail);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private boolean isOmniSwitchInstalled() {
        return PackageUtils.isAvailableApp(ColtUtils.APP_PACKAGE_NAME, getActivity());
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }
}
