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

package com.drextended.actionhandler.action;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Base action for implementing call a network request with Observer pattern (ex. using Retrofit + RxJava)
 *
 * @param <RM> The type of network response
 * @param <M>  The type of model which can be handled
 */
public abstract class ObservableRequestAction<RM, M> extends RequestAction<RM, M> {
    protected Subscription mSubscription;

    public ObservableRequestAction() {
    }

    public ObservableRequestAction(boolean showProgressEnabled, boolean showDialog) {
        super(showProgressEnabled, showDialog);
    }

    @Override
    protected void onMakeRequest(final Context context, final View view, final String actionType, final M model) {
        final Observable<RM> observableRequest = getRequest(context, view, actionType, model);
        if (observableRequest == null) {
            hideProgressDialog();
            return;
        }
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
        mSubscription = observableRequest
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<RM>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        onResponseError(context, view, actionType, model, e);
                    }

                    @Override
                    public void onNext(RM response) {
                        onResponseSuccess(context, view, actionType, model, response);
                    }
                });
    }


    @Nullable
    protected abstract Observable<RM> getRequest(Context context, View view, String actionType, M model);
}
