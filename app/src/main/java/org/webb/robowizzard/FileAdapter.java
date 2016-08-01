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
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.hardware.configuration.ReadXMLFileHandler;
import com.qualcomm.robotcore.hardware.configuration.Utility;
import com.qualcomm.robotcore.util.SerialNumber;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class FileAdapter extends BaseAdapter{
    private Activity activity;
    private List<String> mList;
    private HashMap<String, Integer> mIdMap;
    private int id;
    private ActiveChangeManager mActiveChangeManager;
    private long activeId;
    private int lastX;
    private int llWidth;

    public FileAdapter(Activity activity, List<String> fileNames) {
        mActiveChangeManager = new ActiveChangeManager();
        activeId = -1;
        this.activity = activity;
        mList = new ArrayList<>();
        Iterator iterator = fileNames.iterator();
        id = 0;
        mIdMap = new HashMap<>();
        while(iterator.hasNext()) {
            add((String) iterator.next());
        }
    }

    public void add(String fileName) {
        mList.add(fileName);
        mIdMap.put(fileName, id++);
    }

    public void remove(String fileName) {
        mList.remove(fileName);
        mIdMap.remove(fileName);
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
        return mIdMap.get((String) getItem(position));
    }

    private static class ViewHolder {
        private ObservableHorizontalScrollView parent;
        private Button file;
        private Button export;
        private Button delete;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null) {
            viewHolder = new ViewHolder();
            convertView = activity.getLayoutInflater().inflate(R.layout.item_file, parent, false);
            viewHolder.parent = (ObservableHorizontalScrollView) convertView.findViewById(R.id.parent);
            viewHolder.file = (Button) convertView.findViewById(R.id.itemButton);
            viewHolder.file.setWidth(BaseActivity.screenSize.x);
            viewHolder.export = (Button) convertView.findViewById(R.id.exportButton);
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

        viewHolder.file.setText((String) getItem(position));
        viewHolder.file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activeId != -1) {
                    mActiveChangeManager.run(activeId);
                    activeId = -1;
                    return;
                }
                String filename = mList.get(position);
                BaseActivity.saved = new LayoutFile(filename);
                BaseActivity.current = new LayoutFile(BaseActivity.saved);
                activity.startActivity(new Intent(activity, ControllerConfigurationActivity.class));
                activity.overridePendingTransition(R.anim.slide_in_horizontal, R.anim.fade_out);
            }
        });
        viewHolder.export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Export using Firebase
                Toast.makeText(activity, "Export", Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.parent.setTranslationX(0);
                viewHolder.parent.animate().setDuration(250).translationX(-BaseActivity.screenSize.x).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if(activeId == getItemId(position)) {
                            activeId = -1;
                        }
                        File file = BaseActivity.getFile((String) getItem(position));
                        if(file.exists() && file.delete()) {
                            Toast.makeText(activity, "File Deleted", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(activity, "File Delete Failed", Toast.LENGTH_SHORT).show();
                        }
                        FileAdapter.this.remove((String) getItem(position));
                        FileAdapter.super.notifyDataSetChanged();
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
