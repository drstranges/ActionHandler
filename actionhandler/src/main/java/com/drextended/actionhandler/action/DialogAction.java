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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.drextended.actionhandler.ActionArgs;
import com.drextended.actionhandler.ActionParams;
import com.drextended.actionhandler.listener.ActionFireInterceptor;
import com.drextended.actionhandler.listener.OnActionDismissListener;
import com.drextended.actionhandler.listener.OnActionErrorListener;
import com.drextended.actionhandler.listener.OnActionFiredListener;

/**
 * Can be used for make an action which show simple dialog before it has been fired.
 * You can extend this class for make custom action on just wrap another action
 * using {@link #wrap(String, Action)}
 *
 * @param <M> type of model to handle
 */
@SuppressWarnings("SameParameterValue")
public abstract class DialogAction<M> extends BaseAction {

    @Override
    public void onFireAction(@NonNull ActionArgs args) {
        final Dialog dialog = createDialog(args);
        dialog.show();
    }

    /**
     * Creates dialog for showing before call {@link #onDialogActionFire}.
     * By default contains:
     * - title, obtained by {@link #getDialogTitle(ActionParams)},
     * - message, obtained by {@link #getDialogMessage(ActionParams)},
     * - negative button with label, obtained by {@link #getNegativeButtonTitleResId()},
     * which refuse action by click,
     * - positive button with label, obtained by {@link #getPositiveButtonTitleResId()},
     * which call {@link #onDialogActionFire} by click
     *
     * @param args The action params, which appointed to the view and actually actionType
     * @return the dialog to show before call {@link #onDialogActionFire}.
     */
    protected Dialog createDialog(@NonNull final ActionArgs args) {
        final String title = getDialogTitle(args.params);
        final AlertDialog.Builder builder = new AlertDialog.Builder(args.params.getViewOrAppContext());
        if (title != null) builder.setTitle(title);
        builder.setMessage(getDialogMessage(args.params))
                .setNegativeButton(getNegativeButtonTitleResId(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notifyOnActionDismiss(args, "Dialog cancelled");
                    }
                })
                .setPositiveButton(getPositiveButtonTitleResId(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onDialogActionFire(args);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        notifyOnActionDismiss(args, "Dialog cancelled");
                    }
                });
        return builder.create();
    }

    /**
     * Provides title for dialog
     *
     * @param params The action params, which appointed to the view
     * @return title for dialog
     */
    protected String getDialogTitle(@NonNull ActionParams params) {
        return null;
    }

    /**
     * Provides message for dialog
     *
     * @param params The action params, which appointed to the view
     * @return message for dialog
     */
    protected abstract String getDialogMessage(@NonNull ActionParams params);

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
     * @param args The action params, which appointed to the view and actually actionType
     */
    protected void onDialogActionFire(@NonNull final ActionArgs args) {
    }

    /**
     * Wrap {@link Action} so that it can show dialog before fired.
     *
     * @param dialogMessage The message for dialog to show on action fire
     * @param action        The action to fire if positive button on the dialog clicked
     * @param <M>           Type of model which can be handled
     * @return the action which can show dialog before fired.
     */
    public static <M> DialogAction<M> wrap(String dialogMessage, Action action) {
        return new DialogActionWrapper<>(dialogMessage, action);
    }

    /**
     * The wrapper class for any type of action so that it can be used as {@link DialogAction}
     *
     * @param <M>
     */
    private static class DialogActionWrapper<M> extends DialogAction<M> {
        private final Action mAction;
        private final String mDialogMessage;

        /**
         * Wrap {@link Action} so that it can show dialog before fired.
         *
         * @param dialogMessage The message for dialog to show on action fire
         * @param action        The action to fire if positive button on the dialog clicked
         */
        public DialogActionWrapper(String dialogMessage, Action action) {
            super();
            mAction = action;
            mDialogMessage = dialogMessage;
        }

        @Override
        protected String getDialogMessage(@NonNull ActionParams params) {
            return mDialogMessage;
        }

        @Override
        protected void onDialogActionFire(@NonNull ActionArgs args) {
            if (mAction != null) mAction.onFireAction(args);
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
