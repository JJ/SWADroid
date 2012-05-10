package es.ugr.swad.swadroid.modules.downloads;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import es.ugr.swad.swadroid.R;
/**
 * Adapter to populate browser of files with the information received from SWAD
 * @author Helena Rodríguez Gijon <hrgijon@gmail.com>
 * */

public class NodeAdapter extends BaseAdapter {
	private ArrayList<DirectoryItem> list;
	private Activity mContext;
	public NodeAdapter(Activity c, ArrayList<DirectoryItem> list){
		mContext = c;
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if(convertView == null){
			DirectoryItem node = list.get(position);
			LayoutInflater inflator = mContext.getLayoutInflater();
			view = inflator.inflate(R.layout.grid_item, null);
			TextView text = (TextView) view.findViewById(R.id.icon_text);
			text.setText(node.getName());
			
			ImageView icon = (ImageView) view.findViewById(R.id.icon_image);
			if(node.isFolder()){
				icon.setImageResource(R.drawable.folder);
			}else{
				icon.setImageResource(R.drawable.file);
			}
			
		}else
		{
			view = convertView;
		}
		return view;
	}

}
