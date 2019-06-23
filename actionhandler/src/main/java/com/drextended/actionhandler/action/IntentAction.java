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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.drextended.actionhandler.ActionArgs;
import com.drextended.actionhandler.util.AcceptCondition;

/**
 * Base action to fire some intent
 *
 * @param <M> type of model, which can be handled
 */
public abstract class IntentAction<M> extends BaseAction {
    /**
     * Type of intent:
     * {@link IntentType#START_ACTIVITY}, {@link IntentType#START_SERVICE}
     * {@link IntentType#SEND_BROADCAST}, {@link IntentType#SEND_LOCAL_BROADCAST}
     */
    protected final IntentType mIntentType;

    public enum IntentType {
        START_ACTIVITY,
        START_SERVICE,
        STOP_SERVICE,
        SEND_BROADCAST,
        SEND_LOCAL_BROADCAST
    }

    public IntentAction() {
        mIntentType = IntentType.START_ACTIVITY;
    }

    public IntentAction(@NonNull IntentType intentType) {
        mIntentType = intentType;
    }

    @Override
    public void onFireAction(@NonNull final ActionArgs args) {
        final Intent intent = getIntent(args);
        if (intent == null) {
            notifyOnActionDismiss(args, "No intent to fire!");
            return;
        }
        Context context = args.params.getViewOrAppContext();
        try {
            switch (mIntentType) {
                case START_ACTIVITY:
                    startActivity(context, intent, args);
                    break;
                case START_SERVICE:
                    startService(context, intent, args);
                    break;
                case STOP_SERVICE:
                    stopService(context, intent, args);
                    break;
                case SEND_BROADCAST:
                    sendBroadcast(context, intent, args);
                    break;
                case SEND_LOCAL_BROADCAST:
                    sendLocalBroadcast(context, intent, args);
                    break;
            }

            notifyOnActionFired(args);
        } catch (Exception e) {
            e.printStackTrace();
            onError(args, e);
        }
    }

    /**
     * Route exceptions from {@link #startActivity}, {@link #startService(Context, Intent, ActionArgs)} and
     * {@link #stopService(Context, Intent, ActionArgs)}
     * Generally can be {@link ActivityNotFoundException} or {@link SecurityException}
     *
     * @param args      The action params, which appointed to the view and actually actionType
     * @param throwable The exception, which was occurred while {@link #onFireAction(ActionArgs)}
     */
    protected void onError(@NonNull ActionArgs args, @Nullable Exception throwable) {
        notifyOnActionError(args, throwable);
    }

    /**
     * If {@link #mIntentType} was set as {@link IntentType#START_ACTIVITY}
     * and {@link #getIntent(ActionArgs)} return not null then this method called.
     *
     * @param context The Context, which generally get from view by {@link View#getContext()}
     * @param intent  The intent provided by {@link #getIntent(ActionArgs)}
     * @param args    The action params, which appointed to the view and actually actionType
     */
    protected void startActivity(
            @NonNull Context context,
            @NonNull Intent intent,
            @NonNull final ActionArgs args
    ) throws ActivityNotFoundException {
        context.startActivity(intent);
    }

    /**
     * Request that a given application service be started.
     * Override this method if you need specific behaviour.
     *
     * @param context The Context, which generally get from view by {@link View#getContext()}
     * @param intent  The intent to start service, provided by {@link #getIntent(ActionArgs)}
     * @param args    The action params, which appointed to the view and actually actionType
     */
    protected void startService(
            @NonNull Context context,
            @NonNull Intent intent,
            @NonNull final ActionArgs args
    ) throws SecurityException {
        context.startService(intent);
    }

    /**
     * Request that a given application service be stopped
     * Override this method if you need specific behaviour
     *
     * @param context The Context, which generally get from view by {@link View#getContext()}
     * @param intent  The intent to stop service, provided by {@link #getIntent(ActionArgs)}
     * @param args    The action params, which appointed to the view and actually actionType
     */
    protected void stopService(
            @NonNull Context context,
            @NonNull Intent intent,
            @NonNull final ActionArgs args
    ) throws SecurityException {
        context.stopService(intent);
    }

    /**
     * Broadcast the given intent to all interested BroadcastReceivers.
     * Override this method if you need specific behaviour.
     *
     * @param context The Context, which generally get from view by {@link View#getContext()}
     * @param intent  The Intent to broadcast, provided by {@link #getIntent(ActionArgs)}
     * @param args    The action params, which appointed to the view and actually actionType
     */
    protected void sendBroadcast(
            @NonNull Context context,
            @NonNull Intent intent,
            @NonNull final ActionArgs args
    ) {
        context.sendBroadcast(intent);
    }

    /**
     * Broadcast the given intent to all interested BroadcastReceivers.
     * Override this method if you need specific behaviour.
     *
     * @param context The Context, which generally get from view by {@link View#getContext()}
     * @param intent  The Intent to broadcast, provided by {@link #getIntent(ActionArgs)}
     * @param args    The action params, which appointed to the view and actually actionType
     */
    protected void sendLocalBroadcast(
            @NonNull Context context,
            @NonNull Intent intent,
            @NonNull final ActionArgs args
    ) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Provide an intent for use for start activity, start service or send broadcast.
     * You cat define how to use this intent by setting {@link #mIntentType} in {@link #IntentAction(IntentType)}
     *
     * @param args The action params, which appointed to the view and actually actionType
     * @return intent for use in role defined by {@link #mIntentType} in {@link #IntentAction(IntentType)}
     * if null action will not be fired.
     */
    @Nullable
    public abstract Intent getIntent(@NonNull final ActionArgs args);

    /**
     * Create simple intent action
     *
     * @param intent          The intent to call
     * @param intentType      Type of intent:
     *                        {@link IntentType#START_ACTIVITY}, {@link IntentType#START_SERVICE}
     *                        {@link IntentType#SEND_BROADCAST}, {@link IntentType#SEND_LOCAL_BROADCAST}
     * @param acceptCondition Condition to check whether model is accepted
     * @return The simple intent action
     */
    public static IntentAction from(Intent intent, IntentType intentType, AcceptCondition acceptCondition) {
        return new SimpleIntentAction(intent, intentType, acceptCondition);
    }

    /**
     * Create simple intent action to start activity intent
     *
     * @param intent The intent to start activity.
     *               Any model is accepted.
     * @return The simple intent action to start activity intent
     */
    public static IntentAction from(Intent intent) {
        return new SimpleIntentAction(intent, IntentType.START_ACTIVITY, null);
    }

    /**
     * Simple intent action
     */
    public static class SimpleIntentAction extends IntentAction {
        protected final Intent mIntent;
        protected final AcceptCondition mAcceptCondition;

        /**
         * @param intent          The intent to call
         * @param intentType      Type of intent:
         *                        {@link IntentType#START_ACTIVITY}, {@link IntentType#START_SERVICE}
         *                        {@link IntentType#SEND_BROADCAST}, {@link IntentType#SEND_LOCAL_BROADCAST}
         * @param acceptCondition Condition to check whether model is accepted
         */
        public SimpleIntentAction(Intent intent, IntentType intentType, @Nullable AcceptCondition acceptCondition) {
            super(intentType != null ? intentType : IntentType.START_ACTIVITY);
            mIntent = intent;
            mAcceptCondition = acceptCondition;
        }

        @Override
        public boolean isModelAccepted(Object model) {
            return mAcceptCondition == null || mAcceptCondition.isModelAccepted(model);
        }

        @Nullable
        @Override
        public Intent getIntent(@NonNull ActionArgs args) {
            return mIntent;
        }
    }
}
