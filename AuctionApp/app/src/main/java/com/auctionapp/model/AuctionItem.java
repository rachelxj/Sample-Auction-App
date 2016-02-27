package com.auctionapp.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "auction_items")
public class AuctionItem implements Serializable {

	@DatabaseField(generatedId = true)
	private long id;
	@DatabaseField
	private String name;
	@DatabaseField
	private String description;
	@DatabaseField
	private double startPrice;
	@DatabaseField(dataType = DataType.DATE)
	private Date startTime;
	@DatabaseField(dataType = DataType.DATE)
	private Date endTime;
	@DatabaseField(foreign = true, columnName = "owner_id")
	private User owner;
	@DatabaseField(dataType = DataType.DATE)
	private Date createDate;
	@ForeignCollectionField
	private Collection<AuctionPhoto> auctionPhotos;
	@ForeignCollectionField
	private Collection<UserBid> userBids;

	public AuctionItem() {
		// Default constructor
		createDate = Calendar.getInstance().getTime();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getStartPrice() {
		return startPrice;
	}

	public void setStartPrice(double startPrice) {
		this.startPrice = startPrice;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Collection<AuctionPhoto> getAuctionPhotos() {
		return auctionPhotos;
	}

	public void setAuctionPhotos(Collection<AuctionPhoto> auctionPhotos) {
		this.auctionPhotos = auctionPhotos;
	}

	public Collection<UserBid> getUserBids() {
		return userBids;
	}

	public void setUserBids(Collection<UserBid> userBids) {
		this.userBids = userBids;
	}
}
