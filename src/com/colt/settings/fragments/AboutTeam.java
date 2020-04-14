/*
 * Copyright © 2018 LeanOS Project
 * Date: 27.11.2018
 * Time: 12:31
 * Author: @darkbeast69 <guptaaryan189@gmail.com>
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

package com.colt.settings.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.logging.nano.MetricsProto;

import com.colt.settings.fragments.common.AboutTeamAdapter;
import com.colt.settings.fragments.common.AboutTeamAdapter.About;
import com.colt.settings.fragments.common.AboutTeamAdapter.Dev;
import com.colt.settings.fragments.common.AboutTeamAdapter.Team;
import com.colt.settings.fragments.common.AboutTeamAdapter.Header;
import com.colt.settings.fragments.common.AboutTeamAdapter.OnClickListener;

import java.util.ArrayList;
import java.util.List;

public class AboutTeam extends SettingsPreferenceFragment {

	private List<AboutTeamAdapter.About> list = new ArrayList<>();

	@Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.about_team, null);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.about_team_title);
        initList();

        RecyclerView recyclerView = getView().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new AboutTeamAdapter(list, new AboutTeamAdapter.OnClickListener() {
            @Override
            public void OnClick(String url) {
                if (!url.isEmpty()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
            }
        }));
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.COLT;
    }

    private void initList() {
        List<AboutTeamAdapter.Dev> team = new ArrayList<>();
        team.add(new AboutTeamAdapter.Dev(
                "Rakesh Batra",
                "Founder | Core Developer",
                "https://avatars3.githubusercontent.com/rakeshbatra",
                "https://forum.xda-developers.com/member.php?u=5985430",
                "https://github.com/rakeshbatra",
                "https://t.me/RakeshBatra"
        ));
	team.add(new AboutTeamAdapter.Dev(
                "Nitinkumar Chobhe",
                "Colt Developer",
                "https://avatars3.githubusercontent.com/nitin1438",
                "https://forum.xda-developers.com/member.php?u=5044214",
                "https://github.com/nitin1438",
                "https://t.me/nitin1438"
        ));
        team.add(new AboutTeamAdapter.Dev(
                "Roger T",
                "Man Behind The Beautification | Core Team",
                "https://avatars3.githubusercontent.com/ROGERDOTT",
                "",
                "https://github.com/ROGERDOTT",
                "https://t.me/Roger_T"
        ));
	team.add(new AboutTeamAdapter.Dev(
                "Mady51",
                "Kernel Developer",
                "https://avatars3.githubusercontent.com/mady51",
                "https://forum.xda-developers.com/member.php?u=7072514",
                "",
                "https://t.me/Mady51"
        ));
        list.add(new AboutTeamAdapter.Team(
                        "https://github.com/Colt-Enigma",
                        "https://t.me/ColtEnigma",
                        team

                )
        );
        list.add(new AboutTeamAdapter.Header());
        list.add(new AboutTeamAdapter.Maintainer(
                        "OnePlus 3/3T",
                        new AboutTeamAdapter.Dev(
                                "Rakesh Batra",
                                "",
                                "https://avatars3.githubusercontent.com/rakeshbatra",
                                "https://forum.xda-developers.com/oneplus-3/oneplus-3--3t-cross-device-development/rom-coltos-t3808635",
                                "https://github.com/rakeshbatra",
                                ""
                        )
                )
        );
	list.add(new AboutTeamAdapter.Maintainer(
                        "Google Pixel 2 XL (Taimen)",
                        new AboutTeamAdapter.Dev(
                                "Nitin1438",
                                "",
                                "https://avatars3.githubusercontent.com/nitin1438",
                                "https://forum.xda-developers.com/pixel-2-xl/development/rom-colt-os-enigma-taimen-t3911826",
                                "https://github.com/nitin1438",
                                ""
                        )
                )
        );
	list.add(new AboutTeamAdapter.Maintainer(
                        "Xiaomi Redmi K20 Pro (Raphael)",
                        new AboutTeamAdapter.Dev(
                                "Ritzz",
                                "",
                                "https://i.postimg.cc/wB7mT1mT/photo1527457720019560381.jpg",
                                "https://forum.xda-developers.com/k20-pro/development/rom-coltos-v5-3-t4016411",
                                "https://github.com/riteshm321",
                                ""
                        )
                )
        );
        list.add(new AboutTeamAdapter.Maintainer(
                        "Xiaomi MI A2 (Jasmine_Sprout)",
                        new AboutTeamAdapter.Dev(
                                "Siddharth Bharadwaj",
                                "",
                                "https://avatars3.githubusercontent.com/SiddharthBharadwaj",
                                "https://forum.xda-developers.com/mi-a2/development/rom-colt-enigma-v5-3-ten-t4005401",
                                "https://github.com/SiddharthBharadwaj",
                                ""
                        )
                )
        );
	list.add(new AboutTeamAdapter.Maintainer(
                        "Oneplus 5t (Dumpling)",
                        new AboutTeamAdapter.Dev(
                                "mukesh22584",
                                "",
                                "https://avatars3.githubusercontent.com/mukesh22584",
                                "https://forum.xda-developers.com/oneplus-5t/development/rom-colt-enigma-v5-3-t4005957",
                                "https://github.com/mukesh22584",
                                ""
                        )
                )
        );
	list.add(new AboutTeamAdapter.Maintainer(
                        "Asus Zenfone Max Pro M2 (X01BD)",
                        new AboutTeamAdapter.Dev(
                                "Sonal Singh",
                                "",
                                "https://avatars3.githubusercontent.com/SonalSingh18",
                                "https://forum.xda-developers.com/max-pro-m2/development/rom-colt-enigmav4-4-t3951353",
                                "https://github.com/SonalSingh18",
                                ""
                        )
                )
        );
	list.add(new AboutTeamAdapter.Maintainer(
                        "Xiaomi Pocophone F1 (Beryllium)",
                        new AboutTeamAdapter.Dev(
                                "Mani-Madhuri",
                                "",
                                "https://avatars3.githubusercontent.com/Joker-commits",
                                "",
                                "https://github.com/Joker-commits",
                                ""
                        )
                )
        );
	list.add(new AboutTeamAdapter.Maintainer(
                        "Redmi Note 5 Pro (Whyred)",
                        new AboutTeamAdapter.Dev(
                                "Ayush Kakkar",
                                "",
                                "https://avatars3.githubusercontent.com/Ayush12ka",
                                "",
                                "https://github.com/Ayush12ka",
                                ""
                        )
                )
        );
	list.add(new AboutTeamAdapter.Maintainer(
                        "Redmi Note 7 Pro (Violet)",
                        new AboutTeamAdapter.Dev(
                                "Athul Dinesan",
                                "",
                                "https://avatars3.githubusercontent.com/athul05",
                                "",
                                "https://github.com/athul05",
                                ""
                        )
                )
        );
	list.add(new AboutTeamAdapter.Maintainer(
                        "Santoni (Redmi 4x)",
                        new AboutTeamAdapter.Dev(
                                "Aman Dwivedi",
                                "",
                                "https://avatars3.githubusercontent.com/amandwivedi0",
                                "https://forum.xda-developers.com/xiaomi-redmi-4x/development/rom-coltos-5-6-t4049845",
                                "https://github.com/amandwivedi0",
                                ""
                        )
                )
        );
	list.add(new AboutTeamAdapter.Maintainer(
                        "Onclite (Redmi 7)",
                        new AboutTeamAdapter.Dev(
                                "Gagan Malvi",
                                "",
                                "https://avatars3.githubusercontent.com/TheCynicalDev",
                                "https://forum.xda-developers.com/redmi-7/development/rom-coltos-t4063441",
                                "https://github.com/TheCynicalDev",
                                ""
                        )
                )
        );
	list.add(new AboutTeamAdapter.Maintainer(
                        "Tulip (Redmi Note 6 Pro)",
                        new AboutTeamAdapter.Dev(
                                "Mahi Pawar",
                                "",
                                "https://avatars3.githubusercontent.com/pawar-mahesh",
                                "https://forum.xda-developers.com/redmi-note-6-pro/development/rom-coltos-v5-7-t4063293",
                                "https://github.com/pawar-mahesh",
                                ""
                        )
                )
        );
    }
}
