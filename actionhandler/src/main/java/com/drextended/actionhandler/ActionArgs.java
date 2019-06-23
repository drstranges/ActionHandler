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

import java.util.Objects;

public class ActionArgs {

    /**
     * The actual action type that fires. If actionType is null, that action match to any actionType
     */
    @Nullable
    public final String fireActionType;

    @NonNull
    public final ActionParams params;


    public ActionArgs(@NonNull ActionParams params, @Nullable String fireActionType) {
        this.params = params;
        this.fireActionType = fireActionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionArgs that = (ActionArgs) o;
        return Objects.equals(fireActionType, that.fireActionType) &&
                params.equals(that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fireActionType, params);
    }
}
