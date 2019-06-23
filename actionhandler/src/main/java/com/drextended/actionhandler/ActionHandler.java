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

package com.drextended.actionhandler;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drextended.actionhandler.action.Action;
import com.drextended.actionhandler.action.ActionFactory;
import com.drextended.actionhandler.action.BaseAction;
import com.drextended.actionhandler.action.Cancelable;
import com.drextended.actionhandler.action.SingleActionFactory;
import com.drextended.actionhandler.action.SingleActionFactoryAdapter;
import com.drextended.actionhandler.listener.ActionCallback;
import com.drextended.actionhandler.listener.ActionClickListener;
import com.drextended.actionhandler.listener.ActionFireInterceptor;
import com.drextended.actionhandler.listener.ActionInterceptor;
import com.drextended.actionhandler.listener.OnActionDismissListener;
import com.drextended.actionhandler.listener.OnActionErrorListener;
import com.drextended.actionhandler.listener.OnActionFiredListener;
import com.drextended.actionhandler.util.DebounceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Use ActionHandler to manage action and bind them to view
 */
@SuppressWarnings("WeakerAccess")
public class ActionHandler implements ActionClickListener, OnActionFiredListener, OnActionErrorListener, OnActionDismissListener, ActionFireInterceptor {

    public static final String TAG = "ActionHandler";

    // Actions which was added to the handler
    protected final List<ActionPair> mActions = new ArrayList<>();

    // Factory for build actions on demand
    protected ActionFactory mActionFactory;

    // Callbacks to be invoked when an action is executed successfully
    protected Set<OnActionFiredListener> mOnActionFiredListeners;

    // Callbacks to be invoked when an action is executed with error
    protected Set<OnActionErrorListener> mOnActionErrorListeners;

    // Callbacks to be invoked when an action is executed but dismissed
    protected Set<OnActionDismissListener> mOnActionDismissListeners;

    // Callback to be invoked right before specific action will be fired.
    // Can intercept an action to prevent it to be fired
    protected Set<ActionFireInterceptor> mActionFireInterceptors;

    // Callback to be invoked after a view with an action is clicked and before action handling started.
    // Can intercept an action to prevent it to be fired
    private Set<ActionInterceptor> mActionInterceptors;

    // Debounce milliseconds for specific action types (<action type, milliseconds>)
    private Map<String, Long> mActionDebounceTime;

    private long mDefaultDebounceTime = 0;

    private DebounceHelper mDebounceHelper;

    /**
     * @param actions list of actions to handle by this handler
     */
    protected ActionHandler(List<ActionPair> actions) {
        addActionsInternal(actions);
    }

    /**
     * Set factory for crate actions lazy.
     *
     * @param actionFactory the factory
     */
    public void setActionFactory(ActionFactory actionFactory) {
        mActionFactory = actionFactory;
    }

    /**
     * Null-safe equivalent of {@code a.equals(b)}.
     *
     * @param a the first object
     * @param b the second object
     * @return true if a equals b
     */
    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    /**
     * Set new callback to be invoked when an action is executed successfully
     * Note: It is called only for BaseActions.
     * You should call {@link BaseAction#notifyOnActionFired(ActionArgs)} to invoke this callback.
     *
     * @param actionFiredListener new callback to be invoked when an action is executed successfully
     * @deprecated Use {@link #addActionFiredListener(OnActionFiredListener)} instead
     */
    @Deprecated
    public void setOnActionFiredListener(OnActionFiredListener actionFiredListener) {
        if (actionFiredListener != null) {
            removeAllActionFiredListeners();
            addActionFiredListener(actionFiredListener);
        } else {
            removeAllActionFiredListeners();
        }

    }

    /**
     * Add new callback to be invoked when an action is executed successfully
     * Note: It is called only for BaseActions.
     * You should call {@link BaseAction#notifyOnActionFired(ActionArgs)} to invoke this callback.
     *
     * @param actionFiredListener new callback to be invoked when an action is executed successfully
     */
    public void addActionFiredListener(OnActionFiredListener actionFiredListener) {
        if (mOnActionFiredListeners == null) {
            mOnActionFiredListeners = new HashSet<>(1);
        }
        mOnActionFiredListeners.add(actionFiredListener);
    }

    /**
     * Remove the callback for fire event
     *
     * @param actionFiredListener callback to remove
     */
    public void removeActionFiredListener(OnActionFiredListener actionFiredListener) {
        if (mOnActionFiredListeners != null) {
            mOnActionFiredListeners.remove(actionFiredListener);
        }
    }

    /**
     * Remove all callbacks for fire event
     */
    public void removeAllActionFiredListeners() {
        if (mOnActionFiredListeners != null) {
            mOnActionFiredListeners.clear();
        }
    }

    /**
     * Add new callback to be invoked when an action is executed with error
     * You should call {@link BaseAction#notifyOnActionError(ActionArgs, Throwable)} to invoke this callback.
     *
     * @param actionErrorListener new callback to be invoked when an action is executed with error
     */
    public void addActionErrorListener(OnActionErrorListener actionErrorListener) {
        if (mOnActionErrorListeners == null) {
            mOnActionErrorListeners = new HashSet<>(1);
        }
        mOnActionErrorListeners.add(actionErrorListener);
    }

    /**
     * Remove the callback for error event
     *
     * @param actionErrorListener callback to remove
     */
    public void removeActionErrorListener(OnActionErrorListener actionErrorListener) {
        if (mOnActionErrorListeners != null) {
            mOnActionErrorListeners.remove(actionErrorListener);
        }
    }

    /**
     * Remove all callbacks for error event
     */
    public void removeAllActionErrorListeners() {
        if (mOnActionErrorListeners != null) {
            mOnActionErrorListeners.clear();
        }
    }

    /**
     * Add new callback to be invoked when an action is executed but dismissed
     * You should call {@link BaseAction#notifyOnActionDismiss(ActionArgs, String)} to invoke this callback.
     *
     * @param listener new callback to be invoked when an action is executed with error
     */
    public void addActionDismissListener(OnActionDismissListener listener) {
        if (mOnActionDismissListeners == null) {
            mOnActionDismissListeners = new HashSet<>(1);
        }
        mOnActionDismissListeners.add(listener);
    }

    /**
     * Remove the callback for dismiss event
     *
     * @param listener callback to remove
     */
    public void removeActionDismissListener(OnActionDismissListener listener) {
        if (mOnActionDismissListeners != null) {
            mOnActionDismissListeners.remove(listener);
        }
    }

    /**
     * Remove all callbacks for dismiss event
     */
    public void removeAllActionDismissListeners() {
        if (mOnActionDismissListeners != null) {
            mOnActionDismissListeners.clear();
        }
    }

    /**
     * Set new callback to be invoked after a view with an action is clicked and before action handling started.
     * Can intercept an action to prevent it to be fired
     *
     * @param actionInterceptor The interceptor, which can prevent actions to be fired
     * @deprecated use {@link #addActionInterceptor(ActionInterceptor)} instead
     */
    @Deprecated
    public void setActionInterceptor(ActionInterceptor actionInterceptor) {
        if (actionInterceptor != null) {
            removeAllActionInterceptors();
            addActionInterceptor(actionInterceptor);
        } else {
            removeAllActionInterceptors();
        }
    }

    /**
     * Add new callback to be invoked after a view with an action is clicked and before action type handling started.
     * Can intercept an action type to prevent it to be handled
     *
     * @param actionInterceptor The interceptor, which can prevent action type to be handled
     */
    public void addActionInterceptor(ActionInterceptor actionInterceptor) {
        if (mActionInterceptors == null) {
            mActionInterceptors = new HashSet<>(1);
        }
        mActionInterceptors.add(actionInterceptor);
    }

    /**
     * Remove action interceptor
     *
     * @param actionInterceptor The interceptor to remove
     */
    public void removeActionInterceptor(ActionInterceptor actionInterceptor) {
        if (mActionInterceptors != null) {
            mActionInterceptors.remove(actionInterceptor);
        }
    }

    /**
     * Remove all action interceptors
     */
    public void removeAllActionInterceptors() {
        if (mActionInterceptors != null) {
            mActionInterceptors.clear();
        }
    }

    /**
     * Add new callback to be invoked right before specific action will be fired..
     * Can intercept an action to prevent it to be fired
     *
     * @param actionFireInterceptor The interceptor, which can prevent action to be fired
     */
    public void addActionFireInterceptor(ActionFireInterceptor actionFireInterceptor) {
        if (mActionFireInterceptors == null) {
            mActionFireInterceptors = new HashSet<>(1);
        }
        mActionFireInterceptors.add(actionFireInterceptor);
    }

    /**
     * Remove action fire interceptor
     *
     * @param actionFireInterceptor The interceptor to remove
     */
    public void removeActionFireInterceptor(ActionFireInterceptor actionFireInterceptor) {
        if (mActionFireInterceptors != null) {
            mActionFireInterceptors.remove(actionFireInterceptor);
        }
    }

    /**
     * Remove all action fire interceptors
     */
    public void removeAllActionFireInterceptors() {
        if (mActionFireInterceptors != null) {
            mActionFireInterceptors.clear();
        }
    }

    /**
     * Add new action callback.
     * One method for adding all listeners and interceptors.
     *
     * @param actionCallback The action callback
     */
    public void addCallback(ActionCallback actionCallback) {
        addActionInterceptor(actionCallback);
        addActionFireInterceptor(actionCallback);
        addActionFiredListener(actionCallback);
        addActionDismissListener(actionCallback);
        addActionErrorListener(actionCallback);
    }

    /**
     * Remove action callback
     *
     * @param actionCallback The action callback
     */
    public void removeCallback(ActionCallback actionCallback) {
        removeActionInterceptor(actionCallback);
        removeActionFireInterceptor(actionCallback);
        removeActionFiredListener(actionCallback);
        removeActionDismissListener(actionCallback);
        removeActionErrorListener(actionCallback);
    }

    /**
     * Remove all callbacks for action intercept, fire, error and dismiss events
     */
    public void removeAllActionListeners() {
        removeAllActionFiredListeners();
        removeAllActionDismissListeners();
        removeAllActionInterceptors();
        removeAllActionFireInterceptors();
        removeAllActionErrorListeners();
    }

    @Override
    public void onActionFired(@NonNull ActionArgs args, @Nullable Object result) {
        if (mOnActionFiredListeners != null) {
            for (final OnActionFiredListener listener : mOnActionFiredListeners) {
                listener.onActionFired(args, result);
            }
        }
    }

    @Override
    public void onActionError(@NonNull ActionArgs args, @Nullable Throwable throwable) {
        if (mOnActionErrorListeners != null) {
            for (final OnActionErrorListener listener : mOnActionErrorListeners) {
                listener.onActionError(args, throwable);
            }
        }
    }

    @Override
    public void onActionDismiss(@NonNull ActionArgs args, @Nullable String reason) {
        if (mOnActionDismissListeners != null) {
            for (final OnActionDismissListener listener : mOnActionDismissListeners) {
                listener.onActionDismiss(args, reason);
            }
        }
    }

    @Override
    public boolean onInterceptActionFire(@NonNull ActionParams actionParams, @Nullable String actionType, @NonNull Action action) {
        return interceptActionFire(actionParams, actionType, action);
    }

    /**
     * Check if there is at least one action that can try to handle {@code actionType}
     *
     * @param actionType The action type to check
     * @return true if there is at least one action that can try to handle {@code actionType},
     * false otherwise.
     */
    public boolean canHandle(final String actionType) {
        for (ActionPair actionPair : mActions) {
            if (equals(actionType, actionPair.actionType)) return true;
        }
        return false;
    }

    /**
     * Check if there is at least one action that can handle {@code actionType} with {@code model}
     *
     * @param actionType The action type to check
     * @param model      The model to check
     * @return true if there is at least one action that can handle {@code actionType} with {@code model},
     * false otherwise.
     */
    public boolean canHandle(@NonNull final String actionType, @Nullable Object model) {
        for (ActionPair actionPair : mActions) {
            if (equals(actionType, actionPair.actionType) &&
                    actionPair.action.isModelAccepted(model)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called when a view with an action is clicked.
     *
     * @param view       The view that was clicked.
     * @param actionType The action type, which appointed to the view
     * @param model      The model, which  appointed to the view and should be handled
     * @param actionTag  The tag, which can be used to distinct click source or etc.
     */
    @Override
    public void onActionClick(
            @NonNull View view,
            @Nullable String actionType,
            @Nullable Object model,
            @Nullable Object actionTag
    ) {
        if (actionType == null) {
            Log.w(TAG, "onActionClick fired, but actionType is null: action dropped!");
            return;
        }
        fireAction(new ActionParams(
                view.getContext(),
                view,
                actionType,
                model,
                actionTag
        ));
    }

    /**
     * Call for initiate actions to fire.
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view that was clicked.
     * @param actionType The action type, which appointed to the view
     * @param model      The model, which  appointed to the view and should be handled
     */
    @Deprecated
    public void fireAction(
            @NonNull Context context,
            @Nullable View view,
            @NonNull String actionType,
            @Nullable Object model
    ) {
        //noinspection ConstantConditions
        if (actionType == null) {
            Log.w(TAG, "fireAction fired, but actionType is null: action dropped!");
            return;
        }
        fireAction(new ActionParams(
                context,
                view,
                actionType,
                model,
                null
        ));

    }

    /**
     * Call for initiate actions to fire.
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view that was clicked.
     * @param actionType The action type, which appointed to the view
     * @param model      The model, which  appointed to the view and should be handled
     * @param actionTag  The tag, which can be used to distinct click source or etc.
     */
    public void fireAction(
            @NonNull Context context,
            @Nullable View view,
            @NonNull String actionType,
            @Nullable Object model,
            @Nullable Object actionTag
    ) {
        //noinspection ConstantConditions
        if (actionType == null) {
            Log.w(TAG, "onActionClick fired, but actionType is null: action dropped!");
            return;
        }
        fireAction(new ActionParams(
                context,
                view,
                actionType,
                model,
                actionTag
        ));
    }

    public void fireAction(ActionParams actionParams) {
        if (!checkDebounceTimeElapsed(actionParams.actionType)) {
            Log.d("ActionHandler", "Debounce time not elapsed. Action intercepted!");
            return;
        }

        if (interceptAction(actionParams)) return;

        List<ActionPair> actionPairs = getActionsForActionType(actionParams.actionType);

        for (ActionPair actionPair : actionPairs) {
            final Action action = actionPair.action;
            if (action.isModelAccepted(actionParams.model)) {
                if (interceptActionFire(actionParams, actionPair.actionType, action)) continue;
                action.onFireAction(new ActionArgs(actionParams, actionPair.actionType));
            }
        }
    }

    private List<ActionPair> getActionsForActionType(String actionType) {
        List<ActionPair> foundActions = new ArrayList<>(1);
        boolean hasActionsForActionType = false;
        for (ActionPair actionPair : mActions) {
            if (actionPair.actionType == null || actionPair.actionType.equals(actionType)) {
                foundActions.add(actionPair);
                hasActionsForActionType |= actionPair.actionType != null;
            }
        }
        if (!hasActionsForActionType && mActionFactory != null && actionType != null) {
            Action[] actions = mActionFactory.provideActions(actionType);
            if (actions != null) {
                for (Action action : actions) {
                    ActionPair actionPair = new ActionPair(actionType, action);
                    foundActions.add(actionPair);
                }
                addActionsInternal(foundActions);
            }
        }
        return foundActions;
    }

    private synchronized void addActionsInternal(List<ActionPair> actions) {
        mActions.addAll(actions);
        for (ActionPair actionPair : actions) {
            if (actionPair.action instanceof BaseAction) {
                BaseAction baseAction = ((BaseAction) actionPair.action);
                baseAction.addActionFiredListener(this);
                baseAction.addActionErrorListener(this);
                baseAction.addActionDismissListener(this);
                baseAction.addActionFireInterceptor(this);
            }
        }
    }

    private boolean checkDebounceTimeElapsed(final String actionType) {
        if (mDefaultDebounceTime > 0 || mActionDebounceTime != null) {
            if (mDebounceHelper == null) {
                synchronized (this) {
                    if (mDebounceHelper == null) {
                        mDebounceHelper = new DebounceHelper();
                    }
                }
            }
            Long debounceMillis = mActionDebounceTime == null ? null : mActionDebounceTime.get(actionType);
            if (debounceMillis == null) debounceMillis = mDefaultDebounceTime;
            return debounceMillis <= 0 || mDebounceHelper.checkTimeAndResetIfElapsed(actionType, debounceMillis);
        }
        return true;
    }

    private boolean interceptAction(@NonNull ActionParams actionParams) {
        if (mActionInterceptors != null) {
            for (ActionInterceptor interceptor : mActionInterceptors) {
                if (interceptor.onInterceptAction(actionParams)) return true;
            }
        }
        return false;
    }

    private boolean interceptActionFire(
            @NonNull ActionParams actionParams,
            @Nullable String actionType,
            @NonNull Action action
    ) {
        if (mActionFireInterceptors != null) {
            for (ActionFireInterceptor interceptor : mActionFireInterceptors) {
                if (interceptor.onInterceptActionFire(actionParams, actionType, action)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Call this method to force actions to cancel.
     * Usually, you may need to call this on Activity destroy to free resources which
     * can lead to memory leak and stop pending transaction or async calls.
     */
    public final void cancelAll() {
        for (ActionPair actionPair : mActions) {
            if (actionPair.action instanceof Cancelable) {
                ((Cancelable) actionPair.action).cancel();
            }
        }
    }

    /**
     * The Builder for configure action handler
     */
    @SuppressWarnings("SameParameterValue")
    public static final class Builder {
        private List<ActionPair> mActions;
        private ActionFactory mActionFactory;
        private Set<OnActionFiredListener> mActionFiredListeners;
        private Set<OnActionErrorListener> mActionErrorListeners;
        private Set<OnActionDismissListener> mActionDismissListeners;
        private Set<ActionInterceptor> mActionInterceptors;
        private Set<ActionFireInterceptor> mActionFireInterceptors;
        private Map<String, Long> mActionDebounceTime;
        private long mDefaultDebounceTime = 0;

        public Builder() {
            mActions = new ArrayList<>();
        }

        /**
         * Set ActionFactory for lazy instantiating actions
         *
         * @param factory the factory
         * @return the builder
         */
        public Builder withActionFactory(ActionFactory factory) {
            mActionFactory = factory;
            return this;
        }

        /**
         * Set ActionFactory for lazy instantiating actions
         * Simplified version of {@link #withActionFactory(ActionFactory)}
         * that returns single action for given actionType instead of array of actions
         *
         * @param factory the factory
         * @return the builder
         */
        public Builder withFactory(SingleActionFactory factory) {
            mActionFactory = new SingleActionFactoryAdapter(factory);
            return this;
        }

        /**
         * Add an action to the action handler
         *
         * @param actionType The type of action
         * @param action     The action
         * @return the builder
         */
        public Builder addAction(String actionType, Action action) {
            mActions.add(new ActionPair(actionType, action));
            return this;
        }

        /**
         * Set new callback to be invoked when an action is executed successfully
         * Note: It is called only for BaseActions.
         * You should call {@link BaseAction#notifyOnActionFired(ActionArgs)} to invoke this callback.
         *
         * @param actionFiredListener new callback to be invoked when an action is executed successfully
         * @return the builder
         * @deprecated use {@link #addActionFiredListener(OnActionFiredListener)} instead
         */
        @Deprecated
        public Builder setActionFiredListener(final OnActionFiredListener actionFiredListener) {
            addActionFiredListener(actionFiredListener);
            return this;
        }

        /**
         * Add new callback to be invoked when an action is executed successfully
         * Note: It is called only for BaseActions.
         * You should call {@link BaseAction#notifyOnActionFired(ActionArgs)} to invoke this callback.
         *
         * @param actionFiredListener new callback to be invoked when an action is executed successfully
         * @return the builder
         */
        public Builder addActionFiredListener(final OnActionFiredListener actionFiredListener) {
            if (mActionFiredListeners == null) {
                mActionFiredListeners = new HashSet<>(1);
            }
            mActionFiredListeners.add(actionFiredListener);
            return this;
        }

        /**
         * Add new callback to be invoked when an action is executed with error
         * Note: It is called only for BaseActions.
         * You should call {@link BaseAction#notifyOnActionError(ActionArgs, Throwable)} to invoke this callback.
         *
         * @param actionErrorListener new callback to be invoked when an action is executed successfully
         * @return the builder
         */
        public Builder addActionErrorListener(final OnActionErrorListener actionErrorListener) {
            if (mActionErrorListeners == null) {
                mActionErrorListeners = new HashSet<>(1);
            }
            mActionErrorListeners.add(actionErrorListener);
            return this;
        }

        /**
         * Add new callback to be invoked when an action is executed but dismissed
         * Note: It is called only for BaseActions.
         * You should call {@link BaseAction#notifyOnActionDismiss(ActionArgs, String)} to invoke this callback.
         *
         * @param listener new callback to be invoked when an action was dismissed
         * @return the builder
         */
        public Builder addActionDismissListener(final OnActionDismissListener listener) {
            if (mActionDismissListeners == null) {
                mActionDismissListeners = new HashSet<>(1);
            }
            mActionDismissListeners.add(listener);
            return this;
        }

        /**
         * Set callback to be invoked after a view with an action is clicked and before action handling started.
         * Can intercept an action to prevent it to be fired
         *
         * @param actionInterceptor The interceptor, which can prevent actions to be fired
         * @return the builder
         * @deprecated use {@link #addActionInterceptor(ActionInterceptor)} instead
         */
        public Builder setActionInterceptor(ActionInterceptor actionInterceptor) {
            addActionInterceptor(actionInterceptor);
            return this;
        }

        /**
         * Add callback to be invoked after a view with an action is clicked and before action type handling started.
         * Can intercept an action type to prevent it to be handled
         *
         * @param actionInterceptor The interceptor, which can prevent actions to be fired
         * @return the builder
         */
        public Builder addActionInterceptor(ActionInterceptor actionInterceptor) {
            if (mActionInterceptors == null) {
                mActionInterceptors = new HashSet<>(1);
            }
            mActionInterceptors.add(actionInterceptor);
            return this;
        }

        /**
         * Add callback to be invoked before specific action will be fired.
         * Can intercept an action to prevent it to be fired
         *
         * @param actionFireInterceptor The interceptor, which can prevent actions to be fired
         * @return the builder
         */
        public Builder addActionFireInterceptor(ActionFireInterceptor actionFireInterceptor) {
            if (mActionFireInterceptors == null) {
                mActionFireInterceptors = new HashSet<>(1);
            }
            mActionFireInterceptors.add(actionFireInterceptor);
            return this;
        }

        /**
         * Add new action callback.
         * One method for adding all listeners and interceptors.
         *
         * @param actionCallback The action callback
         * @return the builder
         */
        public Builder addCallback(ActionCallback actionCallback) {
            addActionInterceptor(actionCallback);
            addActionFireInterceptor(actionCallback);
            addActionFiredListener(actionCallback);
            addActionDismissListener(actionCallback);
            addActionErrorListener(actionCallback);
            return this;
        }

        /**
         * Set default debounce time for distinct click actions
         *
         * @param debounceTimeMillis the debounce time in milliseconds
         * @return the builder
         */
        public Builder setDefaultDebounce(long debounceTimeMillis) {
            this.mDefaultDebounceTime = debounceTimeMillis > 0 ? debounceTimeMillis : 0;
            return this;
        }

        /**
         * Set debounce time for defined action types. If set for specific action, overrides default debounce time.
         *
         * @param debounceTimeMillis the debounce time in milliseconds
         * @param actionTypes        the action types to apply debounce
         * @return the builder
         */
        public Builder setDebounce(long debounceTimeMillis, String... actionTypes) {
            if (actionTypes != null && actionTypes.length > 0) {
                if (this.mActionDebounceTime == null) {
                    this.mActionDebounceTime = new HashMap<>();
                }
                for (final String actionType : actionTypes) {
                    this.mActionDebounceTime.put(actionType, debounceTimeMillis);
                }
            }
            return this;
        }

        public ActionHandler build() {
            final ActionHandler actionHandler = new ActionHandler(mActions);
            actionHandler.mDefaultDebounceTime = this.mDefaultDebounceTime;
            actionHandler.mActionDebounceTime = this.mActionDebounceTime;
            if (mActionFactory != null) {
                actionHandler.mActionFactory = mActionFactory;
            }
            if (mActionFiredListeners != null) {
                actionHandler.mOnActionFiredListeners = mActionFiredListeners;
            }
            if (mActionErrorListeners != null) {
                actionHandler.mOnActionErrorListeners = mActionErrorListeners;
            }
            if (mActionDismissListeners != null) {
                actionHandler.mOnActionDismissListeners = mActionDismissListeners;
            }
            if (mActionInterceptors != null && mActionInterceptors.size() > 0) {
                actionHandler.mActionInterceptors = mActionInterceptors;
            }
            if (mActionFireInterceptors != null && mActionFireInterceptors.size() > 0) {
                actionHandler.mActionFireInterceptors = mActionFireInterceptors;
            }
            return actionHandler;
        }
    }

}
