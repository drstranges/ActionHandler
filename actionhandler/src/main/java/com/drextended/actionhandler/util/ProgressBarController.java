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
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This class used to show progress bar for asynchronous requests
 */
public class ProgressBarController {
    public static final String DEFAULT_TAG = "default_tag";

    private static final Object sLock = new Object();
    private static Application.ActivityLifecycleCallbacks sLifecycleCallbacks;
    private static WeakHashMap<ProgressDialog, Tag> sDialogs = new WeakHashMap<>();

    /**
     * Call this before first call of {@link #showProgressDialog}
     *
     * @param app application
     */
    public static void init(Application app) {
        if (sLifecycleCallbacks != null) {
            app.unregisterActivityLifecycleCallbacks(sLifecycleCallbacks);
        }
        sLifecycleCallbacks = new OnDestroyActivityCallback() {
            @Override
            public void onActivityDestroyed(Activity activity) {
                final int hashCode = activity.hashCode();
                synchronized (sLock) {
                    final List<ProgressDialog> dialogs = findDialogs(hashCode);
                    for (ProgressDialog dialog : dialogs) {
                        dialog.dismiss();
                        sDialogs.remove(dialog);
                    }
                }
            }
        };
        app.registerActivityLifecycleCallbacks(sLifecycleCallbacks);
    }

    /**
     * Hides all dialogs and unregisters activity lifecycle callbacks
     * @param app    application instance
     */
    public static void release(Application app) {
        if (sLifecycleCallbacks != null) {
            app.unregisterActivityLifecycleCallbacks(sLifecycleCallbacks);
        }
        hideProgressDialogsAll();
    }

    /**
     * Shows default progress dialog without any message
     *
     * @param context context
     */
    public static void showProgressDialog(final Context context) {
        showProgressDialog(context, DEFAULT_TAG, null);
    }

    /**
     * Shows default dialog with a message
     *
     * @param context context
     * @param message the message to show in a dialog
     */
    public static void showProgressDialog(final Context context, final String message) {
        showProgressDialog(context, DEFAULT_TAG, message);
    }

    /**
     * Shows a new dialog with a message for each different tag.
     *
     * @param context context
     * @param tag     the tag for determining specific dialog
     * @param message the message to show in a dialog
     */
    public static void showProgressDialog(final Context context, String tag, final String message) {
        final Activity activity = getActivity(context);
        if (!isAlive(activity) || activity.isFinishing()) return;
        if (tag == null) tag = DEFAULT_TAG;
        ProgressDialog dialog = null;
        synchronized (sLock) {
            dialog = findDialog(tag);

            if (!isAlive(dialog)) {
                if (dialog != null) sDialogs.remove(dialog);
                dialog = new ProgressDialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                sDialogs.put(dialog, new Tag(tag, activity.hashCode()));
            }
        }
        dialog.setMessage(message);
        dialog.show();
    }

    /**
     * Hide default dialog
     */
    public static void hideProgressDialog() {
        hideProgressDialog(DEFAULT_TAG);
    }

    /**
     * Hide dialog with specific tag
     * @param tag the tag to determine specific dialog
     */
    public static void hideProgressDialog(String tag) {
        if (tag == null) return;
        synchronized (sLock) {
            ProgressDialog dialog = findDialog(tag);
            if (dialog != null) {
                if (isAlive(dialog) && dialog.isShowing()) dialog.dismiss();
                sDialogs.remove(dialog);
            }
        }
    }

    /**
     * Hide all dialogs
     */
    public static void hideProgressDialogsAll() {
        synchronized (sLock) {
            for (ProgressDialog dialog : sDialogs.keySet()) {
                if (isAlive(dialog) && dialog.isShowing()) dialog.dismiss();
            }
            sDialogs.clear();
        }
    }

    private static ProgressDialog findDialog(@NonNull String tag) {
        for (Map.Entry<ProgressDialog, Tag> entry : sDialogs.entrySet()) {
            if (tag.equals(entry.getValue().tag)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static List<ProgressDialog> findDialogs(long activityHashcode) {
        List<ProgressDialog> result = new ArrayList<>();
        for (Map.Entry<ProgressDialog, Tag> entry : sDialogs.entrySet()) {
            if (activityHashcode == entry.getValue().activityHashcode) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private static Activity getActivity(final Context context) {
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

    private static boolean isAlive(Dialog dialog) {
        return dialog != null && isAlive(getActivity(dialog.getContext()));
    }

    private static boolean isAlive(final Activity activity) {
        if (activity == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed()) return false;
        }
        return true;
    }

    private static class Tag {
        final String tag;
        final long activityHashcode;

        private Tag(String tag, long activityHashcode) {
            this.tag = tag;
            this.activityHashcode = activityHashcode;
        }
    }

    private static abstract class OnDestroyActivityCallback implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }
    }
}
