package com.auctionapp.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.auctionapp.Constants;
import com.auctionapp.R;
import com.auctionapp.dao.DBHelper;
import com.auctionapp.model.UserBid;

import java.sql.SQLException;
import java.util.List;

public class AuctionBidsActivity extends AppCompatActivity {
    private ListView listAuctionBids;
    private List<UserBid> bids;
    private UserBidsAdapter bidsAdapter;
    private TextView emptyText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auction_bids);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        emptyText = (TextView) findViewById(R.id.empty);
        listAuctionBids = (ListView) findViewById(R.id.auctionBidsList);
        long auctionId = getIntent().getLongExtra(Constants.AUCTION_ITEM_ID, -1);
        String title = getIntent().getStringExtra(Constants.AUCTION_NAME);
        getSupportActionBar().setTitle(String.format(getString(R.string.title_activity_auction_bids), title));
        emptyText.setText(String.format(getString(R.string.no_bid_for), title));

        if (auctionId == -1) {
            finish();
        } else {
            try {
                bids = DBHelper.getDBHelper(this).getAuctionBids(auctionId);
            } catch (SQLException ex) {
            } finally {
                DBHelper.releaseHelper();
            }
        }
        showAuctionBids();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAuctionBids() {
        if (bids == null || bids.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            listAuctionBids.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            listAuctionBids.setVisibility(View.VISIBLE);
            bidsAdapter = new UserBidsAdapter();
            listAuctionBids.setAdapter(bidsAdapter);
        }
    }

    private class UserBidsAdapter extends BaseAdapter {

        LayoutInflater inflater;

        public UserBidsAdapter() {
            inflater = LayoutInflater.from(AuctionBidsActivity.this);
        }

        @Override
        public int getCount() {
            return bids.size();
        }

        @Override
        public UserBid getItem(int position) {
            return bids.get(position);
        }

        @Override
        public long getItemId(int position) {
            return bids.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            UserBid bid = getItem(position);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.bids_list_item, null);
                holder = new ViewHolder();
                holder.amount = (TextView) convertView.findViewById(R.id.tvBidAmount);
                holder.notes = (TextView) convertView.findViewById(R.id.tvBidNotes);
                holder.bidder = (TextView) convertView.findViewById(R.id.tvBidder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.bidder.setText(String.format(getString(R.string.bid_by), bid.getBidder().getEmail()));
            holder.amount.setText(String.format(getString(R.string.bid_amount), bid.getQuote()));
            if (bid.getDescription() != null && bid.getDescription().length() > 0) {
                holder.notes.setText(bid.getDescription());
            } else {
                holder.notes.setText("NA");
            }
            convertView.setTag(holder);
            return convertView;
        }

        class ViewHolder {
            TextView amount, notes, bidder;
        }
    }
}
