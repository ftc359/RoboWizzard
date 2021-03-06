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
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;

import java.util.HashMap;
import java.util.Iterator;

public class ControllerAdapter extends BaseAdapter{
    private Activity activity;
    private LayoutFile layout;
    private HashMap<ControllerConfiguration, Integer> mIdMap;
    private int id;
    private long activeId;
    private int llWidth;
    private ActiveChangeManager mActiveChangeManager;

    public ControllerAdapter(Activity activity, LayoutFile layout) {
        mActiveChangeManager = new ActiveChangeManager();
        activeId = -1;
        this.activity = activity;
        this.layout = layout;
        Iterator iterator = layout.iterator();
        id = 0;
        mIdMap = new HashMap<>();
        while(iterator.hasNext()) {
            mIdMap.put((ControllerConfiguration) iterator.next(), id++);
        }
    }

    public void add(ControllerConfiguration controller) {
        layout.add(controller);
        mIdMap.put(controller, id++);
        notifyDataSetChanged();
    }

    public void remove(ControllerConfiguration controller) {
        layout.remove(controller);
        mIdMap.remove(controller);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return layout.size();
    }

    @Override
    public Object getItem(int position) {
        return layout.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mIdMap.get((ControllerConfiguration) getItem(position));
    }

    private static class ViewHolder {
        private RelativeLayout parent;
        private ObservableHorizontalScrollView scrollView;
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
            viewHolder.parent = (RelativeLayout) convertView.findViewById(R.id.parent);
            viewHolder.controller = (Button) convertView.findViewById(R.id.itemButton);
            viewHolder.scrollView = (ObservableHorizontalScrollView) convertView.findViewById(R.id.scrollView);
            viewHolder.controller.setWidth(BaseActivity.screenSize.x);
            viewHolder.serialNumber = (TextView) convertView.findViewById(R.id.serialNumber);
            viewHolder.delete = (Button) convertView.findViewById(R.id.deleteButtonDummy);
            Button deleteReal = (Button) convertView.findViewById(R.id.deleteButton);
            deleteReal.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            viewHolder.delete.setLayoutParams(new LinearLayout.LayoutParams(deleteReal.getMeasuredWidth(), LinearLayout.LayoutParams.MATCH_PARENT));
            mActiveChangeManager.register(getItemId(position), new ActiveChangeListener() {
                @Override
                public void onActiveChange() {
                    viewHolder.scrollView.smoothScrollTo(0, 0);
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

        viewHolder.controller.setText(((ControllerConfiguration) getItem(position)).getName());
        viewHolder.controller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activeId != -1) {
                    mActiveChangeManager.run(activeId);
                    activeId = -1;
                    return;
                }
                View focusedView = activity.getCurrentFocus();
                if(focusedView != null) {
                    focusedView.clearFocus();
                    ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                }
                Intent intent = new Intent(activity, DeviceConfigurationActivity.class);
                intent.putExtra("CONTROLLER", position);
                activity.startActivityForResult(intent, BaseActivity.DEVICE_CONFIG);
                activity.overridePendingTransition(R.anim.slide_in_horizontal, R.anim.fade_out);
            }
        });
        viewHolder.serialNumber.setText(((ControllerConfiguration) getItem(position)).getSerialNumber().toString());
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
                        ControllerAdapter.this.remove((ControllerConfiguration) getItem(position));
                        ControllerAdapter.this.notifyDataSetChanged();
                    }
                });
            }
        });
        viewHolder.scrollView.setOnScrollListener(new ObservableHorizontalScrollView.OnScrollListener() {
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
                int optionsWidth = llWidth;
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
