/*
 * Copyright 2012 Google Inc.
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

package app.holoirc.util;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;

import java.util.HashSet;

/**
 * Utilities for handling multiple selection in list views. Contains functionality similar to
 * {@link
 * AbsListView#CHOICE_MODE_MULTIPLE_MODAL} but that works with {@link AppCompatActivity} and
 * backward-compatible action bars.
 */
public class MultiSelectionUtils {

    public static Controller attachMultiSelectionController(final AbsListView listView,
            final AppCompatActivity activity,
            final MultiChoiceModeListener listener,
            boolean startModeOnClick) {
        return Controller.attach(listView, activity, listener, startModeOnClick);
    }

    /**
     * @see android.widget.AbsListView.MultiChoiceModeListener
     */
    public static interface MultiChoiceModeListener extends ActionMode.Callback {

        /**
         * @see android.widget.AbsListView.MultiChoiceModeListener#onItemCheckedStateChanged(
         *android.view.ActionMode, int, long, boolean)
         */
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                boolean checked);
    }

    public static class Controller implements
            ActionMode.Callback,
            AdapterView.OnItemClickListener,
            AdapterView.OnItemLongClickListener {

        private final Handler mHandler = new Handler();

        private final Runnable mSetChoiceModeNoneRunnable = new Runnable() {
            @Override
            public void run() {
                mListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
            }
        };

        private ActionMode mActionMode;

        private AbsListView mListView = null;

        private AppCompatActivity mActivity = null;

        private MultiChoiceModeListener mListener = null;

        private HashSet<Long> mTempIdsToCheckOnRestore;

        private HashSet<Pair<Integer, Long>> mItemsToCheck;

        private AdapterView.OnItemClickListener mOldItemClickListener;

        private Controller() {
        }

        public static Controller attach(AbsListView listView, AppCompatActivity activity,
                MultiChoiceModeListener listener,
                boolean startModeOnClick) {
            Controller controller = new Controller();
            controller.mListView = listView;
            controller.mActivity = activity;
            controller.mListener = listener;
            listView.setOnItemLongClickListener(controller);
            if (startModeOnClick) {
                listView.setOnItemClickListener(controller);
            }
            return controller;
        }

        public void tryRestoreInstanceState(Bundle savedInstanceState) {
            readInstanceState(savedInstanceState);
            tryRestoreInstanceState();
        }

        public void finish() {
            if (mActionMode != null) {
                mActionMode.finish();
            }
        }

        public void tryRestoreInstanceState() {
            if (mTempIdsToCheckOnRestore == null || mListView.getAdapter() == null) {
                return;
            }

            boolean idsFound = false;
            Adapter adapter = mListView.getAdapter();
            for (int pos = adapter.getCount() - 1; pos >= 0; pos--) {
                if (mTempIdsToCheckOnRestore.contains(adapter.getItemId(pos))) {
                    idsFound = true;
                    if (mItemsToCheck == null) {
                        mItemsToCheck = new HashSet<>();
                    }
                    mItemsToCheck.add(new Pair<>(pos, adapter.getItemId(pos)));
                }
            }

            if (idsFound) {
                // We found some IDs that were checked. Let's now restore the multi-selection
                // state.
                mTempIdsToCheckOnRestore = null; // clear out this temp field
                mActionMode = mActivity.startSupportActionMode(Controller.this);
            }
        }

        public boolean saveInstanceState(Bundle outBundle) {
            // TODO: support non-stable IDs by persisting positions instead of IDs
            if (mActionMode != null && mListView.getAdapter().hasStableIds()) {
                long[] checkedIds = mListView.getCheckedItemIds();
                outBundle.putLongArray(getStateKey(), checkedIds);
                return true;
            }

            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            if (mListener.onCreateActionMode(actionMode, menu)) {
                mActionMode = actionMode;
                mOldItemClickListener = mListView.getOnItemClickListener();
                mListView.setOnItemClickListener(Controller.this);
                mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                mHandler.removeCallbacks(mSetChoiceModeNoneRunnable);

                if (mItemsToCheck != null) {
                    for (Pair<Integer, Long> posAndId : mItemsToCheck) {
                        mListView.setItemChecked(posAndId.first, true);
                        mListener.onItemCheckedStateChanged(mActionMode, posAndId.first,
                                posAndId.second, true);
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            if (mListener.onPrepareActionMode(actionMode, menu)) {
                mActionMode = actionMode;
                return true;
            }
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return mListener.onActionItemClicked(actionMode, menuItem);
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mListener.onDestroyActionMode(actionMode);
            SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions();
            if (checkedPositions != null) {
                for (int i = 0; i < checkedPositions.size(); i++) {
                    mListView.setItemChecked(checkedPositions.keyAt(i), false);
                }
            }
            mListView.setOnItemClickListener(mOldItemClickListener);
            mActionMode = null;
            mHandler.post(mSetChoiceModeNoneRunnable);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (mActionMode == null) {
                mItemsToCheck = new HashSet<>();
                mItemsToCheck.add(new Pair<>(position, id));
                mActionMode = mActivity.startSupportActionMode(Controller.this);
            } else {
                boolean checked = mListView.isItemChecked(position);
                mListView.setItemChecked(position, !checked);
                mListener.onItemCheckedStateChanged(mActionMode, position, id, !checked);

                int numChecked = 0;
                SparseBooleanArray checkedItemPositions = mListView.getCheckedItemPositions();
                if (checkedItemPositions != null) {
                    for (int i = 0; i < checkedItemPositions.size(); i++) {
                        numChecked += checkedItemPositions.valueAt(i) ? 1 : 0;
                    }
                }

                if (numChecked <= 0) {
                    mActionMode.finish();
                }
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,
                long id) {
            if (mActionMode != null) {
                return false;
            }

            mItemsToCheck = new HashSet<>();
            mItemsToCheck.add(new Pair<>(position, id));
            mActionMode = mActivity.startSupportActionMode(Controller.this);
            return true;
        }

        public boolean isActionModeStarted() {
            return mActionMode != null;
        }

        public void startActionMode() {
            if (mActionMode != null) {
                return;
            }
            mActionMode = mActivity.startSupportActionMode(Controller.this);
        }

        private void readInstanceState(Bundle savedInstanceState) {
            mTempIdsToCheckOnRestore = null;
            if (savedInstanceState != null) {
                long[] checkedIds = savedInstanceState.getLongArray(getStateKey());
                if (checkedIds != null && checkedIds.length > 0) {
                    mTempIdsToCheckOnRestore = new HashSet<>();
                    for (long id : checkedIds) {
                        mTempIdsToCheckOnRestore.add(id);
                    }
                }
            }
        }

        private String getStateKey() {
            return MultiSelectionUtils.class.getSimpleName() + "_" + mListView.getId();
        }
    }
}

