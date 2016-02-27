package com.auctionapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.auctionapp.Preferences;
import com.auctionapp.R;
import com.auctionapp.dao.DBHelper;
import com.auctionapp.fragment.AuctionWonFragment;
import com.auctionapp.fragment.AuctionsFragment;
import com.auctionapp.fragment.MyAuctionsFragment;

public class HomeActivity extends AppCompatActivity {
    private FragmentTabHost mTabHost;

    public static final String TAG_IDS[] = {"Home", "My Auctions", "Won"};

    private Class fragmentArray[] =
            {AuctionsFragment.class, MyAuctionsFragment.class, AuctionWonFragment.class};

    private int mImageViewArray[] =
            {R.drawable.tab_home_btn, R.drawable.tab_auction_btn, R.drawable.tab_selfinfo_btn};
    private LayoutInflater layoutInflater;
    private FloatingActionButton floatingActionButton;

    private static final int CREATE_AUCTION_REQUEST = 1002;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        layoutInflater = LayoutInflater.from(this);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HomeActivity.this, CreateAuctionActivity.class);
                startActivityForResult(i, CREATE_AUCTION_REQUEST);
            }
        });

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        int count = fragmentArray.length;

        for (int i = 0; i < count; i++) {
            View view = getTabItemView(i);
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(TAG_IDS[i]).setIndicator(view);
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
        }

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (mTabHost.getCurrentTab() == 0) {
                    floatingActionButton.setVisibility(View.VISIBLE);
                } else {
                    floatingActionButton.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            new Preferences(this).saveLoggedUserId(-1);
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            mTabHost.setCurrentTab(1);
        } else if (requestCode == 101) {
            mTabHost.setCurrentTab(2);
        }
    }

    private View getTabItemView(int index) {
        View view = layoutInflater.inflate(R.layout.tab_item_view, null);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
        imageView.setImageResource(mImageViewArray[index]);

        TextView textView = (TextView) view.findViewById(R.id.textview);
        textView.setText(TAG_IDS[index]);
        return view;
    }
}
