package com.auctionapp.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.auctionapp.AuctionUtil;
import com.auctionapp.Constants;
import com.auctionapp.R;
import com.auctionapp.dao.DBHelper;
import com.auctionapp.fragment.BidActionFragment;
import com.auctionapp.model.AuctionItem;
import com.auctionapp.model.AuctionPhoto;
import com.auctionapp.model.User;
import com.auctionapp.model.UserBid;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

public class AuctionItemActivity extends AuctionAppActivity implements View.OnClickListener, BidActionFragment.BidActionListener {
    private AuctionItem auctionItem;
    private TextView timerText;
    private TextView tvStatus;
    private ImageLoader imageLoader;
    private ImageButton btnBidNow;
    private BidCloseTimer bidCloseTimer;
    private UserBid higestBid;
    private UserBid userBid;
    private DisplayImageOptions options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.default_bid_image)
            .showImageOnFail(R.drawable.default_bid_image).showImageOnLoading(R.drawable.default_bid_image)
            .displayer(new FadeInBitmapDisplayer(500)).cacheOnDisk(true).handler(new Handler()).build();
    DBHelper dbHelper;
    private boolean fromWon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auction_item);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_activity_auction_item);
        dbHelper = DBHelper.getDBHelper(this);
        long itemId = getIntent().getLongExtra(Constants.AUCTION_ITEM_ID, -1);
        fromWon = getIntent().getBooleanExtra(Constants.FROM_WON, false);
        auctionItem = dbHelper.getItemRuntimeDao().queryForId(itemId);
        if (auctionItem == null) {
            finish();
        } else {
            imageLoader = ImageLoader.getInstance();
            try {
                User user = getUser();
                higestBid = dbHelper.getHighestBid(itemId);
                if (higestBid != null && higestBid.getBidder().getId() == user.getId()) {
                    userBid = higestBid;
                } else {
                    userBid = dbHelper.getUserBid(itemId, user.getId());
                }
            } catch (SQLException ex) {
                higestBid = null;
            }
            btnBidNow = (ImageButton) findViewById(R.id.btnBid);
            btnBidNow.setOnClickListener(this);
            TextView textView = (TextView) findViewById(R.id.tvTitle);
            textView.setText(auctionItem.getName());
            textView = (TextView) findViewById(R.id.tvDescription);
            textView.setText(auctionItem.getDescription());
            textView = (TextView) findViewById(R.id.tvBasePrice);
            textView.setText(String.format("%.2f", auctionItem.getStartPrice()));
            tvStatus = (TextView) findViewById(R.id.tvStatus);
            ImageView imageView = (ImageView) findViewById(R.id.ivItem);
            Collection<AuctionPhoto> auctionPhotos = auctionItem.getAuctionPhotos();
            if (auctionPhotos != null) {
                Iterator<AuctionPhoto> iterator = auctionPhotos.iterator();
                while (iterator.hasNext()) {
                    AuctionPhoto photo = iterator.next();

                    imageLoader.displayImage(Uri.fromFile(new File(photo.getPath())).toString(), imageView, options);
                    break;
                }
            }
            timerText = (TextView) findViewById(R.id.tvEndTimer);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // not started yet
        if (auctionItem.getStartTime().getTime() - Calendar.getInstance().getTimeInMillis() > 0) {
            btnBidNow.setVisibility(View.GONE);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText(String.format(getString(R.string.bidding_starts_at), AuctionUtil.formatDate(auctionItem.getStartTime())));
        }
        else if (userBid == null) {
            btnBidNow.setEnabled(true);
            long millisInFuture = auctionItem.getEndTime().getTime() - Calendar.getInstance().getTimeInMillis();
            if (millisInFuture > 0) {
                bidCloseTimer = new BidCloseTimer(millisInFuture, 1000);
                bidCloseTimer.start();
            } else {
                btnBidNow.setVisibility(View.GONE);
                tvStatus.setVisibility(View.VISIBLE);
                if (higestBid != null) {
                    tvStatus.setText(String.format(getString(R.string.winner), higestBid.getBidder().getEmail(), higestBid.getQuote()));
                } else {
                    tvStatus.setText(R.string.auction_ended);
                }
            }
        } else {
            btnBidNow.setVisibility(View.GONE);
            tvStatus.setVisibility(View.VISIBLE);
            long millisInFuture = auctionItem.getEndTime().getTime() - Calendar.getInstance().getTimeInMillis();
            if (millisInFuture > 0) {
                tvStatus.setText(String.format(getString(R.string.your_bid_amount), userBid.getQuote()));
            } else {
                if (!fromWon) {
                    if (higestBid != null) {
                        tvStatus.setText(String.format(getString(R.string.winner), higestBid.getBidder().getEmail(), higestBid.getQuote()));
                    } else {
                        tvStatus.setText(R.string.auction_ended);
                    }
                } else {
                    timerText.setText(String.format(getString(R.string.your_bid_amount), userBid.getQuote()));
                    tvStatus.setText(R.string.congratulations);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DBHelper.releaseHelper();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bidCloseTimer != null) {
            bidCloseTimer.cancel();
        }
    }

    @Override
    public void onClick(View view) {
        BidActionFragment fragment = new BidActionFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Constants.AUCTION_NAME, auctionItem.getName());
        arguments.putDouble(Constants.AUCTION_BASE_PRICE, auctionItem.getStartPrice());
        if (higestBid != null) {
            arguments.putDouble(Constants.AUCTION_HIGHEST_AMOUNT, higestBid.getQuote());
        } else {
            arguments.putDouble(Constants.AUCTION_HIGHEST_AMOUNT, 0.0);
        }
        fragment.setArguments(arguments);
        fragment.show(getSupportFragmentManager(), "SUBMIT_BID_TAG");
    }

    @Override
    public void bidMade(double amount, String notes) {
        // create new bid from current user
        btnBidNow.setEnabled(false);
        UserBid newBid = new UserBid();
        newBid.setItem(auctionItem);
        newBid.setBidder(getUser());
        newBid.setQuote(amount);
        newBid.setDescription(notes);
        if (dbHelper.getUserBidRuntimeDao().create(newBid) == 1) {
            userBid = higestBid = newBid;
            bidCloseTimer.cancel();
            timerText.setText(String.format(getString(R.string.your_bid_amount), userBid.getQuote()));
            btnBidNow.setVisibility(View.GONE);
            Toast.makeText(AuctionItemActivity.this, R.string.bid_submitted, Toast.LENGTH_SHORT).show();
        } else {
            btnBidNow.setEnabled(true);
            Toast.makeText(AuctionItemActivity.this, R.string.bid_submit_error, Toast.LENGTH_SHORT).show();
        }
    }

    private class BidCloseTimer extends CountDownTimer {

        private String endingFormat;

        public BidCloseTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            endingFormat = getString(R.string.auction_ends_in);
        }

        @Override
        public void onFinish() {
            btnBidNow.setVisibility(View.GONE);
            timerText.setText(R.string.auction_ended);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long h = millisUntilFinished / (60 * 60 * 1000);
            millisUntilFinished = millisUntilFinished % (60 * 60 * 1000);
            long m = millisUntilFinished / (60 * 1000);
            millisUntilFinished = millisUntilFinished % (60 * 1000);
            long s = millisUntilFinished / 1000;
            String time = String.format("%02d:%02d:%02d", h, m, s);
            timerText.setText(String.format(endingFormat, time));
        }
    }

}
