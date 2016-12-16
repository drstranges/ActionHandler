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
import android.view.View;

import com.drextended.actionhandler.action.Action;
import com.drextended.actionhandler.action.BaseAction;
import com.drextended.actionhandler.action.Cancelable;
import com.drextended.actionhandler.listener.ActionClickListener;
import com.drextended.actionhandler.listener.ActionInterceptor;
import com.drextended.actionhandler.listener.OnActionErrorListener;
import com.drextended.actionhandler.listener.OnActionFiredListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Use ActionHandler to manage action and bind them to view
 */
public class ActionHandler implements ActionClickListener, OnActionFiredListener, OnActionErrorListener {

    // Actions which was added to the handler
    protected final List<ActionPair> mActions;

    // Callbacks to be invoked when an action is executed successfully
    protected Set<OnActionFiredListener> mOnActionFiredListeners;

    // Callbacks to be invoked when an action is executed with error
    protected Set<OnActionErrorListener> mOnActionErrorListeners;

    // Callback to be invoked after a view with an action is clicked and before action handling started.
    // Can intercept an action to prevent it to be fired
    private List<ActionInterceptor> mActionInterceptors;

    /**
     * @param actions list of actions to handle by this handler
     */
    protected ActionHandler(List<ActionPair> actions) {
        mActions = actions != null ? actions : Collections.<ActionPair>emptyList();

        for (ActionPair actionPair : mActions) {
            if (actionPair.action instanceof BaseAction) {
                BaseAction baseAction = ((BaseAction) actionPair.action);
                baseAction.addActionFiredListener(this);
                baseAction.addActionErrorListener(this);
            }
        }
    }

    /**
     * Set new callback to be invoked when an action is executed successfully
     * Note: It is called only for BaseActions.
     * You should call {@link BaseAction#notifyOnActionFired(View, String, Object)} to invoke this callback.
     *
     * @param actionFiredListener new callback to be invoked when an action is executed successfully
     *
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
     * You should call {@link BaseAction#notifyOnActionFired(View, String, Object)} to invoke this callback.
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
     * You should call {@link BaseAction#notifyOnActionError(Throwable, View, String, Object)} to invoke this callback.
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
     * You should call {@link BaseAction#notifyOnActionError(Throwable, View, String, Object)} to invoke this callback.
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
     * You should call {@link BaseAction#notifyOnActionError(Throwable, View, String, Object)} to invoke this callback.
     */
    public void removeAllActionErrorListeners() {
        if (mOnActionErrorListeners != null) {
            mOnActionErrorListeners.clear();
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
     * Add new callback to be invoked after a view with an action is clicked and before action handling started.
     * Can intercept an action to prevent it to be fired
     *
     * @param actionInterceptor The interceptor, which can prevent actions to be fired
     */
    public void addActionInterceptor(ActionInterceptor actionInterceptor) {
        if (mActionInterceptors == null) {
            mActionInterceptors = new ArrayList<>(1);
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
            mActionInterceptors.clear();
        }
    }

    /**
     * Remove all interceptors
     */
    public void removeAllActionInterceptors() {
        if (mActionInterceptors != null) {
            mActionInterceptors.clear();
        }
    }

    @Override
    public void onActionFired(View view, String actionType, Object model) {
        if (mOnActionFiredListeners != null) {
            for (final OnActionFiredListener listener : mOnActionFiredListeners) {
                listener.onActionFired(view, actionType, model);
            }
        }
    }

    @Override
    public void onActionError(Throwable throwable, View view, String actionType, Object model) {
        if (mOnActionErrorListeners != null) {
            for (final OnActionErrorListener listener : mOnActionErrorListeners) {
                listener.onActionError(throwable, view, actionType, model);
            }
        }
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
     * Called when a view with an action is clicked.
     *
     * @param view       The view that was clicked.
     * @param actionType The action type, which appointed to the view
     * @param model      The model, which  appointed to the view and should be handled
     */
    @Override
    public void onActionClick(View view, String actionType, Object model) {
        if (view == null) return;
        final Context context = view.getContext();

        fireAction(context, view, actionType, model);

    }

    /**
     * Call for initiate actions to fire.
     *
     * @param context    The Context, which generally get from view by {@link View#getContext()}
     * @param view       The view that was clicked.
     * @param actionType The action type, which appointed to the view
     * @param model      The model, which  appointed to the view and should be handled
     */
    public void fireAction(Context context, View view, String actionType, Object model) {
        if (mActionInterceptors != null) {
            for (ActionInterceptor interceptor : mActionInterceptors) {
                if (interceptor.onInterceptAction(context, view, actionType, model)) return;
            }
        }

        for (ActionPair actionPair : mActions) {
            if (actionPair.actionType == null || actionPair.actionType.equals(actionType)) {
                final Action action = actionPair.action;
                if (action != null && action.isModelAccepted(model)) {
                    //noinspection unchecked
                    action.onFireAction(context, view, actionType, model);
                }
            }
        }
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
     * Null-safe equivalent of {@code a.equals(b)}.
     */
    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    /**
     * The Builder for configure action handler
     */
    public static final class Builder {
        private List<ActionPair> mActions;
        private Set<OnActionFiredListener> mActionFiredListeners;
        private Set<OnActionErrorListener> mActionErrorListeners;
        private List<ActionInterceptor> mActionInterceptors;

        public Builder() {
            mActions = new ArrayList<>();
        }

        /**
         * Add an action to the action handler
         *
         * @param actionType The type of action
         * @param action     The action
         * @return
         */
        public Builder addAction(String actionType, Action action) {
            mActions.add(new ActionPair(actionType, action));
            return this;
        }

        /**
         * Set new callback to be invoked when an action is executed successfully
         * Note: It is called only for BaseActions.
         * You should call {@link BaseAction#notifyOnActionFired(View, String, Object)} to invoke this callback.
         *
         * @param actionFiredListener new callback to be invoked when an action is executed successfully
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
         * You should call {@link BaseAction#notifyOnActionFired(View, String, Object)} to invoke this callback.
         *
         * @param actionFiredListener new callback to be invoked when an action is executed successfully
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
         * You should call {@link BaseAction#notifyOnActionError(Throwable, View, String, Object)} to invoke this callback.
         *
         * @param actionErrorListener new callback to be invoked when an action is executed successfully
         */
        public Builder addActionErrorListener(final OnActionErrorListener actionErrorListener) {
            if (mActionErrorListeners == null) {
                mActionErrorListeners = new HashSet<>(1);
            }
            mActionErrorListeners.add(actionErrorListener);
            return this;
        }

        /**
         * Set callback to be invoked after a view with an action is clicked and before action handling started.
         * Can intercept an action to prevent it to be fired
         *
         * @param actionInterceptor The interceptor, which can prevent actions to be fired
         * @deprecated use {@link #addActionInterceptor(ActionInterceptor)} instead
         */
        public Builder setActionInterceptor(ActionInterceptor actionInterceptor) {
            addActionInterceptor(actionInterceptor);
            return this;
        }

        /**
         * Add callback to be invoked after a view with an action is clicked and before action handling started.
         * Can intercept an action to prevent it to be fired
         *
         * @param actionInterceptor The interceptor, which can prevent actions to be fired
         */
        public Builder addActionInterceptor(ActionInterceptor actionInterceptor) {
            if (mActionInterceptors == null) {
                mActionInterceptors = new ArrayList<>(1);
            }
            mActionInterceptors.add(actionInterceptor);
            return this;
        }

        public ActionHandler build() {
            final ActionHandler actionHandler = new ActionHandler(mActions);
            if (mActionFiredListeners != null) {
                actionHandler.mOnActionFiredListeners = mActionFiredListeners;
            }
            if (mActionErrorListeners != null) {
                actionHandler.mOnActionErrorListeners = mActionErrorListeners;
            }
            if (mActionInterceptors != null && mActionInterceptors.size() > 0) {
                actionHandler.mActionInterceptors = mActionInterceptors;
            }
            return actionHandler;
        }
    }

    /**
     * Holder for an action and corresponded type
     */
    public static class ActionPair {
        public String actionType;
        public Action action;

        public ActionPair(String actionType, Action action) {
            this.actionType = actionType;
            this.action = action;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ActionPair that = (ActionPair) o;

            if (actionType != null ? !actionType.equals(that.actionType) : that.actionType != null)
                return false;
            return action != null ? action.equals(that.action) : that.action == null;

        }

        @Override
        public int hashCode() {
            int result = actionType != null ? actionType.hashCode() : 0;
            result = 31 * result + (action != null ? action.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ActionPair{" +
                    "actionType='" + actionType + '\'' +
                    ", action=" + action +
                    '}';
        }
    }
}
