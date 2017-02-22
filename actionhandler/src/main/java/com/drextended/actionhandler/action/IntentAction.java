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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.drextended.actionhandler.util.AcceptCondition;

/**
 * Base action to fire some intent
 *
 * @param <M> type of model, which can be handled
 */
public abstract class IntentAction<M> extends BaseAction<M> {
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
    public void onFireAction(Context context, @Nullable View view, String actionType, @Nullable M model) {
        final Intent intent = getIntent(view, context, actionType, model);
        if (intent == null) return;

        try {
            switch (mIntentType) {
                case START_ACTIVITY:
                    startActivity(context, view, intent);
                    break;
                case START_SERVICE:
                    startService(context, intent);
                    break;
                case STOP_SERVICE:
                    stopService(context, intent);
                    break;
                case SEND_BROADCAST:
                    sendBroadcast(context, intent);
                    break;
                case SEND_LOCAL_BROADCAST:
                    sendLocalBroadcast(context, intent);
                    break;
            }

            notifyOnActionFired(view, actionType, model);
        } catch (Exception e) {
            e.printStackTrace();
            onError(e, view, actionType, model);
        }
    }

    /**
     * Route exceptions from {@link #startActivity}, {@link #startService(Context, Intent)} and
     * {@link #stopService(Context, Intent)}
     * Generally can be {@link ActivityNotFoundException} or {@link SecurityException}
     *
     * @param throwable  The exception, which was occurred while {@link #onFireAction(Context, View, String, Object)}
     * @param view       The view, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire
     * @param actionType Type of the action which was executed. Can be null.
     * @param model      The model which should be handled by the action. Can be null.
     */
    protected void onError(Exception throwable, View view, String actionType, M model) {
        notifyOnActionError(throwable, view, actionType, model);
    }

    /**
     * If {@link #mIntentType} was set as {@link IntentType#START_ACTIVITY}
     * and {@link #getIntent(View, Context, String, Object)} return not null then this method called.
     * If build Sdk version below 16 then {@link Context#startActivity(Intent)} will be used,
     * else {@link Context#startActivity(Intent, Bundle)} will be used with {@code ActivityOptionsCompat}
     * as second argument, provided by {@link #prepareTransition(Context, View, Intent)} method.
     *
     * @param context The Context, which generally get from view by {@link View#getContext()}
     * @param view    The view, which can be used for prepare any visual effect (like animation),
     *                Generally it is that view which was clicked and initiated action to fire
     * @param intent  The intent provided by {@link #getIntent(View, Context, String, Object)}
     */
    protected void startActivity(Context context, View view, Intent intent) throws ActivityNotFoundException {
        if (Build.VERSION.SDK_INT >= 16) {
            ActivityOptionsCompat activityOptions = prepareTransition(context, view, intent);
            if (activityOptions != null) {
                context.startActivity(intent, activityOptions.toBundle());
                return;
            }
        }
        context.startActivity(intent);
    }

    /**
     * Request that a given application service be started.
     * Override this method if you need specific behaviour.
     *
     * @param context The Context, which generally get from view by {@link View#getContext()}
     * @param intent  The intent to start service, provided by {@link #getIntent(View, Context, String, Object)}
     */
    protected void startService(Context context, Intent intent) throws SecurityException {
        context.startService(intent);
    }

    /**
     * Request that a given application service be stopped
     * Override this method if you need specific behaviour
     *
     * @param context The Context, which generally get from view by {@link View#getContext()}
     * @param intent  The intent to stop service, provided by {@link #getIntent(View, Context, String, Object)}
     */
    protected void stopService(Context context, Intent intent) throws SecurityException {
        context.stopService(intent);
    }

    /**
     * Broadcast the given intent to all interested BroadcastReceivers.
     * Override this method if you need specific behaviour.
     *
     * @param context The Context, which generally get from view by {@link View#getContext()}
     * @param intent  The Intent to broadcast, provided by {@link #getIntent(View, Context, String, Object)}
     */
    protected void sendBroadcast(Context context, Intent intent) {
        context.sendBroadcast(intent);
    }

    /**
     * Broadcast the given intent to all interested BroadcastReceivers.
     * Override this method if you need specific behaviour.
     *
     * @param context The Context, which generally get from view by {@link View#getContext()}
     * @param intent  The Intent to broadcast, provided by {@link #getIntent(View, Context, String, Object)}
     */
    protected void sendLocalBroadcast(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Provide an intent for use for start activity, start service or send broadcast.
     * You cat define how to use this intent by setting {@link #mIntentType} in {@link #IntentAction(IntentType)}
     *
     * @param view       The view, which can be used for prepare any visual effect (like animation),
     *                   Generally it is that view which was clicked and initiated action to fire
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param actionType Type of the action which was executed. Can be null.
     * @param model      The model which should be handled by the action. Can be null.
     * @return intent for use in role defined by {@link #mIntentType} in {@link #IntentAction(IntentType)}
     * if null action will not be fired.
     */
    @Nullable
    public abstract Intent getIntent(@Nullable View view, Context context, String actionType, M model);

    /**
     * Additional options for how the Activity should be started.
     * May be null if there are no options.
     * Used only if {@link #mIntentType} set as {@link IntentType#START_ACTIVITY} and build Sdk version greater 15.
     * Intended for settings activity transition or shared element transition
     *
     * @param context The Context, which generally get from view by {@link View#getContext()}
     * @param view    The view, which can be used for prepare shared element transition.
     *                Generally it is that view which was clicked and initiated action to fire
     * @param intent  The intent, provided by {@link #getIntent(View, Context, String, Object)}
     * @return options for how the Activity should be started. Used to pass in
     * {@link Context#startActivity(Intent, Bundle)} as second argument.
     */
    protected ActivityOptionsCompat prepareTransition(Context context, View view, Intent intent) {
        return null;
    }

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
        public Intent getIntent(@Nullable View view, Context context, String actionType, Object model) {
            return mIntent;
        }
    }
}
