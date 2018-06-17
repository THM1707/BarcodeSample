package thm.com.barcodesample.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import thm.com.barcodesample.R;
import thm.com.barcodesample.adapters.MyPagerAdapter;
import thm.com.barcodesample.fragments.ProductFragment;
import thm.com.barcodesample.fragments.ScanFragment;
import thm.com.barcodesample.utils.Constants;
import thm.com.barcodesample.views.ZoomOutPageTransformer;

public class HomeActivity extends AppCompatActivity {
    private static final int PRODUCT = 0;
    private static final int SCAN = 1;
    private static final int HISTORY = 2;
    private boolean isAllowed;
    private ViewPager mPager;
    private BottomNavigationView mBottomNavigationView;
    private MyPagerAdapter mPagerAdapter;

    public static void start(Context context, boolean allowed) {
        Intent homeIntent = new Intent(context, HomeActivity.class);
        homeIntent.putExtra(Constants.EXTRA_IS_ALLOWED, allowed);
        context.startActivity(homeIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            isAllowed = getIntent().getBooleanExtra(Constants.EXTRA_IS_ALLOWED, false);
        }
        setContentView(R.layout.activity_home);
        initViews();
    }

    private void initViews() {
        mPager = findViewById(R.id.pager_home);
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        setupPager();
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        mBottomNavigationView.setOnNavigationItemSelectedListener(
            item -> {
                switch (item.getItemId()) {
                    case R.id.menu_product:
                        mPager.setCurrentItem(0);
                        break;
                    case R.id.menu_scan:
                        mPager.setCurrentItem(1);
                        break;
                    case R.id.menu_history:
                        mPager.setCurrentItem(2);
                        break;
                }
                return false;
            });
    }

    private void setupPager() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(ProductFragment.newInstance(1));
        fragments.add(ScanFragment.newInstance(isAllowed));
        fragments.add(ProductFragment.newInstance(2));
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.setFragmentList(fragments);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                int menuId = -1;
                switch (position) {
                    case PRODUCT:
                        menuId = R.id.menu_product;
                        break;
                    case SCAN:
                        menuId = R.id.menu_scan;
                        break;
                    case HISTORY:
                        menuId = R.id.menu_history;
                        break;
                }
                if (menuId != -1) {
                    updateNavigationBarState(menuId);
                    mBottomNavigationView.setSelectedItemId(menuId);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void updateNavigationBarState(int actionId) {
        int currentId = mBottomNavigationView.getSelectedItemId();
        if (currentId != actionId) {
            mBottomNavigationView.getMenu().findItem(currentId).setChecked(false);
            mBottomNavigationView.getMenu().findItem(actionId).setChecked(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }
}
