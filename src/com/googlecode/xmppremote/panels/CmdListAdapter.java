package com.googlecode.xmppremote.panels;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.xmppremote.R;
import com.googlecode.xmppremote.cmd.Cmd;
import com.googlecode.xmppremote.tools.StringFmt;
  
public class CmdListAdapter extends ArrayAdapter<Cmd> {
    
    LayoutInflater mInflater;
    Cmd[] mCommands;
    
    public CmdListAdapter(Activity activity, int textViewResourceId, Cmd[] list) {
        super(activity, textViewResourceId, list);
        mInflater = activity.getLayoutInflater();
        mCommands = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        View row = convertView;
        if (row == null) {
            row = mInflater.inflate(R.layout.cmd_item, parent, false);
        }
        
        Cmd cmd = getItem(position);
        
        ImageView imageView = (ImageView) row.findViewById(R.id.State);
        imageView.setImageResource(cmd.isActive() ? R.drawable.buddy_available : R.drawable.buddy_offline);
        
        TextView textView = (TextView) row.findViewById(R.id.Name);
        textView.setText(cmd.getName());
        
        String alias = StringFmt.join(cmd.getAlias(), ", ");
        textView = (TextView) row.findViewById(R.id.Alias);
        textView.setText(alias + " "); // Adding space because italic text is truncated...
        
        textView = (TextView) row.findViewById(R.id.Help);
        textView.setText(cmd.getHelpSummary() + " "); // Adding space because italic text is truncated...
        
        return row;
    }
}