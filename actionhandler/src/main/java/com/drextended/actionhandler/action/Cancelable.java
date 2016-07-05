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

import com.drextended.actionhandler.ActionHandler;

/**
 * Defines an interface for actions that can (or need to) be cancelled. For example, if they
 * are not used any longer or if ActionHandler will be recreated and all old actions has to be cancelled.
 * For actions collected by {@link ActionHandler} this method can be called by {@link ActionHandler#cancelAll()}
 */
public interface Cancelable {

    /**
     * Cancel any action once this method is called.
     * For actions collected by {@link ActionHandler} this method can be called by {@link ActionHandler#cancelAll()}
     */
    void cancel();
}
