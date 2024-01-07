/*
 * Copyright (C) 2022 Yet Another AOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.colt.enigma.preference;

import static android.os.UserHandle.CURRENT;
import static android.os.UserHandle.USER_CURRENT;

import android.content.Context;
import android.content.om.OverlayManager;
import android.content.om.OverlayManagerTransaction;
import android.content.om.OverlayIdentifier;
import android.content.om.OverlayInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.util.Log;

import androidx.preference.SwitchPreference;

import java.lang.SecurityException;
import java.util.List;

public class OverlaySwitchPreference extends SwitchPreference {
    private final static String TAG = "OverlaySwitchPreference";
    private final static String SETTINGSNS = "http://schemas.android.com/apk/res-auto";

    private final String mDisableKey;
    private final OverlayManager mOverlayManager;

    public OverlaySwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mDisableKey = attrs.getAttributeValue(SETTINGSNS, "dkey");
        mOverlayManager = context.getSystemService(OverlayManager.class);
    }

    public OverlaySwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OverlaySwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.switchPreferenceStyle);
    }

    public OverlaySwitchPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onAttached() {
        super.onAttached();
        if (mOverlayManager == null) return;
        OverlayInfo info = null;
        info = mOverlayManager.getOverlayInfo(getOverlayID(getKey()), CURRENT);
        if (info != null) setChecked(info.isEnabled());
    }

    @Override
    public void setChecked(boolean checked) {
        if (mOverlayManager == null) return;
        OverlayManagerTransaction.Builder transaction = new OverlayManagerTransaction.Builder();
        transaction.setEnabled(getOverlayID(getKey()), checked, USER_CURRENT);
        if (mDisableKey != null && !mDisableKey.isEmpty())
            transaction.setEnabled(getOverlayID(mDisableKey), !checked, USER_CURRENT);
        try {
            mOverlayManager.commit(transaction.build());
        } catch (SecurityException | IllegalStateException e) {
            Log.e(TAG, "Failed setting overlay(s), future logs will point the reason");
            e.printStackTrace();
            return;
        }
        super.setChecked(checked);
    }

    private OverlayIdentifier getOverlayID(String name) throws IllegalStateException {
        if (mOverlayManager == null) return null;
        if (name.contains(":")) {
            // specific overlay name in a package
            final String[] value = name.split(":");
            final String pkgName = value[0];
            final String overlayName = value[1];
            final List<OverlayInfo> infos =
                    mOverlayManager.getOverlayInfosForTarget(pkgName, CURRENT);
            for (OverlayInfo info : infos) {
                if (overlayName.equals(info.getOverlayName()))
                    return info.getOverlayIdentifier();
            }
            throw new IllegalStateException("No overlay found for " + name);
        }
        // package with only one overlay
        return mOverlayManager.getOverlayInfo(name, CURRENT).getOverlayIdentifier();
    }
}

