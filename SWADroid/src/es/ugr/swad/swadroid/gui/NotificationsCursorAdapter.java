/*
 *  This file is part of SWADroid.
 *
 *  Copyright (C) 2010 Juan Miguel Boyero Corral <juanmi1982@gmail.com>
 *
 *  SWADroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SWADroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with SWADroid.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.ugr.swad.swadroid.gui;

import java.util.Date;
import es.ugr.swad.swadroid.R;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Custom adapter for display notifications
 * @author Juan Miguel Boyero Corral <juanmi1982@gmail.com>
 *
 */
public class NotificationsCursorAdapter extends CursorAdapter {
	private static final String EXAM_ANNOUNCEMENT_COLOR = "#380B61";
	private static final String MARKS_FILE_COLOR = "#4B088A";
	private static final String NOTICE_COLOR = "#08088A";
	private static final String MESSAGE_COLOR = "#0B615E";
	private static final String FORUM_REPLY_COLOR = "#0B0B3B";
	
	public NotificationsCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	public NotificationsCursorAdapter(Context context, Cursor c,
			boolean autoRequery) {
		
		super(context, c, autoRequery);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {		
		long unixTime;
		String type, sender, from, dateTitle, summaryTitle, summaryText;
    	Date d;
    	
        TextView eventType = (TextView) view.findViewById(R.id.eventType);
        TextView eventTime = (TextView) view.findViewById(R.id.eventTime);
        TextView eventSender = (TextView) view.findViewById(R.id.eventSender);
        TextView location = (TextView) view.findViewById(R.id.eventLocation);
        TextView summary = (TextView) view.findViewById(R.id.eventSummary);
        
        if(eventType != null) {
        	type = cursor.getString(cursor.getColumnIndex("eventType"));
        	
        	if(type.equals("examAnnouncement"))
        	{
        		type = context.getString(R.string.examAnnouncement);
        		view.setBackgroundColor(Color.parseColor(EXAM_ANNOUNCEMENT_COLOR));
        	} else if(type.equals("marksFile"))
        	{
        		type = context.getString(R.string.marksFile);
        		view.setBackgroundColor(Color.parseColor(MARKS_FILE_COLOR));
        	} else if(type.equals("notice"))
        	{
        		type = context.getString(R.string.notice);
        		view.setBackgroundColor(Color.parseColor(NOTICE_COLOR));
        	} else if(type.equals("message"))
        	{
        		type = context.getString(R.string.message);
        		view.setBackgroundColor(Color.parseColor(MESSAGE_COLOR));
        	} else if(type.equals("forumReply"))
        	{
        		type = context.getString(R.string.forumReply);
        		view.setBackgroundColor(Color.parseColor(FORUM_REPLY_COLOR));
        	}
        	
        	eventType.setText(type);
        }
        if(eventTime != null){
        	unixTime = Long.parseLong(cursor.getString(cursor.getColumnIndex("eventTime")));
        	d = new Date(unixTime * 1000);
        	dateTitle = context.getString(R.string.notificationsDateMsg);
        	eventTime.setText(dateTitle + ": " + d.toLocaleString());
        }
        if(eventSender != null){
        	sender = cursor.getString(cursor.getColumnIndex("userFirstname")) + " "
        			+ cursor.getString(cursor.getColumnIndex("userSurname1")) + " "
        			+ cursor.getString(cursor.getColumnIndex("userSurname2"));
        	from = context.getString(R.string.notificationsFromMsg);
        	eventSender.setText(from + ": " + sender);
        }
        if(location != null) {
        	location.setText(cursor.getString(cursor.getColumnIndex("location")));
        }
        if(summary != null){
        	summaryTitle = context.getString(R.string.content);
        	summaryText = cursor.getString(cursor.getColumnIndex("summary"));
        	summary.setText(summaryTitle + ":\n" + Html.fromHtml(summaryText));
        }
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {	
		LayoutInflater vi = LayoutInflater.from(context);
		View v = vi.inflate(R.layout.notifications_list_item, null);
		
		return v;
	}

}
