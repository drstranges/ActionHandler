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

package com.drextended.actionhandlersample.action;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import android.view.View;

import com.drextended.actionhandler.ActionArgs;
import com.drextended.actionhandler.action.IntentAction;
import com.drextended.actionhandlersample.activity.SecondActivity;


public class OpenSecondActivity extends IntentAction<String> {
    @Override
    public boolean isModelAccepted(Object model) {
        return model instanceof String;
    }


    @Nullable
    @Override
    public Intent getIntent(@NonNull ActionArgs args) {
        return SecondActivity.getIntent(args.params.getViewOrAppContext(), args.params.getModel(String.class));
    }

    @Override
    protected void startActivity(@NonNull Context context, @NonNull Intent intent, @NonNull ActionArgs args) throws ActivityNotFoundException {
        ActivityOptionsCompat transition = prepareTransition(args);
        if (transition != null) {
            context.startActivity(intent, transition.toBundle());
        } else {
            super.startActivity(context, intent, args);
        }
    }

    private ActivityOptionsCompat prepareTransition(@NonNull ActionArgs args) {
        View view = args.params.tryGetView();
        Activity activity = args.params.tryGetActivity();

        if (activity == null || view == null) return null;
        return ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, view, SecondActivity.TRANSITION_NAME);
    }
}
