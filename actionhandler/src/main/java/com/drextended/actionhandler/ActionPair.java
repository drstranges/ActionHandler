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

package com.drextended.actionhandler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drextended.actionhandler.action.Action;

/**
 * Holder for an action and corresponded type
 */
public class ActionPair {

    /**
     * If actionType is null, that action match to any actionType
     */
    @Nullable
    public final String actionType;

    @NonNull
    public final Action action;

    public ActionPair(@Nullable String actionType, @NonNull Action action) {
        this.actionType = actionType;
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ActionPair that = (ActionPair) o;

        if (actionType != null ? !actionType.equals(that.actionType) : that.actionType != null)
            return false;
        return action.equals(that.action);

    }

    @Override
    public int hashCode() {
        int result = actionType != null ? actionType.hashCode() : 0;
        result = 31 * result + action.hashCode();
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "ActionPair{" +
                "actionType='" + actionType + '\'' +
                ", action=" + action +
                '}';
    }
}
