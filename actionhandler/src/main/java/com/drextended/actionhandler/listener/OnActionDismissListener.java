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

import com.drextended.actionhandler.ActionArgs;

/**
 * Interface definition for a callback to be invoked when an action was dismissed.
 */
public interface OnActionDismissListener {

    /**
     * Called when error occurred while an action is executing.
     *
     * @param args   The action params, which appointed to the view
     *               and actual fired action type
     * @param reason The reason to dismiss
     */
    void onActionDismiss(
            @NonNull ActionArgs args,
            @Nullable String reason
    );
}
