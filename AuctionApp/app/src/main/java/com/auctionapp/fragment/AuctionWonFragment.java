package com.auctionapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.auctionapp.activity.AuctionItemActivity;
import com.auctionapp.adapter.AuctionsAdapter;
import com.auctionapp.dao.DBHelper;
import com.auctionapp.model.AuctionItem;
import com.auctionapp.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * create an instance of this fragment.
 */
public class AuctionWonFragment extends Fragment {
    private TextView emptyText;
    private ListView wonAuctionList;
    private List<AuctionItem> auctionItems;
    private AuctionsAdapter auctionsAdapter;

    public AuctionWonFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.title_won_auction));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_auction_won, null);
        emptyText = (TextView) rootView.findViewById(R.id.empty);
        wonAuctionList = (ListView) rootView.findViewById(R.id.wonAuctionList);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getString(R.string.title_won_auction));
        try {
            User user = ((AuctionApplication) getActivity().getApplication()).getLoggedUser();
            auctionItems = DBHelper.getDBHelper(getActivity()).getWonAuctions(user);
        } catch (SQLException ex) {
        }
        showAuctionsWon();
    }

    private void showAuctionsWon() {
        if (auctionItems == null || auctionItems.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            wonAuctionList.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            wonAuctionList.setVisibility(View.VISIBLE);
            Context context = getActivity().getApplicationContext();
            auctionsAdapter = new AuctionsAdapter(context, auctionItems);
            wonAuctionList.setAdapter(auctionsAdapter);
            wonAuctionList.setOnItemClickListener(onItemClick);
        }
    }

    private AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AuctionItem auctionItem = auctionItems.get(position);
            Intent intent = new Intent(getActivity(), AuctionItemActivity.class);
            intent.putExtra(Constants.AUCTION_ITEM_ID, auctionItem.getId());
            intent.putExtra(Constants.FROM_WON, true);
            getActivity().startActivityForResult(intent, 101);
        }
    };

//    private User getOwner(long ownerId) {
//        User user = DBHelper.getDBHelper(getActivity()).getUserRuntimeDao().queryForId(ownerId);
//        return user;
//    }
}
