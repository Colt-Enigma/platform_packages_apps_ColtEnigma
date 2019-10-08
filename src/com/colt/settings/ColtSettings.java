/*
 * Copyright (C) 2017 ColtOS Project
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

package com.colt.settings;

import android.os.Bundle;
import androidx.preference.PreferenceScreen;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.PagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.colt.settings.customtab.IconTitleIndicator;
import com.colt.settings.customtab.Indicatorable;
import com.colt.settings.fragments.StatusBarSettings;
import com.colt.settings.fragments.QuickSettings;
import com.colt.settings.fragments.ButtonSettings;
import com.colt.settings.fragments.GestureSettings;
import com.colt.settings.fragments.LockScreenSettings;
import com.colt.settings.fragments.NavbarSettings;
import com.colt.settings.fragments.PowerMenuSettings;
import com.colt.settings.fragments.System;
import com.colt.settings.fragments.About;


import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.SettingsPreferenceFragment;
import com.colt.settings.utils.Utils;

public class ColtSettings extends SettingsPreferenceFragment {

    private static final String TAG = "ColtSettings";

    private IconTitleIndicator mIndicator;
    private ViewPager mViewpager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.colt, container, false);

        mIndicator = (IconTitleIndicator) view.findViewById(R.id.tabs);
        mViewpager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewpager.setAdapter(new MyAdapter(getFragmentManager()));
        init1();

		return view;
    }

    private void init1() {
        mIndicator.setTextSize(12);
        mIndicator.setTextColorResId(R.color.selector_tab);
        mIndicator.setIconWidthHeight(50);
        mIndicator.setItemPaddingTop(15);
        mIndicator.setViewPager(mViewpager);
    }

    class MyAdapter extends FragmentPagerAdapter implements Indicatorable.IconPageAdapter {
        String titles[] = getTitles();
        private Fragment frags[] = new Fragment[titles.length];

        public MyAdapter(FragmentManager fm) {
            super(fm);
	    frags[0] = new StatusBarSettings();
	    frags[1] = new QuickSettings();
	    frags[2] = new ButtonSettings();
	    frags[3] = new GestureSettings();
	    frags[4] = new LockScreenSettings();
            frags[5] = new NavbarSettings();
	    frags[6] = new PowerMenuSettings();
            frags[7] = new System();
            frags[8] = new About();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return frags.length;
        }

        public int getIconResId(int position) {
            return icons[position];
        }

    }

    private String[] getTitles() {
        String titleString[];
        titleString = new String[]{
            getString(R.string.statusbar_settings_title),
            getString(R.string.quicksettings_title),
            getString(R.string.button_title),
	    getString(R.string.gestures_title),
	    getString(R.string.lockscreen_settings_title),
	    getString(R.string.navbar_title),
	    getString(R.string.powermenu_title),
            getString(R.string.system_tab),
            getString(R.string.about_tab)};
        return titleString;
    }

    private int icons[] = {
            R.drawable.statusbar_tab,
	    R.drawable.quickSettings_tab,
	    R.drawable.buttons_tab,
	    R.drawable.gestures_tab,
	    R.drawable.lockscreen_tab,
	    R.drawable.navbar_tab,
            R.drawable.power_tab,
            R.drawable.system_tab,
            R.drawable.about_tab};

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }
}

