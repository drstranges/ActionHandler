/*
 *  Copyright Roman Donchenko. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.drextended.actionhandler.util;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Helper to handle debounce time
 * Created on 25.07.2017.
 */

public class DebounceHelper {
    private final HashMap<String, Long> mDebounceMap = new HashMap<>();
    private final ReentrantReadWriteLock mLock = new ReentrantReadWriteLock();

    /**
     * Check if time {@param debounceMillis} elapsed since last timer reset by call {@link #resetTime}
     * or {@link #checkTimeAndResetIfElapsed(String, long)}
     *
     * @param tag            the tag
     * @param debounceMillis the debounce time for defined tag
     * @return true if debounce time has been elapsed since last call, false otherwise
     */
    public boolean checkTimeElapsed(String tag, long debounceMillis) {
        mLock.readLock().lock();
        Long lastCallMillis = this.mDebounceMap.get(tag);
        mLock.readLock().unlock();
        long nowMillis = System.currentTimeMillis();
        return lastCallMillis == null
                || nowMillis - lastCallMillis > debounceMillis;
    }

    /**
     * Reset timer for specific tag
     *
     * @param tag the tag
     */
    public void resetTime(String tag){
        mLock.writeLock().lock();
        this.mDebounceMap.put(tag, System.currentTimeMillis());
        mLock.writeLock().unlock();
    }

    /**
     * Check if time {@param debounceMillis} elapsed since last timer reset by call {@link #resetTime}
     * or {@link #checkTimeAndResetIfElapsed(String, long)}
     *
     * @param tag            the tag
     * @param debounceMillis the debounce time for defined tag
     * @return true if debounce time has been elapsed since last call, false otherwise
     */
    public boolean checkTimeAndResetIfElapsed(String tag, long debounceMillis) {
        mLock.readLock().lock();
        Long lastCallMillis = this.mDebounceMap.get(tag);
        mLock.readLock().unlock();
        long nowMillis = System.currentTimeMillis();
        if(lastCallMillis == null || nowMillis - lastCallMillis > debounceMillis) {
            mLock.writeLock().lock();
            this.mDebounceMap.put(tag, nowMillis);
            mLock.writeLock().unlock();
            return true;
        }
        return false;
    }
}
