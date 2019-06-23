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
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drextended.actionhandler.ActionArgs;
import com.drextended.actionhandler.ActionParams;
import com.drextended.actionhandler.action.RequestAction;
import com.drextended.actionhandlersample.R;

public class SampleRequestAction extends RequestAction<String, String> {

    private int mCount;

    public SampleRequestAction() {
        super(true, true);
    }

    @Override
    public boolean isModelAccepted(Object model) {
        return model instanceof String;
    }

    @Override
    protected String getDialogMessage(@NonNull ActionParams params) {
        return params.appContext.getString(R.string.action_request_dialog_message, params.model);
    }

    @Override
    protected void onMakeRequest(@NonNull ActionArgs args) {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            Activity activity = args.params.tryGetActivity();
            if (activity != null && (activity.isFinishing() || activity.isDestroyed())) return;
            if (mCount++ % 3 == 0) {
                onResponseError(args, new Exception("Test Error!:) Just repeat this request!"));
            } else {
                onResponseSuccess(args, "Request has been done successfully");
            }
        }, 3000);
    }

    @Override
    protected void onResponseSuccess(@NonNull ActionArgs args, @Nullable String response) {
        super.onResponseSuccess(args, response);
        Toast.makeText(args.params.appContext, response, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResponseError(@NonNull ActionArgs args, @NonNull Throwable e) {
        super.onResponseError(args, e);
    }
}
