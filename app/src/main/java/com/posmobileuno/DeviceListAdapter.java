package com.posmobileuno;

import java.util.List;

import com.posmobileuno.btdroid.R;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class DeviceListAdapter extends BaseAdapter{
	private LayoutInflater mInflater;	
	private List<BluetoothDevice> mData;
	private OnPairButtonClickListener mListener;
	
	public DeviceListAdapter(Context context) { 
        mInflater = LayoutInflater.from(context);        
    }
	
	public void setData(List<BluetoothDevice> data) {
		mData = data;
	}
	
	public void setListener(OnPairButtonClickListener listener) {
		mListener = listener;
	}
	
	public int getCount() {
		return (mData == null) ? 0 : mData.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {			
			convertView			=  mInflater.inflate(R.layout.list_item_device, null);
			
			holder 				= new ViewHolder();
			
			holder.nameTv		= (TextView) convertView.findViewById(R.id.tv_name);
			holder.addressTv 	= (TextView) convertView.findViewById(R.id.tv_address);
			holder.pairBtn		= (Button) convertView.findViewById(R.id.btn_pair);
			holder.btnAddress = (Button) convertView.findViewById(R.id.btnAddress);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final BluetoothDevice device	= mData.get(position);
		
		holder.nameTv.setText(device.getName());
		holder.addressTv.setText(device.getAddress());
		holder.pairBtn.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Unpair" : "Pair");
		holder.pairBtn.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onPairButtonClick(position);
				}
			}
		});
		holder.btnAddress.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (mListener != null) {
				
					Intent intent = new Intent(v.getContext(), Gerenciar.class);
					intent.putExtra("MAC", device.getAddress());
					v.getContext().startActivity(intent);
				}
			}
		});
		
        return convertView;
	}

	static class ViewHolder {
		TextView nameTv;
		TextView addressTv;
		TextView pairBtn;
		TextView btnAddress;
	}
	
	public interface OnPairButtonClickListener {
		public abstract void onPairButtonClick(int position);
	}
}