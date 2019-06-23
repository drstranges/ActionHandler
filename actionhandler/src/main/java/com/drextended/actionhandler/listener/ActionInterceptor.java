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

import com.drextended.actionhandler.ActionParams;

/**
 * Interface definition for a callback to be invoked after a view with an action is clicked
 * and before action type handling started. If {@link #onInterceptAction(ActionParams)} return true
 * then this action type will not be handled.
 */
public interface ActionInterceptor {
    /**
     * Called after a view with an action is clicked
     * and before action handling started. If return true then this action will not be handled.
     *
     * @param params The actionParams
     * @return true for intercept the action, false to handle the action in normal way.
     */
    boolean onInterceptAction(@NonNull ActionParams params);
}
