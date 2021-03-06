package com.auctionapp.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.auctionapp.Constants;
import com.auctionapp.R;

public class BidActionFragment extends DialogFragment implements OnClickListener {

	private BidActionListener bidActionListener;
	private TextView tvBaseAmount, tvHighestAmount;
	private double baseAmount, highestAmount;
	private EditText etYourAmount, etBidNotes;

	public static interface BidActionListener {
		void bidMade(double amount, String notes);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof BidActionListener) {
			bidActionListener = (BidActionListener) activity;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		bidActionListener = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.dialog_submit_bid, null);
		tvBaseAmount = (TextView) rootView.findViewById(R.id.tvBasePrice);
		tvHighestAmount = (TextView) rootView.findViewById(R.id.tvHighestBid);
		etYourAmount = (EditText) rootView.findViewById(R.id.etUserBid);
		etBidNotes = (EditText) rootView.findViewById(R.id.etBidNotes);
		rootView.findViewById(R.id.btnSubmit).setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle arguments = getArguments();
		String title = arguments.getString(Constants.AUCTION_NAME, "N/A");
		baseAmount = arguments.getDouble(Constants.AUCTION_BASE_PRICE, 0.0);
		highestAmount = arguments.getDouble(Constants.AUCTION_HIGHEST_AMOUNT, 0.0);
		tvBaseAmount.setText(String.format("%.2f", baseAmount));
		if (highestAmount > 0.0) {
			tvHighestAmount.setText(String.format("%.2f", highestAmount));
		} else {
			tvHighestAmount.setText(R.string.no_bids);
		}
		getDialog().setTitle(String.format(getString(R.string.bidding_for), title));
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnSubmit:
				if (bidActionListener != null) {
					String strAmount = etYourAmount.getText().toString().trim();
					if (strAmount.equals("")) {
						strAmount = "0.0";
					}
					double amount = Double.valueOf(strAmount);
					if (amount > baseAmount) {
						if (amount > highestAmount) {
							bidActionListener.bidMade(amount, etBidNotes.getText().toString().trim());
							getDialog().dismiss();
						} else {
							Toast.makeText(getActivity(), R.string.cant_bid_less, Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getActivity(), R.string.more_than_base_amount, Toast.LENGTH_SHORT).show();
					}
				}
				break;
		}
	}
}