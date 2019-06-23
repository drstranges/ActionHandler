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

package com.drextended.databinding.action;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.drextended.actionhandler.ActionArgs;
import com.drextended.actionhandler.action.BaseAction;
import com.drextended.databinding.R;

public class ShowToastAction extends BaseAction {

    @Override
    public boolean isModelAccepted(Object model) {
        return model instanceof String;
    }

    @Override
    public void onFireAction(@NonNull ActionArgs args) {
        Context appContext = args.params.appContext;

        Toast.makeText(
                appContext,
                appContext.getString(R.string.toast_message, args.params.model),
                Toast.LENGTH_SHORT
        ).show();

        notifyOnActionFired(args);
    }
}
