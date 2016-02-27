package com.auctionapp.model;

import java.util.Calendar;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User implements Parcelable {

	@DatabaseField(generatedId = true)
	private long id;
	@DatabaseField(unique = true)
	private String email;
	@DatabaseField
	private String password;
	@DatabaseField(dataType = DataType.DATE)
	private Date createDate;

	public User() {
		// Default constructor
		createDate = Calendar.getInstance().getTime();
	}

	private User(Parcel in) {
		id = in.readLong();
		email = in.readString();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeStringArray(new String[] { String.valueOf(id), email });
	}

	public static final Creator<User> CREATOR = new Creator<User>() {
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			return new User[size];
		}
	};
}