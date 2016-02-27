package com.auctionapp.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.auctionapp.Constants;
import com.auctionapp.R;
import com.auctionapp.dao.DBHelper;
import com.auctionapp.model.AuctionItem;
import com.auctionapp.model.AuctionPhoto;
import com.auctionapp.service.BotService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreateAuctionActivity extends AuctionAppActivity implements View.OnClickListener  {
    private AuctionItem auctionItem;
    private List<String> pics;
    private int mStartYear, mStartMonth, mStartDay, mStartHour, mStartMinute;
    private int mEndYear, mEndMonth, mEndDay, mEndHour, mEndMinute;

    private EditText etTitle, etDescription, etBasePrice, etStartDate, etStartTime, etEndDate, etEndTime;
    private ImageView imageView;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    protected String mCurrentPhotoPath;
    private DBHelper dbHelper;


    private static final int TAKE_PHOTO_REQUEST = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_auction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dbHelper = DBHelper.getDBHelper(this);
        auctionItem = new AuctionItem();
        pics = new ArrayList<String>();
        init();
    }

    private void init() {
        etTitle = (EditText) findViewById(R.id.etTitle);
        etDescription = (EditText) findViewById(R.id.etDescription);
        etBasePrice = (EditText) findViewById(R.id.etBasePrice);
        etStartDate = (EditText) findViewById(R.id.etBidStartDate);
        etStartDate.setOnClickListener(this);
        etStartTime = (EditText) findViewById(R.id.etBidStartTime);
        etStartTime.setOnClickListener(this);

        etEndDate = (EditText) findViewById(R.id.etBidEndDate);
        etEndDate.setOnClickListener(this);
        etEndTime = (EditText) findViewById(R.id.etBidEndTime);
        etEndTime.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.ivItem);
        findViewById(R.id.ibTakePhoto).setOnClickListener(this);
        findViewById(R.id.btnSubmitAuction).setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DBHelper.releaseHelper();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.etBidStartDate:
            case R.id.etBidEndDate:
                showDatePicker(v.getId() == R.id.etBidStartDate);
                break;
            case R.id.etBidStartTime:
            case R.id.etBidEndTime:
                showTimePicker(v.getId() == R.id.etBidStartTime);
                break;
            case R.id.ibTakePhoto:
                takePicture();
                break;
            case R.id.btnSubmitAuction:
                saveAuction();
                break;
        }
    }

    private void saveAuction() {
        auctionItem.setOwner(getUser());
        auctionItem.setName(etTitle.getText().toString().trim());
        auctionItem.setDescription(etDescription.getText().toString().trim());
        String basePrice = etBasePrice.getText().toString().trim();
        if (basePrice.length() == 0) {
            basePrice = "0.0";
            Toast.makeText(CreateAuctionActivity.this, R.string.enter_base_price, Toast.LENGTH_SHORT).show();
            return;
        }
        auctionItem.setStartPrice(Double.valueOf(basePrice));
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        if (mStartYear == 0 || mStartMonth == 0 || mStartDay == 0) {
            Toast.makeText(CreateAuctionActivity.this, R.string.select_auction_start_date, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mEndYear == 0 || mEndMonth == 0 || mEndDay == 0) {
            Toast.makeText(CreateAuctionActivity.this, R.string.select_auction_end_date, Toast.LENGTH_SHORT).show();
            return;
        }
        calendar.set(mStartYear, mStartMonth, mStartDay, mStartHour, mStartMinute);
        Date auctionStart = calendar.getTime();

        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(mEndYear, mEndMonth, mEndDay, mEndHour, mEndMinute);
        Date auctionEnd = calendar2.getTime();

        if (auctionEnd.before(auctionStart)) {
            Toast.makeText(CreateAuctionActivity.this, R.string.select_auction_end_time_wrong, Toast.LENGTH_SHORT).show();
            return;
        }
        if (auctionEnd.before(new Date())) {
            Toast.makeText(CreateAuctionActivity.this, R.string.select_auction_end_time_wrong2, Toast.LENGTH_SHORT).show();
            return;
        }
        auctionItem.setStartTime(auctionStart);
        auctionItem.setEndTime(auctionEnd);
        if (auctionItem.getName().length() == 0) {
            Toast.makeText(CreateAuctionActivity.this, R.string.enter_auction_name, Toast.LENGTH_SHORT).show();
        } else if (auctionItem.getDescription().length() == 0) {
            Toast.makeText(CreateAuctionActivity.this, R.string.enter_auction_description, Toast.LENGTH_SHORT).show();
        } else if (auctionItem.getStartPrice() <= 0.0) {
            Toast.makeText(CreateAuctionActivity.this, R.string.enter_base_price, Toast.LENGTH_SHORT).show();
        } else {
            int result = dbHelper.getItemRuntimeDao().create(auctionItem);
            if (result == 1) {
                for (String path : pics) {
                    AuctionPhoto auctionPhoto = new AuctionPhoto();
                    auctionPhoto.setPath(path);
                    auctionPhoto.setAuctionItem(auctionItem);
                    dbHelper.getPhotoRuntimeDao().create(auctionPhoto);
                }
                Intent i = new Intent(this, BotService.class);
                i.putExtra(Constants.AUCTION_ITEM_ID, auctionItem.getId());
                i.putExtra(Constants.AUCTION_BASE_PRICE, auctionItem.getStartPrice());
                startService(i);
                Toast.makeText(CreateAuctionActivity.this, R.string.auction_item_created, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void showTimePicker(final boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        int hour;
        int minute;
        if (isStart) {
            hour = mStartHour != 0 ? mStartHour : calendar.get(Calendar.HOUR_OF_DAY);
            minute = mStartMinute != 0 ? mStartMinute : calendar.get(Calendar.MINUTE);
        } else {
            hour = mEndHour != 0 ? mEndHour : calendar.get(Calendar.HOUR_OF_DAY);
            minute = mEndMinute != 0 ? mEndMinute : calendar.get(Calendar.MINUTE);
        }
        TimePickerDialog timeDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (isStart) {
                    mStartHour = hourOfDay;
                    mStartMinute = minute;
                    etStartTime.setText(String.format("%s:%s", mStartHour < 10 ? "0" + mStartHour : "" + mStartHour, mStartMinute < 10 ? "0" + mStartMinute : "" + mStartMinute));
                } else {
                    mEndHour = hourOfDay;
                    mEndMinute = minute;
                    etEndTime.setText(String.format("%s:%s", mEndHour < 10 ? "0" + mEndHour : "" + mEndHour, mEndMinute < 10 ? "0" + mEndMinute : "" + mEndMinute));
                }
            }
        }, hour, minute, false);
        timeDialog.show();
    }

    private void showDatePicker(final boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        int year;
        int month;
        int day;
        if (isStart) {
            year = mStartYear != 0 ? mStartYear : calendar.get(Calendar.YEAR);
            month = mStartMonth != 0 ? mStartMonth : calendar.get(Calendar.MONTH);
            day = mStartDay != 0 ? mStartDay : calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            year = mEndYear != 0 ? mEndYear : calendar.get(Calendar.YEAR);
            month = mEndMonth != 0 ? mEndMonth : calendar.get(Calendar.MONTH);
            day = mEndDay != 0 ? mEndDay : calendar.get(Calendar.DAY_OF_MONTH);
        }
        DatePickerDialog dateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (isStart) {
                    mStartYear = year;
                    mStartMonth = monthOfYear;
                    mStartDay = dayOfMonth;
                    etStartDate.setText(String.format("%s-%s-%s", mStartYear, (mStartMonth + 1) < 10 ? "0" + (mStartMonth + 1) : "" + (mStartMonth + 1), mStartDay < 10 ? "0" + mStartDay : "" + mStartDay));
                } else {
                    mEndYear = year;
                    mEndMonth = monthOfYear;
                    mEndDay = dayOfMonth;
                    etEndDate.setText(String.format("%s-%s-%s", mEndYear, (mEndMonth + 1) < 10 ? "0" + (mEndMonth + 1) : "" + mEndMonth, mEndDay < 10 ? "0" + mEndDay : "" + mEndDay));
                }
            }
        }, year, month, day);
        dateDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == TAKE_PHOTO_REQUEST) {
                // for now only one image
                pics.clear();
                pics.add(mCurrentPhotoPath);
                showImage(imageView, mCurrentPhotoPath);
            }
        }
    }

    private File getAlbumDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(Environment.getExternalStorageDirectory(), "AuctionApp");
            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Toast.makeText(CreateAuctionActivity.this, R.string.external_storage_not_available, Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }
            }
        } else {
            Toast.makeText(CreateAuctionActivity.this, R.string.external_storage_not_available, Toast.LENGTH_SHORT).show();
        }
        return storageDir;
    }

    private File createImageOutputFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        imageF.createNewFile();
        mCurrentPhotoPath = imageF.getAbsolutePath();
        return imageF;
    }

    protected void takePicture() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = null;
        try {
            file = createImageOutputFile();
            mCurrentPhotoPath = file.getAbsolutePath();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        }
        catch (IOException ex) {
            file = null;
            mCurrentPhotoPath = null;
        }
        startActivityForResult(intent, TAKE_PHOTO_REQUEST);
    }

    protected void showImage(ImageView imageView, String path) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        bitmap = BitmapFactory.decodeFile(path, options);
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);
    }

}
