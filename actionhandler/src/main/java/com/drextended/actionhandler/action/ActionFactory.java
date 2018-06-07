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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Used by ActionHandler for lazy instantiating actions.
 */
public interface ActionFactory {

    /**
     * Called when ActionHandler have not had the action to handle given actionType yet.
     * When ActionHandler already have the action for the actionType, this method will not be called
     *
     * @param actionType the actionType to handle
     * @return actions which can handle given action type
     */
    @Nullable
    Action[] provideActions(@NonNull String actionType);

}
