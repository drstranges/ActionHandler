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

package com.drextended.actionhandler.action;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Base Interface for any action
 *
 * @param <M> Type of model, which can be handled by the action
 */
public interface Action<M> {

    /**
     * Check if action can handle given model
     *
     * @param model The model to check if it can be handled.
     * @return true if the action can handle this model, false otherwise.
     */
    boolean isModelAccepted(Object model);


    /**
     * Executes the action. Should be called only if {@link #isModelAccepted(Object)} return true
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The View, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire.
     * @param actionType Type of the action which was executed. Can be null.
     * @param model      The model which should be handled by the action. Can be null.
     */
    void onFireAction(Context context, @Nullable View view, @Nullable String actionType, @Nullable M model);
}
