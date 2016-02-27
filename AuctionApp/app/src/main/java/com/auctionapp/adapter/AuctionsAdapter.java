package com.auctionapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.auctionapp.AuctionUtil;
import com.auctionapp.R;
import com.auctionapp.model.AuctionItem;
import com.auctionapp.model.AuctionPhoto;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by apple on 2/25/16.
 */
public class AuctionsAdapter extends BaseAdapter {

    private List<AuctionItem> auctions;
    private LayoutInflater inflater;
    private String auctionEndsFormat;
    private DisplayImageOptions displayImageOptions;
    private ImageLoader imageLoader;
    private boolean isFuture;

    public AuctionsAdapter(Context context, List<AuctionItem> items, boolean isFuture) {
        this.auctions = items;
        this.isFuture = isFuture;
        this.displayImageOptions = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.default_bid_image)
                .showImageOnFail(R.drawable.default_bid_image).showImageOnLoading(R.drawable.default_bid_image)
                .displayer(new FadeInBitmapDisplayer(500)).cacheOnDisk(true).handler(new Handler()).build();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        auctionEndsFormat = isFuture ? context.getString(R.string.bidding_starts_at) : context.getString(R.string.bidding_ends_on);
        imageLoader = ImageLoader.getInstance();
    }

    public AuctionsAdapter(Context context, List<AuctionItem> items) {
        this(context, items, false);
    }

    @Override
    public int getCount() {
        return auctions.size();
    }

    @Override
    public Object getItem(int position) {
        return auctions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return auctions.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AuctionItem item = (AuctionItem) getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.auction_list_item, null);
            holder = new ViewHolder();
            holder.itemImage = (ImageView) convertView.findViewById(R.id.ivItem);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDescription);
            holder.tvEndsOn = (TextView) convertView.findViewById(R.id.tvEndTime);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Collection<AuctionPhoto> auctionPhotos = item.getAuctionPhotos();
        if (auctionPhotos != null) {
            Iterator<AuctionPhoto> iterator = auctionPhotos.iterator();
            while (iterator.hasNext()) {
                AuctionPhoto photo = iterator.next();
                imageLoader.displayImage(Uri.fromFile(new File(photo.getPath())).toString(), holder.itemImage, displayImageOptions);
                break;
            }
        }
        holder.tvTitle.setText(item.getName());
        holder.tvDesc.setText(item.getDescription());
        if (isFuture) {
            holder.tvEndsOn.setText(String.format(auctionEndsFormat, AuctionUtil.formatDate(item.getStartTime())));
        } else {
            holder.tvEndsOn.setText(String.format(auctionEndsFormat, AuctionUtil.formatDate(item.getEndTime())));
        }
        convertView.setTag(holder);
        return convertView;
    }

    class ViewHolder {
        ImageView itemImage;
        TextView tvTitle, tvDesc, tvEndsOn;
    }
}