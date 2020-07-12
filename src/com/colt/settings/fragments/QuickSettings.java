package com.colt.settings.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.ArrayList;

import com.colt.settings.preference.CustomSeekBarPreference;
import com.colt.settings.preference.SystemSettingSeekBarPreference;
import com.colt.settings.preference.SystemSettingEditTextPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String KEY_QS_PANEL_ALPHA = "qs_panel_alpha";
    private static final String QS_PANEL_COLOR = "qs_panel_color";
    private static final String QS_HEADER_CLOCK_SIZE  = "qs_header_clock_size";
    private static final String QS_HEADER_CLOCK_FONT_STYLE  = "qs_header_clock_font_style";

    private static final String QS_BLUR_INTENSITY = "qs_blur_intensity";
    private static final String X_FOOTER_TEXT_STRING = "x_footer_text_string";
    private static final String PREF_SMART_PULLDOWN = "smart_pulldown";

    static final int DEFAULT_QS_PANEL_COLOR = 0xffffffff;

    private ColorPickerPreference mQsPanelColor;
    private ListPreference mClockFontStyle;
    private SystemSettingSeekBarPreference mQsPanelAlpha;
    private CustomSeekBarPreference mQsBlurIntensity;
    private SystemSettingEditTextPreference mFooterString;
    private CustomSeekBarPreference mQsClockSize;

    private static final String QUICK_PULLDOWN = "quick_pulldown";

    private ListPreference mQuickPulldown;
    private ListPreference mSmartPulldown;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.colt_settings_quicksettings);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

	mQsPanelAlpha = (SystemSettingSeekBarPreference) findPreference(KEY_QS_PANEL_ALPHA);
        int qsPanelAlpha = Settings.System.getInt(getContentResolver(),
                Settings.System.OMNI_QS_PANEL_BG_ALPHA, 225);
        mQsPanelAlpha.setValue((int)(((double) qsPanelAlpha / 255) * 100));
        mQsPanelAlpha.setOnPreferenceChangeListener(this);

	mQsPanelColor = (ColorPickerPreference) findPreference(QS_PANEL_COLOR);
        mQsPanelColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QS_PANEL_BG_COLOR, DEFAULT_QS_PANEL_COLOR, UserHandle.USER_CURRENT);
        String hexColor = String.format("#%08x", (0xffffffff & intColor));
        mQsPanelColor.setSummary(hexColor);
        mQsPanelColor.setNewPreviewColor(intColor);

	mQsClockSize = (CustomSeekBarPreference) findPreference(QS_HEADER_CLOCK_SIZE);
        int qsClockSize = Settings.System.getInt(resolver,
                Settings.System.QS_HEADER_CLOCK_SIZE, 14);
                mQsClockSize.setValue(qsClockSize / 1);
        mQsClockSize.setOnPreferenceChangeListener(this);

        mClockFontStyle = (ListPreference) findPreference(QS_HEADER_CLOCK_FONT_STYLE);
        int showClockFont = Settings.System.getInt(resolver,
                Settings.System.QS_HEADER_CLOCK_FONT_STYLE, 14);
        mClockFontStyle.setValue(String.valueOf(showClockFont));
        mClockFontStyle.setOnPreferenceChangeListener(this);

	mQuickPulldown = (ListPreference) findPreference(QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        int quickPulldownValue = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 0, UserHandle.USER_CURRENT);
        mQuickPulldown.setValue(String.valueOf(quickPulldownValue));
        updatePulldownSummary(quickPulldownValue);

	mSmartPulldown = (ListPreference) findPreference(PREF_SMART_PULLDOWN);
        mSmartPulldown.setOnPreferenceChangeListener(this);
        int smartPulldown = Settings.System.getInt(resolver,
                Settings.System.QS_SMART_PULLDOWN, 0);
        mSmartPulldown.setValue(String.valueOf(smartPulldown));
        updateSmartPulldownSummary(smartPulldown);

	mQsBlurIntensity = (CustomSeekBarPreference) findPreference(QS_BLUR_INTENSITY);
        int qsBlurIntensity = Settings.System.getIntForUser(resolver,
                Settings.System.QS_BLUR_INTENSITY, 100, UserHandle.USER_CURRENT);
        mQsBlurIntensity.setValue(qsBlurIntensity);
        mQsBlurIntensity.setOnPreferenceChangeListener(this);

	mFooterString = (SystemSettingEditTextPreference) findPreference(X_FOOTER_TEXT_STRING);
        mFooterString.setOnPreferenceChangeListener(this);
        String footerString = Settings.System.getString(getContentResolver(),
                X_FOOTER_TEXT_STRING);
        if (footerString != null && footerString != "")
            mFooterString.setText(footerString);
        else {
            mFooterString.setText("ColtOS");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.X_FOOTER_TEXT_STRING, "ColtOS");
        }

        }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	ContentResolver resolver = getActivity().getContentResolver();
	if (preference == mQsPanelAlpha) {
            int bgAlpha = (Integer) newValue;
            int trueValue = (int) (((double) bgAlpha / 100) * 255);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.OMNI_QS_PANEL_BG_ALPHA, trueValue);
            return true;
	} else if (preference == mQuickPulldown) {
            int quickPulldownValue = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(resolver, Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN,
                    quickPulldownValue, UserHandle.USER_CURRENT);
            updatePulldownSummary(quickPulldownValue);
            return true;
	} else if (preference == mSmartPulldown) {
            int smartPulldown = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver, Settings.System.QS_SMART_PULLDOWN, smartPulldown);
            updateSmartPulldownSummary(smartPulldown);
            return true;
	} else if (preference == mQsPanelColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QS_PANEL_BG_COLOR, intHex, UserHandle.USER_CURRENT);
            return true;
	}  else if (preference == mQsClockSize) {
                int width = ((Integer)newValue).intValue();
                Settings.System.putInt(getContentResolver(),
                        Settings.System.QS_HEADER_CLOCK_SIZE, width);
                return true;
        } else if (preference == mClockFontStyle) {
                int showClockFont = Integer.valueOf((String) newValue);
                int index = mClockFontStyle.findIndexOfValue((String) newValue);
                Settings.System.putInt(getContentResolver(), Settings.System.
                    QS_HEADER_CLOCK_FONT_STYLE, showClockFont);
                mClockFontStyle.setSummary(mClockFontStyle.getEntries()[index]);
                return true;
	} else if (preference == mQsBlurIntensity) {
            int valueInt = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.QS_BLUR_INTENSITY, valueInt);
            return true;
	} else if (preference == mFooterString) {
            String value = (String) newValue;
            if (value != "" && value != null)
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.X_FOOTER_TEXT_STRING, value);
            else {
                mFooterString.setText("MSM-Xtended");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.X_FOOTER_TEXT_STRING, "MSM-Xtended");
            }
            return true;
	}
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }

    private void updatePulldownSummary(int value) {
        Resources res = getResources();
         if (value == 0) {
            // quick pulldown deactivated
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_off));
        } else if (value == 3) {
            // quick pulldown always
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_summary_always));
        } else {
            String direction = res.getString(value == 2
                    ? R.string.quick_pulldown_left
                    : R.string.quick_pulldown_right);
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_summary, direction));
        }
    }

     private void updateSmartPulldownSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // Smart pulldown deactivated
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_off));
        } else if (value == 3) {
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_none_summary));
        } else {
            String type = res.getString(value == 1
                    ? R.string.smart_pulldown_dismissable
                    : R.string.smart_pulldown_ongoing);
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_summary, type));
        }
    }
}
