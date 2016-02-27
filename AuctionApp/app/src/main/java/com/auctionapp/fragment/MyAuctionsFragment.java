package com.auctionapp.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.auctionapp.AuctionApplication;
import com.auctionapp.Constants;
import com.auctionapp.R;
import com.auctionapp.activity.AuctionBidsActivity;
import com.auctionapp.adapter.AuctionsAdapter;
import com.auctionapp.dao.DBHelper;
import com.auctionapp.model.AuctionItem;
import com.auctionapp.model.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.sql.SQLException;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MyAuctionsFragment extends Fragment {
    private TextView emptyText;
    private ListView myAuctionsList;
    private List<AuctionItem> auctionItems;
    private AuctionsAdapter auctionsAdapter;
    DBHelper dbHelper;

    public MyAuctionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DBHelper.getDBHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_auctions, null);
        emptyText = (TextView) rootView.findViewById(R.id.empty);
        myAuctionsList = (ListView) rootView.findViewById(R.id.myAuctionsList);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.title_my_auction));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getString(R.string.title_my_auction));
        try {
            User user = ((AuctionApplication) getActivity().getApplication()).getLoggedUser();
            auctionItems = dbHelper.getMyAuctions(user);
        }
        catch (SQLException ex) {
        } finally {
        }
        showMyAuctions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DBHelper.releaseHelper();
    }

    private void showMyAuctions() {
        if (auctionItems == null || auctionItems.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            myAuctionsList.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            myAuctionsList.setVisibility(View.VISIBLE);
            Context context = getActivity().getApplicationContext();
            auctionsAdapter = new AuctionsAdapter(context, auctionItems);
            myAuctionsList.setAdapter(auctionsAdapter);
            myAuctionsList.setOnItemClickListener(onItemClick);
        }
    }

    private AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getActivity(), AuctionBidsActivity.class);
            AuctionItem auctionItem = auctionItems.get(position);
            intent.putExtra(Constants.AUCTION_ITEM_ID, auctionItem.getId());
            intent.putExtra(Constants.AUCTION_NAME, auctionItem.getName());
            getActivity().startActivityForResult(intent, 100);
        }
    };
}
