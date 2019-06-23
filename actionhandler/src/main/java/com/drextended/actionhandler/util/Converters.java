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

import androidx.databinding.BindingAdapter;

import com.drextended.actionhandler.listener.ActionClickListener;

/**
 * Helper class for collect all data binding adapters in one place
 */
public class Converters {

    /**
     * Binding adapter to assign an action to a view using android data binding approach.
     * Sample:
     * <pre>
     * &lt;Button
     *     android:layout_width="wrap_content"
     *     android:layout_height="wrap_content"
     *
     *     android:actionHandler="@{someActionHandler}"
     *     android:actionType='@{"send_message"}'
     *     android:actionTypeLongClick='@{"show_menu"}'
     *     android:model="@{user}"
     *     android:modelLongClick="@{userSettings}"
     *     android:actionTag='@{"analytics_tag_1"}'
     *
     *     android:text="@string/my_button_text"/&gt;
     * </pre>
     *
     * @param view                The View to bind an action
     * @param actionHandler       The action handler which will handle an action
     * @param actionType          The action type, which will be handled on view clicked
     * @param actionTypeLongClick The action type, which will be handled on view long clicked
     * @param model               The model which will be handled
     * @param modelLongClick      The model which will be handled for long click. If null, {@code model} will be used
     * @param actionTag           The tag. CAn be used to distinct click source
     */
    @BindingAdapter(
            value = {
                    "actionHandler",
                    "actionType",
                    "actionTypeLongClick",
                    "model",
                    "modelLongClick",
                    "actionTag"
            },
            requireAll = false
    )
    public static void setActionHandler(
            final View view,
            final ActionClickListener actionHandler,
            final String actionType,
            final String actionTypeLongClick,
            final Object model,
            final Object modelLongClick,
            final Object actionTag
    ) {
        if (actionHandler != null) {
            ViewOnActionClickListener clickListener = new ViewOnActionClickListener(
                    actionHandler,
                    actionType,
                    actionTypeLongClick,
                    model,
                    modelLongClick == null ? model : modelLongClick,
                    actionTag
            );
            if (actionType != null) view.setOnClickListener(clickListener);
            if (actionTypeLongClick != null) view.setOnLongClickListener(clickListener);
        } else {
            if (actionType != null) view.setOnClickListener(null);
            if (actionTypeLongClick != null) view.setOnLongClickListener(null);
        }
    }
}
