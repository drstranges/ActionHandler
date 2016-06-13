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
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.drextended.actionhandler.liastener.OnActionFiredListener;

/**
 * Can be used for make an action which show simple dialog before it has been fired.
 * You can extend this class for make custom action on just wrap another action
 * using {@link #wrap(String, Action)}
 *
 * @param <M>    type of model to handle
 */
public abstract class DialogAction<M> extends BaseAction<M> {

    @Override
    public void onFireAction(final Context context, @Nullable final View view, final String actionType, @Nullable final M model) {
        final String title = getDialogTitle(context, actionType, model);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != null) builder.setTitle(title);
        builder.setMessage(getDialogMessage(context, actionType, model))
                .setNegativeButton(getNegativeButtonTitleResId(), null)
                .setPositiveButton(getPositiveButtonTitleResId(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onDialogActionFire(context, view, actionType, model);
                    }
                });
        builder.show();
    }

    /**
     * Provides title for dialog
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param actionType Type of the action which was executed. Can be null.
     * @param model      The model which should be handled by the action. Can be null.
     * @return title for dialog
     */
    protected String getDialogTitle(Context context, String actionType, M model) {
        return null;
    }

    /**
     * Provides message for dialog
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param actionType Type of the action which was executed. Can be null.
     * @param model      The model which should be handled by the action. Can be null.
     * @return message for dialog
     */
    protected abstract String getDialogMessage(Context context, String actionType, M model);

    /**
     * Provides resource id of the text to display in the positive button.
     * Set android.R.string.ok by default.
     *
     * @return resource id of the text to display in the positive button.
     */
    protected int getPositiveButtonTitleResId() {
        return android.R.string.ok;
    }

    /**
     * Provides resource id of the text to display in the negative button.
     * Set android.R.string.cancel by default.
     *
     * @return resource id of the text to display in the negative button.
     */
    protected int getNegativeButtonTitleResId() {
        return android.R.string.cancel;
    }

    /**
     * Executes the action. Called if positive button on a dialog was clicked.
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire
     * @param actionType Type of the action which was executed. Can be null.
     * @param model      The model which should be handled by the action. Can be null.
     */
    protected abstract void onDialogActionFire(Context context, View view, String actionType, M model);

    /**
     * Wrap {@link Action} so that it can show dialog before fired.
     *
     * @param dialogMessage The message for dialog to show on action fire
     * @param action        The action to fire if positive button on the dialog clicked
     * @param <M>           Type of model which can be handled
     * @return the action which can show dialog before fired.
     */
    public static <M> DialogAction<M> wrap(String dialogMessage, Action<M> action) {
        return new DialogActionWrapper<>(dialogMessage, action);
    }

    /**
     * The wrapper class for any type of action so that it can be used as {@link DialogAction}
     *
     * @param <M>
     */
    private static class DialogActionWrapper<M> extends DialogAction<M> implements OnActionFiredListener {
        private final Action<M> mAction;
        private final String mDialogMessage;

        /**
         * Wrap {@link Action} so that it can show dialog before fired.
         *
         * @param dialogMessage The message for dialog to show on action fire
         * @param action        The action to fire if positive button on the dialog clicked
         */
        public DialogActionWrapper(String dialogMessage, Action<M> action) {
            super();
            mAction = action;
            mDialogMessage = dialogMessage;
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).addActionFiredListener(this);
            }
        }

        @Override
        protected String getDialogMessage(Context context, String actionType, M model) {
            return mDialogMessage;
        }

        @Override
        protected void onDialogActionFire(Context context, View view, String actionType, M model) {
            if (mAction != null) mAction.onFireAction(context, view, actionType, model);
        }

        @Override
        public boolean isModelAccepted(Object model) {
            return mAction != null && mAction.isModelAccepted(model);
        }

        @Override
        public void onClickActionFired(String actionType, Object model) {
            notifyOnActionFired(actionType, model);
        }
    }
}
