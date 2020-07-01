/*
 * Copyright (C) 2017 The LineageOS Project
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
package org.lineageos.jelly.favorite;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.lineageos.jelly.R;

class FavoriteAdapter extends RecyclerView.Adapter<FavoriteHolder> {
    private final Context mContext;
    private Cursor mCursor;

    private int mIdColumnIndex;
    private int mTitleColumnIndex;
    private int mUrlColumnIndex;
    private int mColorColumnIndex;

    FavoriteAdapter(Context context) {
        mContext = context;
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
            mTitleColumnIndex = cursor.getColumnIndexOrThrow(FavoriteProvider.Columns.TITLE);
            mUrlColumnIndex = cursor.getColumnIndexOrThrow(FavoriteProvider.Columns.URL);
            mColorColumnIndex = cursor.getColumnIndexOrThrow(FavoriteProvider.Columns.COLOR);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteHolder onCreateViewHolder(ViewGroup parent, int type) {
        return new FavoriteHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteHolder holder, int position) {
        Cursor cursor = this.mCursor;
        if (cursor == null) return;

        long id = cursor.getLong(mIdColumnIndex);
        String title = cursor.getString(mTitleColumnIndex);
        String url = cursor.getString(mUrlColumnIndex);
        int color = cursor.getInt(mColorColumnIndex);
        holder.bind(mContext, id, title, url, color);
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
