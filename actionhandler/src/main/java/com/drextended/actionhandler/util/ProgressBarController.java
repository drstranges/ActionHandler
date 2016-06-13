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
import android.app.ProgressDialog;
import android.content.Context;
import android.view.Window;

import java.lang.ref.WeakReference;

/**
 * This class used to show progress bar for asynchronous requests
 */
public class ProgressBarController {

    /**
     * Holder of application context as weak reference
     */
    private static WeakReference<Context> sContextHolder;

    /**
     * Save a progress dialog for reuse.
     */
    private static ProgressDialog sProgressDialog;

    /**
     * Show a progress dialog with defined message.
     *
     * @param context
     * @param message message to display in a dialog
     */
    public static void showProgressDialog(final Context context, final String message) {
        if (context == null
                || (context instanceof Activity && ((Activity) context).isFinishing())) {
            return;
        }
        if (sContextHolder == null || sContextHolder.get() == null
                || sContextHolder.get() != context)
            sContextHolder = new WeakReference<>(context);
        if (sProgressDialog == null) {
            sProgressDialog = new ProgressDialog(context);
            sProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            sProgressDialog.setCancelable(false);
        }
        sProgressDialog.setMessage(message);
        sProgressDialog.show();
    }

    /**
     * Show a progress dialog without any message, only progress bar will be appeared.
     *
     * @param context
     */
    public static void showProgressDialog(final Context context) {
        showProgressDialog(context, null);
    }

    /**
     * Hide previously showed progress dialog.
     * Do nothing if progress dialog had not been showed previously or has been already hide.
     */
    public static void hideProgressDialog() {
        if (sProgressDialog != null && sProgressDialog.isShowing())
            sProgressDialog.dismiss();
        sProgressDialog = null;
        if (sContextHolder != null)
            sContextHolder.clear();
    }
}
