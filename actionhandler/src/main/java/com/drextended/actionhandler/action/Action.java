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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drextended.actionhandler.ActionArgs;

/**
 * Base Interface for any action
 */
public interface Action {

    /**
     * Check if action can handle given model
     *
     * @param model The model to check if it can be handled.
     * @return true if the action can handle this model, false otherwise.
     */
    boolean isModelAccepted(@Nullable Object model);


    /**
     * Executes the action. Should be called only if {@link #isModelAccepted(Object)} return true
     *
     * @param args          The action params, which appointed to the view
     *                      and type of the action which was actually executed.
     */
    void onFireAction(@NonNull ActionArgs args);
}
