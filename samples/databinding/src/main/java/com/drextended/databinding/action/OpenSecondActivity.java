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

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import android.view.View;

import com.drextended.actionhandler.action.IntentAction;
import com.drextended.databinding.view.SecondActivity;


public class OpenSecondActivity extends IntentAction<String> {
    @Override
    public boolean isModelAccepted(Object model) {
        return model instanceof String;
    }

    @Nullable
    @Override
    public Intent getIntent(@Nullable View view, Context context, String actionType, String model) {
        return SecondActivity.getIntent(context, model);
    }

    @Override
    protected ActivityOptionsCompat prepareTransition(Context context, View view, Intent intent) {
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else if (context instanceof ContextWrapper) {
            final Context baseContext = ((ContextWrapper) context).getBaseContext();
            if (baseContext instanceof Activity) {
                activity = (Activity) baseContext;
            }
        }

        if (activity == null) return null;
        return ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, view, SecondActivity.TRANSITION_NAME);
    }
}
