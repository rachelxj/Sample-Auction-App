package com.auctionapp.model;

import java.util.Calendar;
import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user_bid")
public class UserBid {

	@DatabaseField(generatedId = true)
	private long id;
	@DatabaseField
	private String description;
	@DatabaseField
	private double quote;
	@DatabaseField(dataType = DataType.DATE)
	private Date bidDate;
	@DatabaseField(foreign = true, columnName = "item_id")
	private AuctionItem item;
	@DatabaseField(foreign = true, columnName = "bidder_id", foreignAutoRefresh=true)
	private User bidder;
	@DatabaseField(defaultValue = "0")
	private int status;

	public UserBid() {
		// Default constructor
		quote = 0.0;
		bidDate = Calendar.getInstance().getTime();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getQuote() {
		return quote;
	}

	public void setQuote(double quote) {
		this.quote = quote;
	}

	public AuctionItem getItem() {
		return item;
	}

	public void setItem(AuctionItem item) {
		this.item = item;
	}

	public Date getBidDate() {
		return bidDate;
	}

	public void setBidDate(Date bidDate) {
		this.bidDate = bidDate;
	}

	public User getBidder() {
		return bidder;
	}

	public void setBidder(User bidder) {
		this.bidder = bidder;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}