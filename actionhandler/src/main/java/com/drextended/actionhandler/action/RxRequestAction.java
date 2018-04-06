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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.drextended.actionhandler.ActionHandler;

import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.MaybeTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Base action for implementing call a network request with RxJava Observable (ex. using Retrofit + RxJava)
 *
 * @param <RM> The type of network response
 * @param <M>  The type of model which can be handled
 */
@SuppressWarnings("SameParameterValue")
public abstract class RxRequestAction<RM, M> extends RequestAction<RM, M> implements Cancelable {

    protected CompositeDisposable mDisposable;
    protected boolean mUnsubscribeOnNewRequest = true;

    public RxRequestAction() {
    }

    public RxRequestAction(boolean showProgressEnabled, boolean showDialog) {
        super(showProgressEnabled, showDialog);
    }

    public RxRequestAction(boolean showProgressEnabled, boolean showDialog, boolean unsubscribeOnNewRequest) {
        super(showProgressEnabled, showDialog);
        this.mUnsubscribeOnNewRequest = unsubscribeOnNewRequest;
    }

    @Override
    protected void onMakeRequest(final Context context, final View view, final String actionType, final M model, Object payload) {
        final Maybe<RM> observableRequest = getRequest(context, view, actionType, model, payload);
        if (observableRequest == null) {
            if (mShowProgressEnabled) hideProgressDialog();
            return;
        }
        if (mUnsubscribeOnNewRequest) {
            dispose(mDisposable);
        }
        if (mDisposable == null || mDisposable.isDisposed()) {
            mDisposable = new CompositeDisposable();
        }
        mDisposable.add(observableRequest
                .compose(applySchedulers())
                .subscribeWith(new DisposableMaybeObserver<RM>() {
                    private volatile boolean hasResponse = false;

                    @Override
                    public void onSuccess(RM response) {
                        hasResponse = true;
                        onResponseSuccess(context, view, actionType, model, response);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onResponseError(context, view, actionType, model, e);
                    }

                    @Override
                    public void onComplete() {
                        if (!hasResponse) {
                            onResponseSuccess(context, view, actionType, model, null);
                        }
                    }
                }));
    }

    /**
     * Override this method if you want to apply custom schedulers for request flow.
     * By default {@code Schedulers.io()} applied for subscribeOn,
     * and {@code AndroidSchedulers.mainThread()} for observeOn.
     * @return transformer for apply schedulers
     */
    @NonNull
    protected MaybeTransformer<RM, RM> applySchedulers() {
        return new MaybeTransformer<RM, RM>() {
            @Override
            public MaybeSource<RM> apply(Maybe<RM> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * Helper method to dispose the call
     *
     * @param disposable disposable to dispose
     */
    protected void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) disposable.dispose();
    }

    /**
     * Unsubscribes from request observable once this method is called.
     * Override this if you need other behaviour.
     * For actions collected by {@link ActionHandler} this method can be called by {@link ActionHandler#cancelAll()}
     */
    @Override
    public void cancel() {
        dispose(mDisposable);
    }

    /**
     * Implement network request observable there.
     * By default {@code Schedulers.io()} applied for subscribeOn,
     * and {@code AndroidSchedulers.mainThread()} for observeOn. If you want to apply custom schedulers
     * override {@link #applySchedulers()}
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire
     * @param actionType Type of the action which was executed.
     * @param model      The model which was used in request.
     * @param payload    The payload from {@link #makeRequest(Context, View, String, Object, Object)}.
     * @return request observable.
     */
    @Nullable
    protected abstract Maybe<RM> getRequest(Context context, View view, String actionType, M model, @Nullable Object payload);

}
