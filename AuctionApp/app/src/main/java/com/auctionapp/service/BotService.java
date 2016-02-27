package com.auctionapp.service;

import android.app.IntentService;
import android.content.Intent;

import com.auctionapp.Constants;
import com.auctionapp.R;
import com.auctionapp.dao.DBHelper;
import com.auctionapp.model.AuctionItem;
import com.auctionapp.model.User;
import com.auctionapp.model.UserBid;

import java.sql.SQLException;
import java.util.Random;

public class BotService extends IntentService {

	private static final String TAG = BotService.class.getSimpleName();

	public BotService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		long itemId = intent.getLongExtra(Constants.AUCTION_ITEM_ID, -1);
		double baseAmount = intent.getDoubleExtra(Constants.AUCTION_BASE_PRICE, 0.0);
		// auto-bidder service
		try {
			DBHelper dbHelper = DBHelper.getDBHelper(this);
			User user = dbHelper.getSystemUser();
			if (user != null) {
				AuctionItem bidFor = new AuctionItem();
				bidFor.setId(itemId);
				UserBid newBid = new UserBid();
				newBid.setItem(bidFor);
				newBid.setBidder(user);
				newBid.setQuote(generateBidAmount(baseAmount));
				newBid.setDescription(getString(R.string.be_a_winner));
				dbHelper.getUserBidRuntimeDao().create(newBid);
			}
		}
		catch (SQLException ex) {
		} finally {
			DBHelper.releaseHelper();
		}
	}

	private double generateBidAmount(double baseAmount) {
		double bidAmount = 0.0;
		Random random = new Random();
		bidAmount = random.nextInt(100) + baseAmount;
		return bidAmount;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
