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

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Base action for implementing call a network request with RxJava Observable (ex. using Retrofit + RxJava)
 *
 * @param <RM> The type of network response
 * @param <M>  The type of model which can be handled
 */
public abstract class RxRequestAction<RM, M> extends RequestAction<RM, M> implements Cancelable {
    protected Subscription mSubscription;

    public RxRequestAction() {
    }

    public RxRequestAction(boolean showProgressEnabled, boolean showDialog) {
        super(showProgressEnabled, showDialog);
    }

    @Override
    protected void onMakeRequest(final Context context, final View view, final String actionType, final M model, Object payload) {
        final Observable<RM> observableRequest = getRequest(context, view, actionType, model, payload);
        if (observableRequest == null) {
            if (mShowProgressEnabled) hideProgressDialog();
            return;
        }
        unsubscribe(mSubscription);
        mSubscription = observableRequest
                .compose(applySchedulers())
                .subscribe(new Subscriber<RM>() {
                    @Override
                    public void onCompleted() {
                        onResponseCompleted(context, view, actionType, model);
                    }

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

    /**
     * Override this method if you want to apply custom schedulers for request observable.
     * By default {@code Schedulers.io()} applied for subscribeOn,
     * and {@code AndroidSchedulers.mainThread()} for observeOn.
     * @return transformer for apply schedulers
     */
    @NonNull
    protected Observable.Transformer<RM, RM> applySchedulers() {
        return new Observable.Transformer<RM, RM>() {
            @Override
            public Observable<RM> call(Observable<RM> r) {
                return r.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * Helper method to unsubscribe from subscription
     *
     * @param subscription subscription to unsubscribe
     */
    protected void unsubscribe(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
    }

    /**
     * Unsubscribes from request observable once this method is called.
     * Override this if you need other behaviour.
     * For actions collected by {@link ActionHandler} this method can be called by {@link ActionHandler#cancelAll()}
     */
    @Override
    public void cancel() {
        unsubscribe(mSubscription);
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
     * @return request observable.
     * @deprecated use {@link #getRequest(Context, View, String, Object, Object)}
     */
    @Deprecated
    @Nullable
    protected Observable<RM> getRequest(Context context, View view, String actionType, M model) {
        return getRequest(context, view, actionType, model, null);
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
     * @return request observable.
     */
    @Nullable
    protected Observable<RM> getRequest(Context context, View view, String actionType, M model, @Nullable Object payload) {
        return null;
    }

    /**
     * Called when request observable emits "onComplete" event.
     * Hides progress dialog if enabled and call {@link #notifyOnActionFired}
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire
     * @param actionType Type of the action which was executed.
     * @param oldModel   The model which was used in request.
     */
    protected void onResponseCompleted(Context context, View view, String actionType, M oldModel) {
        if (mShowProgressEnabled) hideProgressDialog();
        notifyOnActionFired(view, actionType, oldModel);
    }

    /**
     * Called on request observable emits "onNext" event.
     * Overrides super, so that not hides progress dialog and not call {@link #notifyOnActionFired} here.
     * See {@link #onResponseCompleted(Context, View, String, Object)} for that.
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire
     * @param actionType Type of the action which was executed.
     * @param oldModel   The model which was used in request.
     * @param response   network response
     */
    @Override
    protected void onResponseSuccess(Context context, View view, String actionType, M oldModel, RM response) {
        //super.onResponseSuccess(context, view, actionType, oldModel, response);
    }
}
