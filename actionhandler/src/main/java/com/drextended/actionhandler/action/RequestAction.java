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

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drextended.actionhandler.ActionArgs;
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
    protected boolean mShowDialogEnabled;
    protected String mProgressTag;

    public RequestAction() {
    }

    /**
     * Base action for implementing call a network request
     *
     * @param showProgressEnabled Set true to show progress dialog while request
     * @param showDialogEnabled   Set true to show dialog before action fired
     */
    public RequestAction(boolean showProgressEnabled, boolean showDialogEnabled) {
        mShowProgressEnabled = showProgressEnabled;
        mShowDialogEnabled = showDialogEnabled;
        if (mShowProgressEnabled) mProgressTag = getClass().getSimpleName();
    }

    /**
     * Set progress dialog enabled
     *
     * @param showProgressEnabled Set true to show progress dialog while request
     */
    public void setShowProgressEnabled(boolean showProgressEnabled) {
        mShowProgressEnabled = showProgressEnabled;
        if (mProgressTag == null && mShowProgressEnabled) mProgressTag = getClass().getSimpleName();
    }

    /**
     * Set dialog before action fired enabled
     *
     * @param showDialogEnabled Set true to show dialog before action fired
     */
    public void setShowDialogEnabled(boolean showDialogEnabled) {
        mShowDialogEnabled = showDialogEnabled;
    }

    @Override
    public void onFireAction(@NonNull ActionArgs args) {
        if (mShowDialogEnabled) {
            super.onFireAction(args);
        } else {
            makeRequest(args);
        }
    }

    @Override
    protected void onDialogActionFire(@NonNull ActionArgs args) {
        makeRequest(args);
    }

    /**
     * Prepare and call a network request
     *
     * @param args The action params, which appointed to the view and actually actionType
     */
    public void makeRequest(@NonNull ActionArgs args) {
        onRequestStarted(args);
        onMakeRequest(args);
    }

    /**
     * Called on request started. Shows progress dialog if enabled.
     *
     * @param args The action params, which appointed to the view and actually actionType
     */
    protected void onRequestStarted(@NonNull ActionArgs args) {
        if (mShowProgressEnabled) showProgressDialog(args);
    }

    /**
     * Provides message for progress dialog
     *
     * @param args The action params, which appointed to the view and actually actionType
     * @return message for progress dialog
     */
    protected String getProgressDialogMessage(@NonNull ActionArgs args) {
        return args.params.appContext.getString(R.string.action_handler_dialog_message_wait);
    }

    /**
     * Called on request has been fired successfully.
     * Hides progress dialog if enabled and call {@link #notifyOnActionFired}
     * Should be called manually in request callback.
     *
     * @param args     The action params, which appointed to the view and actually actionType
     * @param response network response
     */
    @CallSuper
    protected void onResponseSuccess(@NonNull ActionArgs args, @Nullable RM response) {
        if (mShowProgressEnabled) hideProgressDialog();
        notifyOnActionFired(args, response);
    }

    /**
     * Call for show progress dialog
     *
     * @param args The action params, which appointed to the view and actually actionType
     */
    public void showProgressDialog(@NonNull ActionArgs args) {
        ProgressBarController.showProgressDialog(
                args.params.getViewOrAppContext(),
                mProgressTag,
                getProgressDialogMessage(args)
        );
    }

    /**
     * Call for hide progress dialog
     */
    public void hideProgressDialog() {
        ProgressBarController.hideProgressDialog(mProgressTag);
    }

    /**
     * Called if request returns error.
     * Hides progress dialog if enabled
     * Should be called manually in request callback.
     *
     * @param args The action params, which appointed to the view and actually actionType
     * @param e    The Error
     */
    @CallSuper
    protected void onResponseError(@NonNull ActionArgs args, @NonNull Throwable e) {
        if (mShowProgressEnabled) hideProgressDialog();
        notifyOnActionError(args, e);
    }

    /**
     * Implement network request there.
     * Note: You should call {@link #onResponseSuccess(ActionArgs, Object)} if request finished successfully
     * and {@link #onResponseError(ActionArgs, Throwable)} if it is failed.
     *
     * @param args The action params, which appointed to the view and actually actionType
     */
    protected abstract void onMakeRequest(@NonNull ActionArgs args);

}
