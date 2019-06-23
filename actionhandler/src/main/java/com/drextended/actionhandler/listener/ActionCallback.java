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

package com.drextended.actionhandler.listener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drextended.actionhandler.ActionArgs;
import com.drextended.actionhandler.ActionParams;
import com.drextended.actionhandler.action.Action;

/**
 * One interface for all listeners and interceptors.
 */
public interface ActionCallback extends ActionInterceptor, ActionFireInterceptor,
        OnActionFiredListener, OnActionDismissListener, OnActionErrorListener {

    abstract class SimpleActionCallback implements ActionCallback {

        /**
         * @inheritDocs
         */
        @Override
        public boolean onInterceptActionFire(@NonNull ActionParams actionParams, @Nullable String actionType, @NonNull Action action) {
            return false;
        }

        /**
         * @inheritDocs
         */
        @Override
        public boolean onInterceptAction(@NonNull ActionParams params) {
            return false;
        }

        /**
         * @inheritDocs
         */
        @Override
        public void onActionDismiss(@NonNull ActionArgs args, @Nullable String reason) {
        }

        /**
         * @inheritDocs
         */
        @Override
        public void onActionError(@NonNull ActionArgs args, @Nullable Throwable throwable) {
        }

        /**
         * @inheritDocs
         */
        @Override
        public void onActionFired(@NonNull ActionArgs args, @Nullable Object result) {
        }
    }
}
