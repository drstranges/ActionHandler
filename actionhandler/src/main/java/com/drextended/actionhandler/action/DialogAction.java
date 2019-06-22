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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.view.View;

import com.drextended.actionhandler.listener.ActionFireInterceptor;
import com.drextended.actionhandler.listener.OnActionDismissListener;
import com.drextended.actionhandler.listener.OnActionErrorListener;
import com.drextended.actionhandler.listener.OnActionFiredListener;

/**
 * Can be used for make an action which show simple dialog before it has been fired.
 * You can extend this class for make custom action on just wrap another action
 * using {@link #wrap(String, Action)}
 *
 * @param <M>    type of model to handle
 */
@SuppressWarnings("SameParameterValue")
public abstract class DialogAction<M> extends BaseAction<M> {

    @Override
    public void onFireAction(final Context context, @Nullable final View view, final String actionType, @Nullable final M model) {
        final Dialog dialog = createDialog(context, view, actionType, model);
        dialog.show();
    }

    /**
     * Creates dialog for showing before call {@link #onDialogActionFire}.
     * By default contains:
     * - title, obtained by {@link #getDialogTitle(Context, String, Object)},
     * - message, obtained by {@link #getDialogMessage(Context, String, Object)},
     * - negative button with label, obtained by {@link #getNegativeButtonTitleResId()},
     *   which refuse action by click,
     * - positive button with label, obtained by {@link #getPositiveButtonTitleResId()},
     *   which call {@link #onDialogActionFire} by click
     *
     * @param context       The Context, which generally get from view by {@link View#getContext()}
     * @param view          The view, which can be used for prepare any visual effect (like animation),
     *                      Generally it is that view which was clicked and initiated action to fire
     * @param actionType    Type of the action which was executed. Can be null.
     * @param model         The model which should be handled by the action. Can be null.
     * @return the dialog to show before call {@link #onDialogActionFire}.
     */
    protected Dialog createDialog(final Context context, @Nullable final View view, final String actionType, @Nullable final M model) {
        final String title = getDialogTitle(context, actionType, model);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != null) builder.setTitle(title);
        builder.setMessage(getDialogMessage(context, actionType, model))
                .setNegativeButton(getNegativeButtonTitleResId(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notifyOnActionDismiss("Dialog cancelled", view, actionType, model);
                    }
                })
                .setPositiveButton(getPositiveButtonTitleResId(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onDialogActionFire(context, view, actionType, model, null);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        notifyOnActionDismiss("Dialog cancelled", view, actionType, model);
                    }
                });
        return builder.create();
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
    protected void onDialogActionFire(Context context, View view, String actionType, M model){
        onDialogActionFire(context, view, actionType, model, null);
    }

    /**
     * Executes the action. Called if positive button on a dialog was clicked.
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire
     * @param actionType Type of the action which was executed. Can be null.
     * @param model      The model which should be handled by the action. Can be null.
     * @param payload    The payload, for example the parameter for the request
     */
    protected void onDialogActionFire(Context context, View view, String actionType, M model, @Nullable Object payload){
    }

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
    private static class DialogActionWrapper<M> extends DialogAction<M> {
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
        }

        @Override
        protected String getDialogMessage(Context context, String actionType, M model) {
            return mDialogMessage;
        }

        @Override
        protected void onDialogActionFire(Context context, View view, String actionType, M model, Object payload) {
            if (mAction != null) mAction.onFireAction(context, view, actionType, model);
        }

        @Override
        public boolean isModelAccepted(Object model) {
            return mAction != null && mAction.isModelAccepted(model);
        }

        @Override
        public void addActionFiredListener(OnActionFiredListener listener) {
            super.addActionFiredListener(listener);
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).addActionFiredListener(listener);
            }
        }

        @Override
        public void addActionErrorListener(OnActionErrorListener listener) {
            super.addActionErrorListener(listener);
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).addActionErrorListener(listener);
            }
        }

        @Override
        public void addActionDismissListener(OnActionDismissListener listener) {
            super.addActionDismissListener(listener);
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).addActionDismissListener(listener);
            }
        }

        @Override
        public void addActionFireInterceptor(ActionFireInterceptor interceptor) {
            super.addActionFireInterceptor(interceptor);
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).addActionFireInterceptor(interceptor);
            }
        }

        @Override
        public void removeActionFireListener(OnActionFiredListener listener) {
            super.removeActionFireListener(listener);
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).removeActionFireListener(listener);
            }
        }

        @Override
        public void removeAllActionFireListeners() {
            super.removeAllActionFireListeners();
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).removeAllActionFireListeners();
            }
        }

        @Override
        public void removeActionErrorListener(OnActionErrorListener listener) {
            super.removeActionErrorListener(listener);
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).removeActionErrorListener(listener);
            }
        }

        @Override
        public void removeAllActionErrorListeners() {
            super.removeAllActionErrorListeners();
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).removeAllActionFireListeners();
            }
        }

        @Override
        public void removeActionDismissListener(OnActionDismissListener listener) {
            super.removeActionDismissListener(listener);
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).removeActionDismissListener(listener);
            }
        }

        @Override
        public void removeAllActionDismissListeners() {
            super.removeAllActionDismissListeners();
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).removeAllActionDismissListeners();
            }
        }

        @Override
        public void removeActionFireInterceptor(ActionFireInterceptor interceptor) {
            super.removeActionFireInterceptor(interceptor);
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).removeActionFireInterceptor(interceptor);
            }
        }

        @Override
        public void removeAllActionFireInterceptors() {
            super.removeAllActionFireInterceptors();
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).removeAllActionFireListeners();
            }
        }

        @Override
        public void removeAllActionListeners() {
            super.removeAllActionListeners();
            if (mAction instanceof BaseAction) {
                ((BaseAction) mAction).removeAllActionListeners();
            }
        }
    }
}
