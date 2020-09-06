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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager.widget.ViewPager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

import com.colt.settings.tabs.Statusbar;
import com.colt.settings.tabs.Buttons;
import com.colt.settings.tabs.Lockscreen;
import com.colt.settings.tabs.System;
import com.colt.settings.fragments.AboutTeam;
import com.colt.settings.bottomnav.BubbleNavigationConstraintView;
import com.colt.settings.bottomnav.BubbleNavigationChangeListener;

public class ColtSettings extends SettingsPreferenceFragment {

    private MenuItem mMenuItem;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = getActivity();
        View view = inflater.inflate(R.layout.colt, container, false);

        getActivity().setTitle(R.string.coltos_title);

        final BubbleNavigationConstraintView bubbleNavigationConstraintView =  (BubbleNavigationConstraintView) view.findViewById(R.id.bottom_navigation_view_constraint);
        final ViewPager viewPager = view.findViewById(R.id.viewpager);
        PagerAdapter mPagerAdapter = new PagerAdapter(getFragmentManager());
        viewPager.setAdapter(mPagerAdapter);

        bubbleNavigationConstraintView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                switch(view.getId()) {
                    case R.id.statusbar:
                    case R.id.buttons:
                    case R.id.lockscreen:
                    case R.id.system:
		    case R.id.aboutteam:
                         viewPager.setCurrentItem(position, true);
                    break;
                   }
               }
           });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                bubbleNavigationConstraintView.setCurrentActiveItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        setHasOptionsMenu(true);
        return view;
    }

    class PagerAdapter extends FragmentPagerAdapter {

        String titles[] = getTitles();
        private Fragment frags[] = new Fragment[titles.length];

        PagerAdapter(FragmentManager fm) {
            super(fm);
            frags[0] = new Statusbar();
            frags[1] = new Buttons();
            frags[2] = new Lockscreen();
            frags[3] = new System();
            frags[4] = new AboutTeam();
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return frags.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    private String[] getTitles() {
        String titleString[];
        titleString = new String[]{
            getString(R.string.status_bar_tab),
            getString(R.string.button_title),
	    getString(R.string.lockscreen_tab),
            getString(R.string.system_tab),
            getString(R.string.about_tab)};

        return titleString;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }
}
