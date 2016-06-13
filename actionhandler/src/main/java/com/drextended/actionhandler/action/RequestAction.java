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

import com.drextended.actionhandler.R;
import com.drextended.actionhandler.util.ProgressBarController;

/**
 * Base action for implementing call a network request
 *
 * @param <RM> The type of network response
 * @param <M>  The type of model which can be handled
 */
public abstract class RequestAction<RM, M> extends DialogAction<M> {
    protected boolean mShowProgressEnabled;
    protected boolean mShowDialog;

    public RequestAction() {
    }

    /**
     * Base action for implementing call a network request
     *
     * @param showProgressEnabled Set true to show progress dialog while request
     * @param showDialog          Set true to show dialog before action fired
     */
    public RequestAction(boolean showProgressEnabled, boolean showDialog) {
        mShowProgressEnabled = showProgressEnabled;
        mShowDialog = showDialog;
    }

    /**
     * Set progress dialog enabled
     *
     * @param showProgressEnabled Set true to show progress dialog while request
     */
    public void setShowProgressEnabled(boolean showProgressEnabled) {
        mShowProgressEnabled = showProgressEnabled;
    }

    /**
     * Set dialog before action fired enabled
     *
     * @param showDialog Set true to show dialog before action fired
     */
    public void setShowDialog(boolean showDialog) {
        mShowDialog = showDialog;
    }

    @Override
    public void onFireAction(Context context, @Nullable View view, String actionType, @Nullable M model) {
        if (mShowDialog) {
            super.onFireAction(context, view, actionType, model);
        } else {
            makeRequest(context, view, actionType, model);
        }
    }

    @Override
    protected void onDialogActionFire(Context context, View view, String actionType, M model) {
        makeRequest(context, view, actionType, model);
    }

    /**
     * Prepare and call a network request
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire
     * @param actionType Type of the action which was executed. Can be null.
     * @param model      The model which should be handled by the action. Can be null.
     */
    public void makeRequest(Context context, View view, String actionType, final M model) {
        onRequestStarted(context, view, actionType, model);
        onMakeRequest(context, view, actionType, model);
    }

    /**
     * Called on request started. Shows progress dialog if enabled.
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire
     * @param actionType Type of the action which was executed. Can be null.
     * @param model      The model which should be handled by the action. Can be null.
     */
    protected void onRequestStarted(Context context, View view, String actionType, M model) {
        if (mShowProgressEnabled) ProgressBarController
                .showProgressDialog(context, getProgressDialogMessage(context, view, actionType, model));
    }

    /**
     * Provides message for progress dialog
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire
     * @param actionType Type of the action which was executed. Can be null.
     * @param model      The model which should be handled by the action. Can be null.
     * @return message for progress dialog
     */
    protected String getProgressDialogMessage(Context context, View view, String actionType, M model) {
        return context.getString(R.string.action_handler_dialog_message_wait);
    }

    /**
     * Called on request has been fired successfully.
     * Hides progress dialog if enabled and call {@link #notifyOnActionFired}
     * Should be called manually in request callback.
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire
     * @param actionType Type of the action which was executed.
     * @param oldModel   The model which was used in request.
     * @param response   network response
     */
    protected void onSuccess(Context context, View view, String actionType, M oldModel, RM response) {
        if (mShowProgressEnabled) ProgressBarController.hideProgressDialog();
        notifyOnActionFired(actionType, oldModel);
    }

    /**
     * Called if request returns error.
     * Hides progress dialog if enabled
     * Should be called manually in request callback.
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire
     * @param actionType Type of the action which was executed.
     * @param oldModel   The model which was used in request.
     * @param e          The Error
     */
    protected void onError(Context context, View view, String actionType, M oldModel, Throwable e) {
        if (mShowProgressEnabled) ProgressBarController.hideProgressDialog();
    }

    /**
     * Implement network request there.
     * Note: You should call {@link #onSuccess(Context, View, String, Object, Object)} if request finished successfully
     * and {@link #onError(Context, View, String, Object, Throwable)} if it is failed.
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire
     * @param actionType Type of the action which was executed.
     * @param model      The model which was used in request.
     */
    protected abstract void onMakeRequest(Context context, View view, String actionType, final M model);

}
