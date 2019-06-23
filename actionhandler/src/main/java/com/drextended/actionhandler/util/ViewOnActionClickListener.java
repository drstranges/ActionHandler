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

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drextended.actionhandler.listener.ActionClickListener;

public class ViewOnActionClickListener implements View.OnClickListener, View.OnLongClickListener {

    @NonNull
    private final ActionClickListener actionHandler;
    @Nullable
    private final String actionType;
    @Nullable
    private final String actionTypeLongClick;
    @Nullable
    private final Object model;
    @Nullable
    private final Object modelLongClick;
    @Nullable
    private final Object actionTypeTag;

    public ViewOnActionClickListener(
            @NonNull ActionClickListener actionHandler,
            @Nullable String actionType,
            @Nullable String actionTypeLongClick,
            @Nullable Object model,
            @Nullable Object modelLongClick,
            @Nullable Object actionTypeTag
    ) {

        this.actionHandler = actionHandler;
        this.actionType = actionType;
        this.actionTypeLongClick = actionTypeLongClick;
        this.model = model;
        this.modelLongClick = modelLongClick;
        this.actionTypeTag = actionTypeTag;
    }

    public ViewOnActionClickListener(
            @NonNull ActionClickListener actionHandler,
            @Nullable Object model,
            @Nullable String actionType
    ) {
        this(actionHandler, actionType, actionType, model, model, null);
    }

    public ViewOnActionClickListener(
            @NonNull ActionClickListener actionHandler,
            @Nullable Object model,
            @Nullable String actionType,
            @Nullable String actionTypeLongClick
    ) {
        this(actionHandler, actionType, actionTypeLongClick, model, model, null);
    }

    @Override
    public void onClick(View v) {
        if (actionType != null) {
            actionHandler.onActionClick(v, actionType, model, actionTypeTag);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (actionTypeLongClick != null) {
            actionHandler.onActionClick(v, actionTypeLongClick, modelLongClick, actionTypeTag);
            return true;
        }
        return false;
    }
}
