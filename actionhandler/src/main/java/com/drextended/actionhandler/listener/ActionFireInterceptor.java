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

package com.drextended.actionhandler.listener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drextended.actionhandler.ActionPair;
import com.drextended.actionhandler.ActionParams;
import com.drextended.actionhandler.action.Action;

/**
 * Interface definition for a callback to be invoked right before specific action will be fired.
 * If {@link #onInterceptActionFire(ActionParams, String, Action)} return true
 * then this action will not be fired.
 */
public interface ActionFireInterceptor {
    /**
     * Called right before specific action will be fired
     * If return true then this action will not be fired.
     *
     * @param actionParams The action params, which appointed to the view
     * @param actionType   The action type, which is prepared to fire.
     *                     May be different from actionParams.actionType
     *                     when intercepted actionItem in CompositeAction
     *                     or when actionType of fired action is null that match to any
     * @param action       The action, which is prepared to fire.
     * @return true for intercept the action, false to handle the action in normal way.
     */
    boolean onInterceptActionFire(
            @NonNull ActionParams actionParams,
            @Nullable String actionType,
            @NonNull Action action
    );
}
