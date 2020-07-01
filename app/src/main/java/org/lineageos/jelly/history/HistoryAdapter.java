/*
 * Copyright (C) 2020 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lineageos.jelly.history;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.lineageos.jelly.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class HistoryAdapter extends RecyclerView.Adapter<HistoryHolder> {
    private final Context mContext;
    private final DateFormat mHistoryDateFormat;
    private Cursor mCursor;

    private int mIdColumnIndex;
    private int mTitleColumnIndex;
    private int mUrlColumnIndex;
    private int mTimestampColumnIndex;

    HistoryAdapter(Context context) {
        mContext = context;
        mHistoryDateFormat = new SimpleDateFormat(context.getString(R.string.history_date_format),
                Locale.getDefault());
        setHasStableIds(true);
    }

    void swapCursor(Cursor cursor) {
        if (cursor == mCursor) {
            return;
        }
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        if (mCursor != null) {
            mIdColumnIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
            mTitleColumnIndex = cursor.getColumnIndexOrThrow(HistoryProvider.Columns.TITLE);
            mUrlColumnIndex = cursor.getColumnIndexOrThrow(HistoryProvider.Columns.URL);
            mTimestampColumnIndex = cursor.getColumnIndexOrThrow(HistoryProvider.Columns.TIMESTAMP);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryHolder onCreateViewHolder(ViewGroup parent, int type) {
        return new HistoryHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHolder holder, int position) {
        Cursor cursor = this.mCursor;
        if (cursor == null) return;
        if (!cursor.moveToPosition(position)) {
            return;
        }
        long timestamp = cursor.getLong(mTimestampColumnIndex);
        String summary = mHistoryDateFormat.format(new Date(timestamp));
        String title = cursor.getString(mTitleColumnIndex);
        String url = cursor.getString(mUrlColumnIndex);
        holder.bind(mContext, title, url, summary, timestamp);
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    @Override
    public long getItemId(int position) {
        Cursor cursor = this.mCursor;
        if (cursor == null) return -1;
        return cursor.moveToPosition(position) ? cursor.getLong(mIdColumnIndex) : -1;
    }
}
