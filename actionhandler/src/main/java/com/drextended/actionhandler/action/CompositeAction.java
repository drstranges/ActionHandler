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

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.drextended.actionhandler.listener.OnActionDismissListener;
import com.drextended.actionhandler.listener.OnActionErrorListener;
import com.drextended.actionhandler.listener.OnActionFiredListener;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Specific type of action which can contain a few other actions, show them as menu items,
 * and fire an action, if corresponding item clicked.
 * Show in a menu only actions, which is accepted (checked by {@link Action#isModelAccepted(Object)}).
 * Menu can be displayed as popup menu or as single choice list in an alert dialog
 *
 * @param <M>
 */
public class CompositeAction<M> extends BaseAction<M> implements OnActionFiredListener, OnActionErrorListener, OnActionDismissListener {

    /**
     * Actions for show in a menu (dialog or popup window) and fire if is accepted
     */
    protected final ActionItem[] mActions;

    /**
     * Provider of menu title. Used if menu shows as a dialog
     */
    protected final TitleProvider<M> mTitleProvider;

    // Flag for settings how a menu should be displayed.
    // True for show a menu as popup menu, false for show a menu as alert dialog.
    protected boolean mShowAsPopupMenuEnabled;

    // Flag for settings how a single action item should be fired.
    // True for show a menu, false for fire action directly.
    private boolean mDisplayDialogForSingleAction = true;

    /**
     * Specific type of action which can contain a few other actions, show them as menu items,
     * and fire an action, if corresponding item clicked.
     *
     * @param titleResId                   resource id for corresponding menu item's title
     * @param actions                      action item, which contains menu item titles and actions,
     *                                     which will be fired if corresponding menu item selected
     * @param displayDialogForSingleAction True for show a menu, false for fire action directly
     *                                     if there is only single action in a menu.
     */
    public CompositeAction(@StringRes int titleResId, boolean displayDialogForSingleAction, ActionItem... actions) {
        this(new SimpleTitleProvider<M>(titleResId), displayDialogForSingleAction, actions);
    }

    /**
     * Specific type of action which can contain a few other actions, show them as menu items,
     * and fire an action, if corresponding item clicked.
     *
     * @param titleResId resource id for corresponding menu item's title
     * @param actions    action item, which contains menu item titles and actions,
     *                   which will be fired if corresponding menu item selected
     */
    public CompositeAction(@StringRes int titleResId, ActionItem... actions) {
        this(new SimpleTitleProvider<M>(titleResId), actions);
    }

    /**
     * Specific type of action which can contain a few other actions, show them as menu items,
     * and fire an action, if corresponding item clicked.
     *
     * @param titleProvider provider for corresponding menu title
     * @param actions       action item, which contains menu item titles and actions,
     *                      which will be fired if corresponding menu item selected
     */
    public CompositeAction(TitleProvider<M> titleProvider, ActionItem... actions) {
        this(titleProvider, true, actions);
    }

    /**
     * Specific type of action which can contain a few other actions, show them as menu items,
     * and fire an action, if corresponding item clicked.
     *
     * @param titleProvider                provider for corresponding menu item's title
     * @param actions                      action item, which contains menu item titles and actions,
     *                                     which will be fired if corresponding menu item selected
     * @param displayDialogForSingleAction True for show a menu, false for fire action directly
     *                                     if there is only single action in a menu.
     */
    public CompositeAction(TitleProvider<M> titleProvider, boolean displayDialogForSingleAction, ActionItem... actions) {
        if (actions == null) throw new InvalidParameterException("Provide at least one action");
        mActions = actions;
        mTitleProvider = titleProvider;
        mDisplayDialogForSingleAction = displayDialogForSingleAction;

        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                // add listeners to menu actions
                BaseAction baseAction = (BaseAction) item.action;
                baseAction.addActionFiredListener(this);
                baseAction.addActionErrorListener(this);
                baseAction.addActionDismissListener(this);
            }
        }
    }

    /**
     * Flag for settings how a menu should be displayed.
     *
     * @param showAsPopupMenuEnabled true for show a menu as popup window,
     *                               false for show a menu as alert dialog.
     */
    public void setShowAsPopupMenuEnabled(boolean showAsPopupMenuEnabled) {
        mShowAsPopupMenuEnabled = showAsPopupMenuEnabled;
    }

    /**
     * Check if there is at least one action which can handle given model
     *
     * @param model The model to check if it can be handled.
     * @return true if there is at least one action which can handle given model, false otherwise.
     */
    @Override
    public boolean isModelAccepted(Object model) {
        for (ActionItem action : mActions) {
            if (action.action.isModelAccepted(model)) return true;
        }
        return false;
    }

    /**
     * Count actions which can handle given model
     *
     * @param model The model to check if it can be handled.
     * @return Count for actions which can handle given model
     */
    private int getAcceptedActionCount(Object model) {
        int count = 0;
        for (ActionItem action : mActions) {
            if (action.action.isModelAccepted(model)) count++;
        }
        return count;
    }

    /**
     * Returns first action which can handle given model
     *
     * @param model The model to check if it can be handled.
     * @return first action which can handle given model
     */
    private ActionItem getFirstAcceptedActionItem(M model) {
        for (ActionItem action : mActions) {
            if (action.action.isModelAccepted(model)) return action;
        }
        return null;
    }

    @Override
    public void onFireAction(Context context, @Nullable View view, @Nullable String actionType, @Nullable M model) {
        if (!mDisplayDialogForSingleAction && getAcceptedActionCount(model) == 1) {
            final ActionItem actionItem = getFirstAcceptedActionItem(model);
            if (actionItem != null) //noinspection unchecked
                actionItem.action.onFireAction(context, view, actionType, model);
        } else {
            showMenu(context, view, actionType, model);
        }
    }

    /**
     * Show menu with list of actions, which can handle this {@param model}.
     *
     * @param context The Context, which generally get from view by {@link View#getContext()}
     * @param view    The View, which can be used for prepare any visual effect (like animation),
     *                Generally it is that view which was clicked and initiated action to fire.
     * @param actionType The action type
     * @param model   The model which should be handled by the action.
     */
    private void showMenu(final Context context, final View view, String actionType, final M model) {

        // prepare menu items
        final List<ActionItem> menuItems = prepareMenuListItems(model);

        if (mShowAsPopupMenuEnabled) {
            //show as popup menu
            PopupMenu popupMenu = buildPopupMenu(context, view, actionType, model, menuItems);
            popupMenu.show();

        } else {
            //show as alert dialog with single choice items
            String title = mTitleProvider.getTitle(context, model);
            AlertDialog.Builder builder = buildAlertDialog(context, view, actionType, model, title, menuItems);
            final AlertDialog dialog = builder.create();
            if (title == null) {
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            }
            builder.show();
        }
    }

    /**
     * Prepare menu items to show in a menu
     *
     * @param model The model which should be handled by the action.
     * @return list of menu items
     */
    @NonNull
    protected List<ActionItem> prepareMenuListItems(M model) {
        int count = mActions.length;
        final List<ActionItem> menuItems = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            final ActionItem item = mActions[index];
            if (item.action.isModelAccepted(model)) {
                menuItems.add(item);
            }
        }
        return menuItems;
    }

    /**
     * Prepares popup menu to show given menu items
     *
     * @param context   The Context, which generally get from view by {@link View#getContext()}
     * @param view      The View, which can be used for prepare any visual effect (like animation),
     *                  Generally it is that view which was clicked and initiated action to fire.
     * @param actionType The action type
     * @param model     The model which should be handled by the action.
     * @param menuItems list of items which will be shown in a menu
     * @return popup menu to show given menu items
     */
    protected PopupMenu buildPopupMenu(final Context context, final View view, final String actionType, final M model, final List<ActionItem> menuItems) {
        final PopupMenu popupMenu = new PopupMenu(context, view);
        final Menu menu = popupMenu.getMenu();
        int count = menuItems.size();
        for (int index = 0; index < count; index++) {
            final ActionItem item = menuItems.get(index);
            //noinspection unchecked
            menu.add(0, index, 0, item.titleProvider.getTitle(context, model));
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final ActionItem actionItem = menuItems.get(item.getItemId());
                //noinspection unchecked
                actionItem.action.onFireAction(context, view, actionItem.actionType, model);
                return true;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                notifyOnActionDismiss("CompositeAction menu dismissed", view, actionType, model);
            }
        });
        return popupMenu;
    }

    /**
     * Prepares alert dialog to show given menu items
     *
     * @param context   The Context, which generally get from view by {@link View#getContext()}
     * @param view      The View, which can be used for prepare any visual effect (like animation),
     *                  Generally it is that view which was clicked and initiated action to fire.
     * @param actionType The action type
     * @param model     The model which should be handled by the action.
     * @param menuItems list of items which will be shown in a menu
     * @return alert dialog builder to show given menu items
     */
    protected AlertDialog.Builder buildAlertDialog(final Context context, final View view, final String actionType, final M model, String title, final List<ActionItem> menuItems) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title);

        int count = menuItems.size();
        final CharSequence[] itemLabels = new CharSequence[count];

        for (int index = 0; index < count; index++) {
            final ActionItem item = menuItems.get(index);
            //noinspection unchecked
            itemLabels[index] = item.titleProvider.getTitle(context, model);
        }
        builder.setItems(itemLabels, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final ActionItem actionItem = menuItems.get(which);
                //noinspection unchecked
                actionItem.action.onFireAction(context, view, actionItem.actionType, model);
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                notifyOnActionDismiss("CompositeAction menu dismissed", view, actionType, model);
            }
        });
        return builder;
    }

    @Override
    public void onActionFired(View view, String actionType, Object model) {
        notifyOnActionFired(view, actionType, model);
    }

    @Override
    public void onActionError(Throwable throwable, View view, String actionType, Object model) {
        notifyOnActionError(throwable, view, actionType, model);
    }

    @Override
    public void onActionDismiss(String reason, View view, String actionType, Object model) {
        notifyOnActionDismiss(reason, view, actionType, model);
    }

    /**
     * Action item
     */
    public static class ActionItem<M> {
        /**
         * Resource id for the title associated with this item.
         */
        public final TitleProvider<M> titleProvider;
        /**
         * Action type associated with this item.
         */
        public final String actionType;
        /**
         * Action associated with this item.
         */
        public final Action action;

        /**
         * @param actionType     The action type associated with this item.
         * @param action         The action associated with this item.
         * @param menuItemTitleResId The resource id for the title associated with this item.
         */
        public ActionItem(String actionType, Action action, @StringRes int menuItemTitleResId) {
            this(actionType, action, new SimpleTitleProvider<M>(menuItemTitleResId));
        }

        /**
         * @param actionType     The action type associated with this item.
         * @param action         The action associated with this item.
         * @param titleProvider  provider for corresponding menu item's title
         */
        public ActionItem(String actionType, Action action, TitleProvider<M> titleProvider) {
            this.actionType = actionType;
            this.action = action;
            this.titleProvider = titleProvider;
        }
    }

    /**
     * Provide adjustable title
     *
     * @param <M>
     */
    public interface TitleProvider<M> {
        /**
         * Provide adjustable title
         *
         * @param context The Context
         * @param model   The model, which should be handled
         * @return the title, suitable for given model
         */
        String getTitle(Context context, M model);
    }

    /**
     * Simple title provider which give just static title
     *
     * @param <M>
     */
    public static class SimpleTitleProvider<M> implements TitleProvider<M> {

        /**
         * Resource id for the title.
         */
        private final int mTitleResId;

        /**
         * @param titleResId Resource id for the title.
         */
        public SimpleTitleProvider(int titleResId) {
            mTitleResId = titleResId;
        }

        @Override
        public String getTitle(Context context, M model) {
            return mTitleResId == 0 ? null : context.getString(mTitleResId);
        }
    }
}
