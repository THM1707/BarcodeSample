package thm.com.barcodesample.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import thm.com.barcodesample.fragments.ProductFragment;
import thm.com.barcodesample.fragments.ScanFragment;
import thm.com.barcodesample.utils.Constants;

public class MyPagerAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> mFragmentList;
    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return Constants.NUM_PAGES;
    }

    public void setFragmentList(List<Fragment> fragmentList) {
        mFragmentList = fragmentList;
    }
}
