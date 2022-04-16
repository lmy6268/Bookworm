package com.example.bookworm.User;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.bookworm.Follow.fragment_profile_follower;
import com.example.bookworm.Follow.fragment_profile_following;

import java.util.ArrayList;

public class ViewpagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> arrayList = new ArrayList<>();
    private ArrayList<String> name = new ArrayList<>();

    public ViewpagerAdapter(@NonNull FragmentManager fm,String token)
    {
        super(fm);
        arrayList.add(new fragment_profile_follower(token));
        arrayList.add(new fragment_profile_following(token));

        name.add("팔로워 탭");
        name.add("팔로잉 탭");
    }

    @NonNull
    @Override
    public CharSequence getPageTitle(int positon)
    {
        return name.get(positon);
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        return arrayList.get(position);
    }

    @Override
    public int getCount()
    {
        return arrayList.size();
    }
}