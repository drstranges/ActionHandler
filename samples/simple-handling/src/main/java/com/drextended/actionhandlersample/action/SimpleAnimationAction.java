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

import android.animation.ObjectAnimator;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.drextended.actionhandler.ActionArgs;
import com.drextended.actionhandler.action.Action;
import com.drextended.actionhandler.action.BaseAction;

public class SimpleAnimationAction extends BaseAction {

    @Override
    public boolean isModelAccepted(Object model) {
        return true;
    }

    @Override
    public void onFireAction(@NonNull ActionArgs args) {
        View view = args.params.tryGetView();
        if (view == null) {
            notifyOnActionDismiss(args, "No view");
            return;
        }
        ObjectAnimator
                .ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
                .setDuration(200)
                .start();
        notifyOnActionFired(args);
    }
}
