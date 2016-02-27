package com.auctionapp;

import android.test.AndroidTestCase;

import com.auctionapp.dao.DBHelper;
import com.auctionapp.model.AuctionItem;
import com.auctionapp.model.User;
import com.auctionapp.model.UserBid;

import junit.framework.Assert;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * Created by apple on 2/26/16.
 */
public class DBHelperTest extends AndroidTestCase {
    private DBHelper dbHelper;
    private User testUser;
    private User testUser2;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dbHelper = DBHelper.getDBHelper(getContext());
        testUser = new User();
        testUser.setEmail(System.currentTimeMillis() + "@auctionapp.com");
        testUser.setPassword(AuctionUtil.md5("123456"));
        dbHelper.getUserRuntimeDao().create(testUser);

        testUser2 = new User();
        testUser2.setEmail((System.currentTimeMillis() + 5) + "@auctionapp.com");
        testUser2.setPassword(AuctionUtil.md5("123456"));
        dbHelper.getUserRuntimeDao().create(testUser2);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // clear up test environment
        List<AuctionItem> list = dbHelper.getMyAuctions(testUser);
        for (AuctionItem item : list) {
            List<UserBid> bidsList = dbHelper.getAuctionBids(item.getId());
            for (UserBid bid : bidsList) {
                dbHelper.getUserBidRuntimeDao().delete(bid);
            }
            dbHelper.getItemRuntimeDao().delete(item);
        }
        list = dbHelper.getMyAuctions(testUser2);
        for (AuctionItem item : list) {
            List<UserBid> bidsList = dbHelper.getAuctionBids(item.getId());
            for (UserBid bid : bidsList) {
                dbHelper.getUserBidRuntimeDao().delete(bid);
            }
            dbHelper.getItemRuntimeDao().delete(item);
        }
        dbHelper.getUserRuntimeDao().delete(testUser);
        dbHelper.getUserRuntimeDao().delete(testUser2);
        DBHelper.releaseHelper();
    }

    public void testSaveUser() throws Exception {
        User user = new User();
        user.setEmail(System.currentTimeMillis() + "test@auctionapp.com");
        user.setPassword(AuctionUtil.md5("123456"));
        int result = dbHelper.getUserRuntimeDao().create(user);
        Assert.assertEquals(1, result);
        result = dbHelper.getUserRuntimeDao().delete(user);
        Assert.assertEquals(1, result);
    }

    public void testLoginUser() throws Exception {
        User user = dbHelper.loginUser(testUser.getEmail(), testUser.getPassword());
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getId(), testUser.getId());
    }

    public void testGetUserByEmail() throws Exception {
        User user = dbHelper.getUserByEmail(testUser.getEmail());
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getId(), testUser.getId());
    }

    public void testSaveAuction() throws Exception {
        AuctionItem item = new AuctionItem();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -10);
        item.setStartTime(cal.getTime());
        cal.add(Calendar.HOUR, 4);
        item.setEndTime(cal.getTime());
        item.setName("Auction1");
        item.setDescription("Auction Description");
        item.setStartPrice(10);
        item.setOwner(testUser);
        int result = dbHelper.getItemRuntimeDao().create(item);
        Assert.assertEquals(1, result);
        // auction owner won't see this item in the open auctions
        List<AuctionItem> list = dbHelper.getOpenAuctions(testUser);
        boolean find = false;
        for (AuctionItem auctionItem : list) {
            if (auctionItem.getId() == item.getId()) {
                find = true;
            }
        }
        Assert.assertEquals(find, false);
        // other user can see this item in the open auctions
        list = dbHelper.getOpenAuctions(testUser2);
        find = false;
        for (AuctionItem auctionItem : list) {
            if (auctionItem.getId() == item.getId()) {
                find = true;
            }
        }
        Assert.assertEquals(find, true);

        AuctionItem futureItem = new AuctionItem();
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        futureItem.setStartTime(cal.getTime());
        cal.add(Calendar.HOUR, 12);
        futureItem.setEndTime(cal.getTime());
        futureItem.setName("Future Auction1");
        futureItem.setDescription("Future Auction Description");
        futureItem.setStartPrice(10);
        futureItem.setOwner(testUser);
        result = dbHelper.getItemRuntimeDao().create(futureItem);
        Assert.assertEquals(1, result);

        // auction owner won't see this item in the coming auctions
        List<AuctionItem> futureList = dbHelper.getComingAuctions(testUser);
        find = false;
        for (AuctionItem auctionItem : futureList) {
            if (auctionItem.getId() == futureItem.getId()) {
                find = true;
            }
        }
        Assert.assertEquals(find, false);

        // other user can see this item in the coming auctions
        futureList = dbHelper.getComingAuctions(testUser2);
        find = false;
        for (AuctionItem auctionItem : futureList) {
            if (auctionItem.getId() == futureItem.getId()) {
                find = true;
            }
        }
        Assert.assertEquals(find, true);
    }

    public void testSaveBid()  throws Exception {
        List<AuctionItem> list = dbHelper.getMyAuctions(testUser);
        if (list.size() == 0) {
            testSaveAuction();
            list = dbHelper.getMyAuctions(testUser);
            if (list.size() == 0) {
                Assert.fail("Can not create auction item");
            }
        }
        AuctionItem item = list.get(0);
        UserBid bid = new UserBid();
        bid.setItem(item);
        bid.setDescription("TestUser1 test bid");
        bid.setBidder(testUser);
        bid.setQuote(item.getStartPrice() + new Random().nextInt(100));
        int result = dbHelper.getUserBidRuntimeDao().create(bid);
        Assert.assertEquals(1, result);

        UserBid bid2 = new UserBid();
        bid2.setItem(item);
        bid2.setDescription("TestUser2 test bid");
        bid2.setBidder(testUser2);
        bid2.setQuote(item.getStartPrice() + new Random().nextInt(100));
        result = dbHelper.getUserBidRuntimeDao().create(bid2);
        Assert.assertEquals(1, result);

        UserBid highest = dbHelper.getHighestBid(item.getId());
        Assert.assertEquals(highest.getQuote(), (bid.getQuote() > bid2.getQuote()) ? bid.getQuote() : bid2.getQuote());

        UserBid userBid = dbHelper.getUserBid(item.getId(), testUser.getId());
        Assert.assertEquals(userBid.getId(), bid.getId());
    }

    public void testGetSystemUser() throws Exception {
        User user = dbHelper.getSystemUser();
        Assert.assertNotNull(user);
    }
}
