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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.drextended.actionhandler.ActionArgs;
import com.drextended.actionhandler.ActionParams;
import com.drextended.actionhandler.action.RxRequestAction;
import com.drextended.databinding.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.functions.Function;

public class SampleRxRequestAction extends RxRequestAction<String, String> {

    private int mCount;

    public SampleRxRequestAction() {
        super(true, true);
    }

    @Nullable
    @Override
    protected Maybe<String> getRequest(@NonNull ActionArgs args) {
        if (mCount++ % 3 == 0) {
            return Maybe.just("")
                    .delay(2000, TimeUnit.MILLISECONDS)
                    .flatMap(s -> Maybe.error(new Throwable("Request has failed")));
        } else {
            return Maybe.just("Request has been done successfully")
                    .delay(2000, TimeUnit.MILLISECONDS);
        }
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
    protected void onResponseSuccess(@NonNull ActionArgs args, @Nullable String response) {
        super.onResponseSuccess(args, response);
        Toast.makeText(args.params.appContext, response, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResponseError(@NonNull ActionArgs args, @NonNull Throwable e) {
        super.onResponseError(args, e);
        Toast.makeText(args.params.appContext, e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
