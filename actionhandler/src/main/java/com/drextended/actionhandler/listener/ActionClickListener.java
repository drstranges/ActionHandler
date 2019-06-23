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

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Interface definition for a callback to be invoked when a view with an action is clicked.
 */
public interface ActionClickListener {
    /**
     * Called when a view with an action is clicked.
     *
     * @param view       The view that was clicked.
     * @param actionType The action type, which appointed to the view
     * @param model      The model, which  appointed to the view and should be handled
     * @param actionTag  The tag, which can be used to distinct click source or etc.
     */
    void onActionClick(
            @NonNull final View view,
            @Nullable final String actionType,
            @Nullable final Object model,
            @Nullable final Object actionTag
    );
}
