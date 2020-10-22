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

package com.colt.enigma;

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
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentPagerAdapter;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

import com.colt.enigma.tabs.Statusbar;
import com.colt.enigma.tabs.Buttons;
import com.colt.enigma.tabs.Lockscreen;
import com.colt.enigma.tabs.System;


import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.SettingsPreferenceFragment;
import com.colt.enigma.utils.Utils;

import com.colt.enigma.bottomnav.BubbleNavigationConstraintView;
import com.colt.enigma.bottomnav.BubbleNavigationChangeListener;

public class ColtEnigma extends SettingsPreferenceFragment {

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
		int id = view.getId();
		if (id == R.id.statusbar){
		viewPager.setCurrentItem(position, true);
		} else if (id == R.id.buttons){
		viewPager.setCurrentItem(position, true);
		} else if (id == R.id.lockscreen){
		viewPager.setCurrentItem(position, true);
		} else if (id == R.id.system){
		viewPager.setCurrentItem(position, true);
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
            getString(R.string.system_tab)};

        return titleString;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 0, 0, R.string.colt_about_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

    AboutColt newFragment = AboutColt .newInstance();
            newFragment.show(ft, "AboutColt");
            return true;
        }
        return false;
    }
}
