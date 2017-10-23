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
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.drextended.actionhandler.action.RxRequestAction;
import com.drextended.databinding.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

public class SampleRxRequestAction extends RxRequestAction<String, String> {

    private int mCount;

    public SampleRxRequestAction() {
        super(true, true);
    }

    @Nullable
    @Override
    protected Single<String> getRequest(Context context, View view, String actionType, String model, Object payload) {
        if (mCount++ % 3 == 0) {
            return Single.just("").delay(2000, TimeUnit.MILLISECONDS).flatMap(new Function<String, SingleSource<? extends String>>() {

                @Override
                public SingleSource<? extends String> apply(String s) throws Exception {
                    return Single.error(new Throwable("Request has failed"));
                }
            });
        } else {
            return Single.just("Request has been done successfully").delay(2000, TimeUnit.MILLISECONDS);
        }
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
