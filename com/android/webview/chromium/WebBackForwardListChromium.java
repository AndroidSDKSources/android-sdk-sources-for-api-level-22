/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.webview.chromium;

import org.chromium.content_public.browser.NavigationHistory;

import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * WebView Chromium implementation of WebBackForwardList. Simple immutable
 * wrapper around NavigationHistory.
 */
public class WebBackForwardListChromium extends WebBackForwardList {
    private final List<WebHistoryItemChromium> mHistroryItemList;
    private final int mCurrentIndex;

    /* package */ WebBackForwardListChromium(NavigationHistory nav_history) {
        mCurrentIndex = nav_history.getCurrentEntryIndex();
        mHistroryItemList = new ArrayList<WebHistoryItemChromium>(nav_history.getEntryCount());
        for (int i = 0; i < nav_history.getEntryCount(); ++i) {
            mHistroryItemList.add(
                    new WebHistoryItemChromium(nav_history.getEntryAtIndex(i)));
        }
    }

    /**
     * See {@link android.webkit.WebBackForwardList#getCurrentItem}.
     */
    @Override
    public synchronized WebHistoryItem getCurrentItem() {
        if (getSize() == 0) {
            return null;
        } else {
            return getItemAtIndex(getCurrentIndex());
        }
    }

    /**
     * See {@link android.webkit.WebBackForwardList#getCurrentIndex}.
     */
    @Override
    public synchronized int getCurrentIndex() {
        return mCurrentIndex;
    }

    /**
     * See {@link android.webkit.WebBackForwardList#getItemAtIndex}.
     */
    @Override
    public synchronized WebHistoryItem getItemAtIndex(int index) {
        if (index < 0 || index >= getSize()) {
            return null;
        } else {
            return mHistroryItemList.get(index);
        }
    }

    /**
     * See {@link android.webkit.WebBackForwardList#getSize}.
     */
    @Override
    public synchronized int getSize() {
        return mHistroryItemList.size();
    }

    // Clone constructor.
    private WebBackForwardListChromium(List<WebHistoryItemChromium> list,
                                       int currentIndex) {
        mHistroryItemList = list;
        mCurrentIndex = currentIndex;
    }

    /**
     * See {@link android.webkit.WebBackForwardList#clone}.
     */
    @Override
    protected synchronized WebBackForwardListChromium clone() {
        List<WebHistoryItemChromium> list =
                new ArrayList<WebHistoryItemChromium>(getSize());
        for (int i = 0; i < getSize(); ++i) {
            list.add(mHistroryItemList.get(i).clone());
        }
        return new WebBackForwardListChromium(list, mCurrentIndex);
    }
}
