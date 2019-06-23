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

package com.drextended.actionhandler.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.annotation.Nullable;

public class AUtils {


    public static Activity getAliveActivity(@Nullable final Context context) {
        if (context != null) {
            Context baseContext = context;
            while (!(baseContext instanceof Activity) && baseContext instanceof ContextWrapper) {
                baseContext = ((ContextWrapper) baseContext).getBaseContext();
            }
            if (baseContext instanceof Activity) {
                Activity activity = (Activity) baseContext;
                if (isAlive(activity) && !activity.isFinishing()) {
                    return activity;
                }
            }
        }
        return null;
    }

    public static Activity getActivity(@Nullable final Context context) {
        if (context != null) {
            Context baseContext = context;
            while (!(baseContext instanceof Activity) && baseContext instanceof ContextWrapper) {
                baseContext = ((ContextWrapper) baseContext).getBaseContext();
            }
            if (baseContext instanceof Activity) {
                return (Activity) baseContext;
            }
        }
        return null;
    }

    public static boolean isAlive(Dialog dialog) {
        return dialog != null && isAlive(getActivity(dialog.getContext()));
    }

    public static boolean isAlive(final Activity activity) {
        if (activity == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !activity.isDestroyed();
        }
        return true;
    }
}
