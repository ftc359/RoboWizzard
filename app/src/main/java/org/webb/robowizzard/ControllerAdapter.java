/*
 * Copyright (c) 2014, 2015 Qualcomm Technologies Inc
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of Qualcomm Technologies Inc nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.webb.robowizzard;


import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.util.SerialNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ControllerAdapter extends BaseAdapter{
    private Activity activity;
    private List<Map.Entry> mList;
    private HashMap<Map.Entry, Integer> mIdMap;
    private int id;
    private long activeId;
    private int llWidth;
    private ActiveChangeManager mActiveChangeManager;

    public ControllerAdapter(Activity activity, Map<SerialNumber, ControllerConfiguration> layout) {
        mActiveChangeManager = new ActiveChangeManager();
        activeId = -1;
        this.activity = activity;
        mList = new ArrayList<>();
        Iterator iterator = layout.entrySet().iterator();
        id = 0;
        mIdMap = new HashMap<>();
        while(iterator.hasNext()) {
            add((Map.Entry) iterator.next());
        }
    }

    public void add(Map.Entry entry) {
        mList.add(entry);
        mIdMap.put(entry, id++);
        notifyDataSetChanged();
    }

    public void remove(Map.Entry entry) {
        mList.remove(entry);
        mIdMap.remove(entry);
    }

    public HashMap<SerialNumber, ControllerConfiguration> parseLayout() {
        HashMap<SerialNumber, ControllerConfiguration> tempMap = new HashMap<>();
        Iterator iterator = mList.iterator();
        while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            tempMap.put((SerialNumber) entry.getKey(), (ControllerConfiguration) entry.getValue());
        }
        return tempMap;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mIdMap.get((Map.Entry) getItem(position));
    }

    private static class ViewHolder {
        private ObservableHorizontalScrollView parent;
        private Button controller;
        private TextView serialNumber;
        private Button delete;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null) {
            viewHolder = new ViewHolder();
            convertView = activity.getLayoutInflater().inflate(R.layout.item_controller, parent, false);
            viewHolder.controller = (Button) convertView.findViewById(R.id.itemButton);
            viewHolder.parent = (ObservableHorizontalScrollView) convertView.findViewById(R.id.parent);
            viewHolder.controller.setWidth(BaseActivity.screenSize.x);
            viewHolder.serialNumber = (TextView) convertView.findViewById(R.id.serialNumber);
            viewHolder.delete = (Button) convertView.findViewById(R.id.deleteButton);
            mActiveChangeManager.register(getItemId(position), new ActiveChangeListener() {
                @Override
                public void onActiveChange() {
                    viewHolder.parent.smoothScrollTo(0, 0);
                }
            });
            LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.linearLayout);
            ll.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llWidth = ll.getMeasuredWidth();
            convertView.setHasTransientState(true);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.controller.setText(((ControllerConfiguration) ((Map.Entry) getItem(position)).getValue()).getName());
        viewHolder.controller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activeId != -1) {
                    mActiveChangeManager.run(activeId);
                    activeId = -1;
                    return;
                }
            }
        });
        viewHolder.serialNumber.setText(((SerialNumber) ((Map.Entry) getItem(position)).getKey()).getSerialNumber());
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activeId == getItemId(position)) {
                activeId = -1;
            }
                viewHolder.parent.setTranslationX(0);
                viewHolder.parent.animate().setDuration(250).translationX(-BaseActivity.screenSize.x).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        ControllerAdapter.this.remove((Map.Entry) getItem(position));
                        ControllerAdapter.super.notifyDataSetChanged();
                    }
                });
            }
        });
        viewHolder.parent.setOnScrollListener(new ObservableHorizontalScrollView.OnScrollListener() {
            @Override
            public void onStartScroll(ObservableHorizontalScrollView scrollView) {
                if(activeId != -1 && activeId != getItemId(position)) {
                    mActiveChangeManager.run(activeId);
                }
            }

            @Override
            public void onScrollChanged(ObservableHorizontalScrollView scrollView, int x, int y, int oldX, int oldY) {
                if(x == oldX) {
                    onEndScroll(scrollView);
                }
            }

            @Override
            public void onEndScroll(ObservableHorizontalScrollView scrollView) {
                int scrollX = scrollView.getScrollX();
                int optionsWidth = llWidth - BaseActivity.screenSize.x;
                if((scrollX > optionsWidth / 8.0 && activeId != getItemId(position)) || (scrollX > optionsWidth * 7 / 8.0 && activeId == getItemId(position))) {
                    scrollView.smoothScrollTo(optionsWidth, 0);
                    activeId = getItemId(position);
                }
                else {
                    scrollView.smoothScrollTo(0, 0);
                    activeId = -1;
                }
            }
        });

        return convertView;
    }

    private interface ActiveChangeListener {
        public void onActiveChange();
    }

    private class ActiveChangeManager {
        private HashMap<Long, ActiveChangeListener> mIdMap;

        public ActiveChangeManager() {
            mIdMap = new HashMap<>();
        }

        public void register(long itemId, ActiveChangeListener activeChangeListener) {
            mIdMap.put(itemId, activeChangeListener);
        }

        public void run(long itemId) {
            mIdMap.get(itemId).onActiveChange();
        }
    }
}
