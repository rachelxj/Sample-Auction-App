package com.auctionapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.auctionapp.AuctionApplication;
import com.auctionapp.Constants;
import com.auctionapp.R;
import com.auctionapp.activity.AuctionItemActivity;
import com.auctionapp.adapter.AuctionsAdapter;
import com.auctionapp.dao.DBHelper;
import com.auctionapp.model.AuctionItem;
import com.auctionapp.model.User;

import java.sql.SQLException;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * create an instance of this fragment.
 */
public class AuctionsFragment extends Fragment {
    private TextView emptyText;
    private ListView auctionList;
    private List<AuctionItem> openingItems;
    private List<AuctionItem> comingItems;
    private AuctionsAdapter openingAdapter, comingAdapter;
    private SegmentedGroup segmentedGroup;
    private DBHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DBHelper.getDBHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auctions, null);
        emptyText = (TextView) view.findViewById(R.id.empty);
        auctionList = (ListView) view.findViewById(R.id.auctionList);
        segmentedGroup = (SegmentedGroup) view.findViewById(R.id.segmented2);
        segmentedGroup.setTintColor(getResources().getColor(R.color.colorBlue));
        segmentedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                showOpenAuctions();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_activity_home);
        showOpenAuctions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DBHelper.releaseHelper();
    }

    private void showOpenAuctions() {
        if (dbHelper != null) {
            try {
                User user = ((AuctionApplication) getActivity().getApplication()).getLoggedUser();
                openingItems = dbHelper.getOpenAuctions(user);
                comingItems = dbHelper.getComingAuctions(user);
            } catch (SQLException e) {
            }
        }
        List<AuctionItem> items = null;
        if (segmentedGroup.getCheckedRadioButtonId() == R.id.rbOpening) {
            items = openingItems;
        } else {
            items = comingItems;
        }
        if (items == null || items.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            auctionList.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            auctionList.setVisibility(View.VISIBLE);
            Context context = getActivity().getApplicationContext();
            if (segmentedGroup.getCheckedRadioButtonId() == R.id.rbOpening) {
                openingAdapter = new AuctionsAdapter(context, openingItems);
                auctionList.setAdapter(openingAdapter);
            } else {
                comingAdapter = new AuctionsAdapter(context, comingItems, true);
                auctionList.setAdapter(comingAdapter);
            }
            auctionList.setOnItemClickListener(onItemClick);
        }
    }

    private AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent intent = new Intent(getActivity(), AuctionItemActivity.class);
            intent.putExtra(Constants.AUCTION_ITEM_ID, id);
            startActivity(intent);
        }
    };

}
