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
    protected String getDialogMessage(Context context, String actionType, String model) {
        return context.getString(R.string.action_request_dialog_message, model);
    }

    @Override
    protected void onMakeRequest(final Context context, final View view, final String actionType, final String model) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (context instanceof Activity && ((Activity) context).isFinishing()) return;
                if (mCount++ % 3 == 0) {
                    onResponseError(context, view, actionType, model, new Exception("Error!) Try request one more time"));
                } else {
                    onResponseSuccess(context, view, actionType, model, "Request has been done successfully");
                }
            }
        }, 3000);
    }

    @Override
    protected void onResponseSuccess(Context context, View view, String actionType, String oldModel, String response) {
        super.onResponseSuccess(context, view, actionType, oldModel, response);
        Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResponseError(Context context, View view, String actionType, String oldModel, Throwable e) {
        super.onResponseError(context, view, actionType, oldModel, e);
        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
