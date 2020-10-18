/*
 * Copyright (C) 2021 The ColtOS Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 b* the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.colt.enigma.fragments;

import com.android.internal.logging.nano.MetricsProto;

import static android.os.UserHandle.USER_SYSTEM;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.os.UserHandle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.development.OverlayCategoryPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import com.colt.enigma.display.QsTileStylePreferenceController;
import com.colt.enigma.display.SwitchStylePreferenceController;

import com.android.internal.util.colt.ThemesUtils;
import com.android.internal.util.colt.ColtUtils;
import com.colt.enigma.preference.SystemSettingListPreference;
import com.colt.enigma.preference.SystemSettingSwitchPreference;
import com.colt.enigma.preference.CustomSeekBarPreference;
import com.colt.enigma.preference.QsColorPreferenceController;

import com.android.settings.display.FontPickerPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;
import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class ColtTheme extends DashboardFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "ColtDecorations";

    private static final String ACCENT_PRESET = "accent_preset";

    private static final String PREF_NAVBAR_STYLE = "theme_navbar_style";
    private static final String BRIGHTNESS_SLIDER_STYLE = "brightness_slider_style";
    private static final String SYSTEM_SLIDER_STYLE = "system_slider_style";
    private static final String ACCENT_COLOR = "accent_color";
    private static final String ACCENT_COLOR_PROP = "persist.sys.theme.accentcolor";
    private static final String GRADIENT_COLOR = "gradient_color";
    private static final String GRADIENT_COLOR_PROP = "persist.sys.theme.gradientcolor";
    private static final int MENU_RESET = Menu.FIRST;
    private static final String PREF_KEY_CUTOUT = "cutout_settings";
    private static final String PREF_CUSTOM_ICONS = "custom_icons";
    private static final String PREF_SETTINGS_ICONS = "theming_settings_dashboard_icons";

    static final int DEFAULT = 0xff1a73e8;

    private ListPreference mAccentPreset;
    private IOverlayManager mOverlayManager;
    private IOverlayManager mOverlayService;
    private ListPreference mBrightnessSliderStyle;
    private ListPreference mNavbarPicker;
    private ListPreference mSystemSliderStyle;
    private ColorPickerPreference mThemeColor;
    private ColorPickerPreference mGradientColor;
    private SystemSettingListPreference mDashboardIcons;
    private SystemSettingSwitchPreference mCustomIcons;

    private IntentFilter mIntentFilter;
    private static FontPickerPreferenceController mFontPickerPreference;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.android.server.ACTION_FONT_CHANGED")) {
                mFontPickerPreference.stopProgress();
            }
        }
    };

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.colt_enigma_theme;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.android.server.ACTION_FONT_CHANGED");

	mThemeColor = (ColorPickerPreference) findPreference(ACCENT_COLOR);
        String colorVal = SystemProperties.get(ACCENT_COLOR_PROP, "-1");
        try {
            int color = "-1".equals(colorVal)
                    ? Color.WHITE
                    : Color.parseColor("#" + colorVal);
            mThemeColor.setNewPreviewColor(color);
        }
        catch (Exception e) {
            mThemeColor.setNewPreviewColor(Color.WHITE);
        }
        mThemeColor.setOnPreferenceChangeListener(this);

	mDashboardIcons = (SystemSettingListPreference)
                findPreference(PREF_SETTINGS_ICONS);
        mDashboardIcons.setOnPreferenceChangeListener(this);

	mNavbarPicker = (ListPreference) findPreference(PREF_NAVBAR_STYLE);
        int navbarStyleValues = getOverlayPosition(ThemesUtils.NAVBAR_STYLES);
        if (navbarStyleValues != -1) {
            mNavbarPicker.setValue(String.valueOf(navbarStyleValues + 2));
        } else {
            mNavbarPicker.setValue("1");
        }
        mNavbarPicker.setSummary(mNavbarPicker.getEntry());
        mNavbarPicker.setOnPreferenceChangeListener(this);

        mAccentPreset = (ListPreference) findPreference(ACCENT_PRESET);
        mAccentPreset.setOnPreferenceChangeListener(this);
        checkColorPreset(colorVal);
        setupGradientPref();
        getBrightnessSliderPref();
        setSystemSliderPref();
        setHasOptionsMenu(true);

        Preference mCutoutPref = (Preference) findPreference(PREF_KEY_CUTOUT);

        String hasDisplayCutout = getResources().getString(com.android.internal.R.string.config_mainBuiltInDisplayCutout);

        if (TextUtils.isEmpty(hasDisplayCutout)) {
            getPreferenceScreen().removePreference(mCutoutPref);
        }

	mCustomIcons = (SystemSettingSwitchPreference) findPreference(PREF_CUSTOM_ICONS);
        mCustomIcons.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.DASHBOARD_ICONS, 0) == 1));
        mDashboardIcons.setEnabled((Settings.System.getInt(getContentResolver(),
                Settings.System.DASHBOARD_ICONS, 0) == 1));
        mCustomIcons.setOnPreferenceChangeListener(this);
    }

    private int getOverlayPosition(String[] overlays) {
        int position = -1;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (ColtUtils.isThemeEnabled(overlay)) {
                position = i;
            }
        }
        return position;
    }

    private String getOverlayName(String[] overlays) {
        String overlayName = null;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (ColtUtils.isThemeEnabled(overlay)) {
                overlayName = overlay;
            }
        }
        return overlayName;
    }

    public void handleOverlays(String packagename, Boolean state, IOverlayManager mOverlayManager) {
        try {
            mOverlayService.setEnabled(packagename, state, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {

        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(mFontPickerPreference = new FontPickerPreferenceController(context, lifecycle));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.adaptive_icon_shape"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.icon_pack.android"));
	controllers.add(new QsColorPreferenceController(context));
        controllers.add(new QsTileStylePreferenceController(context));
	controllers.add(new SwitchStylePreferenceController(context));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.signal_icon"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.wifi_icon"));
        return controllers;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mThemeColor) {
            int color = (Integer) newValue;
            String hexColor = String.format("%08X", (0xFFFFFFFF & color));
            SystemProperties.set(ACCENT_COLOR_PROP, hexColor);
	    checkColorPreset(hexColor);
            try {
                 mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
            } catch (RemoteException ignored) {
            }
        } else if (preference == mAccentPreset) {
            String value = (String) newValue;
            int index = mAccentPreset.findIndexOfValue(value);
            mAccentPreset.setSummary(mAccentPreset.getEntries()[index]);
            SystemProperties.set(ACCENT_COLOR_PROP, value);
            try {
                 mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
             } catch (RemoteException ignored) {
             }
        } else if (preference == mGradientColor) {
            int color = (Integer) newValue;
            String hexColor = String.format("%08X", (0xFFFFFFFF & color));
            SystemProperties.set(GRADIENT_COLOR_PROP, hexColor);
            try {
                 mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
             } catch (RemoteException ignored) {
             }
        } else if (preference == mNavbarPicker) {
            String navbarStyle = (String) newValue;
            int navbarStyleValue = Integer.parseInt(navbarStyle);
            mNavbarPicker.setValue(String.valueOf(navbarStyleValue));
            String overlayName = getOverlayName(ThemesUtils.NAVBAR_STYLES);
                if (overlayName != null) {
                    handleOverlays(overlayName, false, mOverlayManager);
                }
                if (navbarStyleValue > 1) {
                    handleOverlays(ThemesUtils.NAVBAR_STYLES[navbarStyleValue - 2],
                            true, mOverlayManager);
            }
            mNavbarPicker.setSummary(mNavbarPicker.getEntry());
            return true;
        } else if (preference == mBrightnessSliderStyle) {
            String brightness_style = (String) newValue;
            final Context context = getContext();
            switch (brightness_style) {
                case "1":
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_A12);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MINIHALF);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_HALF);
                   break;
                case "2":
                    handleOverlays(true, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_A12);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MINIHALF);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_HALF);
                   break;
                case "3":
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(true, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_A12);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MINIHALF);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_HALF);
                   break;
                case "4":
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(true, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_A12);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MINIHALF);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_HALF);
                   break;
                case "5":
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(true, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_A12);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MINIHALF);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_HALF);
                   break;
                case "6":
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(true, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_A12);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MINIHALF);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_HALF);
                   break;
		case "7":
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
		    handleOverlays(true, context, ThemesUtils.BRIGHTNESS_SLIDER_A12);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MINIHALF);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_HALF);
                   break;
		case "8":
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_A12);
		    handleOverlays(true, context, ThemesUtils.BRIGHTNESS_SLIDER_MINIHALF);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_HALF);
                   break;
		case "9":
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MEMESTROKE);
                    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_A12);
		    handleOverlays(false, context, ThemesUtils.BRIGHTNESS_SLIDER_MINIHALF);
                    handleOverlays(true, context, ThemesUtils.BRIGHTNESS_SLIDER_HALF);
                   break;
            }
            return true;
        } else if (preference == mSystemSliderStyle) {
            String slider_style = (String) newValue;
            final Context context = getContext();
            switch (slider_style) {
                case "1":
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMESTROKE);
                   break;
                case "2":
                    handleOverlays(true, context, ThemesUtils.SYSTEM_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMESTROKE);
                   break;
                case "3":
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_DANIEL);
                    handleOverlays(true, context, ThemesUtils.SYSTEM_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMESTROKE);
                   break;
                case "4":
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEMINII);
                    handleOverlays(true, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMESTROKE);
                   break;
                case "5":
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUND);
                    handleOverlays(true, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMESTROKE);
                   break;
                case "6":
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_DANIEL);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEMINII);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUND);
                    handleOverlays(false, context, ThemesUtils.SYSTEM_SLIDER_MEMEROUNDSTROKE);
                    handleOverlays(true, context, ThemesUtils.SYSTEM_SLIDER_MEMESTROKE);
                   break;
            }
            return true;
	     } else if (preference == mCustomIcons) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.DASHBOARD_ICONS, value ? 1 : 0);
            mDashboardIcons.setEnabled(value);
            return true;
        } else if (preference == mDashboardIcons) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.THEMING_SETTINGS_DASHBOARD_ICONS, value);
            return true;
        }
        return false;
    }

   private void checkColorPreset(String colorValue) {
        List<String> colorPresets = Arrays.asList(
                getResources().getStringArray(R.array.accent_presets_values));
        if (colorPresets.contains(colorValue)) {
            mAccentPreset.setValue(colorValue);
            int index = mAccentPreset.findIndexOfValue(colorValue);
            mAccentPreset.setSummary(mAccentPreset.getEntries()[index]);
        }
        else {
            mAccentPreset.setSummary(
                    getResources().getString(R.string.custom_string));
        }
    }

    private void setupGradientPref() {
        mGradientColor = (ColorPickerPreference) findPreference(GRADIENT_COLOR);
        String colorVal = SystemProperties.get(GRADIENT_COLOR_PROP, "-1");
        int color = "-1".equals(colorVal)
                ? DEFAULT
                : Color.parseColor("#" + colorVal);
        mGradientColor.setNewPreviewColor(color);
        mGradientColor.setOnPreferenceChangeListener(this);
    }

    private void getBrightnessSliderPref() {
        mBrightnessSliderStyle = (ListPreference) findPreference(BRIGHTNESS_SLIDER_STYLE);
        mBrightnessSliderStyle.setOnPreferenceChangeListener(this);
        if (ColtUtils.isThemeEnabled("com.android.systemui.brightness.slider.half")) {
            mBrightnessSliderStyle.setValue("9");
	} else if (ColtUtils.isThemeEnabled("com.android.systemui.brightness.slider.minihalf")) {
            mBrightnessSliderStyle.setValue("8");
	} else if (ColtUtils.isThemeEnabled("com.android.systemui.brightness.slider.a12")) {
            mBrightnessSliderStyle.setValue("7");
	} else if (ColtUtils.isThemeEnabled("com.android.systemui.brightness.slider.memestroke")) {
            mBrightnessSliderStyle.setValue("6");
        } else if (ColtUtils.isThemeEnabled("com.android.systemui.brightness.slider.memeroundstroke")) {
            mBrightnessSliderStyle.setValue("5");
        } else if (ColtUtils.isThemeEnabled("com.android.systemui.brightness.slider.memeround")) {
            mBrightnessSliderStyle.setValue("4");
        } else if (ColtUtils.isThemeEnabled("com.android.systemui.brightness.slider.mememini")) {
            mBrightnessSliderStyle.setValue("3");
        } else if (ColtUtils.isThemeEnabled("com.android.systemui.brightness.slider.daniel")) {
            mBrightnessSliderStyle.setValue("2");
        } else {
            mBrightnessSliderStyle.setValue("1");
        }
    }

    private void setSystemSliderPref() {
        mSystemSliderStyle = (ListPreference) findPreference(SYSTEM_SLIDER_STYLE);
        mSystemSliderStyle.setOnPreferenceChangeListener(this);
	if (ColtUtils.isThemeEnabled("com.android.system.slider.memestroke")) {
	    mSystemSliderStyle.setValue("6");
        } else if (ColtUtils.isThemeEnabled("com.android.system.slider.memeroundstroke")) {
            mSystemSliderStyle.setValue("5");
        } else if (ColtUtils.isThemeEnabled("com.android.system.slider.memeround")) {
            mSystemSliderStyle.setValue("4");
        } else if (ColtUtils.isThemeEnabled("com.android.system.slider.mememini")) {
            mSystemSliderStyle.setValue("3");
        } else if (ColtUtils.isThemeEnabled("com.android.system.slider.daniel")) {
            mSystemSliderStyle.setValue("2");
        } else {
            mSystemSliderStyle.setValue("1");
        }
    }

    private void handleOverlays(Boolean state, Context context, String[] overlays) {
        if (context == null) {
            return;
        }
        for (int i = 0; i < overlays.length; i++) {
            String xui = overlays[i];
            try {
                mOverlayService.setEnabled(xui, state, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_reset)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.theme_option_reset_title);
        alertDialog.setMessage(R.string.theme_option_reset_message);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
        final Context context = getContext();
        mGradientColor = (ColorPickerPreference) findPreference(GRADIENT_COLOR);
        SystemProperties.set(GRADIENT_COLOR_PROP, "-1");
        mGradientColor.setNewPreviewColor(DEFAULT);
        mThemeColor = (ColorPickerPreference) findPreference(ACCENT_COLOR);
        SystemProperties.set(ACCENT_COLOR_PROP, "-1");
        mThemeColor.setNewPreviewColor(DEFAULT);
    }

    @Override
    public void onResume() {
        super.onResume();
        final Context context = getActivity();
        context.registerReceiver(mIntentReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        final Context context = getActivity();
        context.unregisterReceiver(mIntentReceiver);
        mFontPickerPreference.stopProgress();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ENIGMA;
    }

    /**
     * For Search
     */

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.colt_enigma_theme;
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
