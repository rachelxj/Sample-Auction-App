package com.auctionapp.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.auctionapp.AuctionUtil;
import com.auctionapp.Constants;
import com.auctionapp.model.AuctionItem;
import com.auctionapp.model.AuctionPhoto;
import com.auctionapp.model.User;
import com.auctionapp.model.UserBid;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DBHelper extends OrmLiteSqliteOpenHelper {

    protected static final String TAG = DBHelper.class.getName();
    public static final int DBVERSION = 1;
    public static final String DBNAME = "auctionapp.sqlite";

    private RuntimeExceptionDao<User, Long> userRuntimeDao = null;
    private RuntimeExceptionDao<UserBid, Long> bidRuntimeDao = null;
    private RuntimeExceptionDao<AuctionItem, Long> auctionItemRuntimeDao = null;
    private RuntimeExceptionDao<AuctionPhoto, Long> photosRuntimeDao = null;

    public DBHelper(final Context context) {
        super(context, DBHelper.DBNAME, null, DBHelper.DBVERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db, final ConnectionSource connectionSource) {
        createTables();
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final ConnectionSource connectionSource, int oldVersion, final int newVersion) {
        if (oldVersion != newVersion) {
            dropAllTables();
        }
    }

    public void createTables() {
        try {
            // create the tables
            TableUtils.createTableIfNotExists(connectionSource, User.class);
            TableUtils.createTableIfNotExists(connectionSource, UserBid.class);
            TableUtils.createTableIfNotExists(connectionSource, AuctionItem.class);
            TableUtils.createTableIfNotExists(connectionSource, AuctionPhoto.class);
            // create system bot
            User user = new User();
            user.setEmail(Constants.BOT_EMAIL);
            user.setPassword(AuctionUtil.md5(Constants.BOT_PASSWORD));
            getUserRuntimeDao().create(user);
        } catch (SQLException ex) {
            Log.e(TAG, " --> createTables" + ex);
        }
    }

    public void dropAllTables() {
        try {
            TableUtils.dropTable(connectionSource, UserBid.class, true);
            TableUtils.dropTable(connectionSource, AuctionPhoto.class, true);
            TableUtils.dropTable(connectionSource, AuctionItem.class, true);
            TableUtils.dropTable(connectionSource, User.class, true);
        } catch (SQLException ex) {
            Log.e(TAG, " --> dropAllTables" + ex);
        }
    }

    public RuntimeExceptionDao<User, Long> getUserRuntimeDao() {
        if (userRuntimeDao == null) {
            userRuntimeDao = getRuntimeExceptionDao(User.class);
        }
        return userRuntimeDao;
    }

    public RuntimeExceptionDao<AuctionItem, Long> getItemRuntimeDao() {
        if (auctionItemRuntimeDao == null) {
            auctionItemRuntimeDao = getRuntimeExceptionDao(AuctionItem.class);
        }
        return auctionItemRuntimeDao;
    }

    public RuntimeExceptionDao<AuctionPhoto, Long> getPhotoRuntimeDao() {
        if (photosRuntimeDao == null) {
            photosRuntimeDao = getRuntimeExceptionDao(AuctionPhoto.class);
        }
        return photosRuntimeDao;
    }

    public RuntimeExceptionDao<UserBid, Long> getUserBidRuntimeDao() {
        if (bidRuntimeDao == null) {
            bidRuntimeDao = getRuntimeExceptionDao(UserBid.class);
        }
        return bidRuntimeDao;
    }

    public User getUserByEmail(String email) throws SQLException {
        QueryBuilder<User, Long> queryBuilder = getUserRuntimeDao().queryBuilder();
        queryBuilder.where().eq("email", email);
        List<User> list = getUserRuntimeDao().query(queryBuilder.prepare());
        return list.size() > 0 ? list.get(0) : null;
    }

    public User loginUser(String email, String password) throws SQLException {
        QueryBuilder<User, Long> queryBuilder = getUserRuntimeDao().queryBuilder();
        queryBuilder.where().eq("email", email).and().eq("password", password);
        List<User> users = getUserRuntimeDao().query(queryBuilder.prepare());
        return users.size() == 1 ? users.get(0) : null;
    }

    public List<AuctionItem> getOpenAuctions(User user) throws SQLException {
        Calendar calendar = Calendar.getInstance();
        QueryBuilder<AuctionItem, Long> queryBuilder = getItemRuntimeDao().queryBuilder();
        queryBuilder.where().le("startTime", calendar.getTime()).and().ge("endTime", calendar.getTime()).and().ne("owner_id", user.getId());
        queryBuilder.orderBy("endTime", false);
        return getItemRuntimeDao().query(queryBuilder.prepare());
    }

    public List<AuctionItem> getComingAuctions(User user) throws SQLException {
        Calendar calendar = Calendar.getInstance();
        QueryBuilder<AuctionItem, Long> queryBuilder = getItemRuntimeDao().queryBuilder();
        queryBuilder.where().gt("startTime", calendar.getTime()).and().ne("owner_id", user.getId());
        queryBuilder.orderBy("startTime", true);
        return getItemRuntimeDao().query(queryBuilder.prepare());
    }

    public List<AuctionItem> getMyAuctions(User user) throws SQLException {
        QueryBuilder<AuctionItem, Long> queryBuilder = getItemRuntimeDao().queryBuilder();
        queryBuilder.where().eq("owner_id", user);
        return getItemRuntimeDao().query(queryBuilder.prepare());
    }

    public UserBid getHighestBid(long auctionId) throws SQLException {
        QueryBuilder<UserBid, Long> queryBuilder = getUserBidRuntimeDao().queryBuilder();
        queryBuilder.where().eq("item_id", auctionId);
        queryBuilder.orderBy("quote", false);
        List<UserBid> bids = getUserBidRuntimeDao().query(queryBuilder.prepare());
        if (bids != null && bids.size() > 0) {
            return bids.get(0);
        } else {
            return null;
        }
    }

    public UserBid getUserBid(long itemId, long userId) throws SQLException {
        QueryBuilder<UserBid, Long> queryBuilder = getUserBidRuntimeDao().queryBuilder();
        queryBuilder.where().eq("bidder_id", userId).and().eq("item_id", itemId);
        List<UserBid> bids = getUserBidRuntimeDao().query(queryBuilder.prepare());
        if (bids != null && bids.size() > 0) {
            return bids.get(0);
        } else {
            return null;
        }
    }

    public List<AuctionItem> getWonAuctions(User user) throws SQLException {
        List<AuctionItem> auctionsWon = new ArrayList<AuctionItem>();
        Calendar calendar = Calendar.getInstance();
        QueryBuilder<AuctionItem, Long> queryBuilder = getItemRuntimeDao().queryBuilder();
        queryBuilder.where().le("endTime", calendar.getTime()).and().ne("owner_id", user.getId());
        List<AuctionItem> expiredAuctions = getItemRuntimeDao().query(queryBuilder.prepare());
        for (AuctionItem auction : expiredAuctions) {
            UserBid maxBid = new UserBid();
            for (UserBid bid : auction.getUserBids()) {
                if (bid.getQuote() > maxBid.getQuote()) {
                    maxBid = bid;
                }
            }
            if (maxBid.getBidder() != null && maxBid.getBidder().getId() == user.getId()) {
                auctionsWon.add(auction);
            }
        }
        return auctionsWon;
    }

    public List<UserBid> getAuctionBids(long auctionId) throws SQLException {
        QueryBuilder<UserBid, Long> queryBuilder = getUserBidRuntimeDao().queryBuilder();
        queryBuilder.where().eq("item_id", auctionId);
        return getUserBidRuntimeDao().query(queryBuilder.prepare());
    }

    public User getSystemUser() throws SQLException {
        return getUserByEmail(Constants.BOT_EMAIL);
    }

    public static DBHelper getDBHelper(final Context context) {
        return OpenHelperManager.getHelper(context, DBHelper.class);
    }

    public static void releaseHelper() {
        OpenHelperManager.releaseHelper();
    }
}