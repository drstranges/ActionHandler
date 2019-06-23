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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.drextended.actionhandler.ActionArgs;
import com.drextended.actionhandler.R;
import com.drextended.actionhandler.listener.ActionFireInterceptor;
import com.drextended.actionhandler.listener.OnActionDismissListener;
import com.drextended.actionhandler.listener.OnActionErrorListener;
import com.drextended.actionhandler.listener.OnActionFiredListener;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Specific type of action which can contain a few other actions, show them as menu items,
 * and fire an action, if corresponding item clicked.
 * Show in a menu only actions, which is accepted (checked by {@link Action#isModelAccepted(Object)}).
 * Menu can be displayed as popup menu or as single choice list in an alert dialog
 *
 * @param <M> model type
 */
@SuppressWarnings("SameParameterValue")
public class CompositeAction<M> extends BaseAction {

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
    protected boolean mDisplayDialogForSingleAction;

    // True for show non accepted actions in menu as disabled, false to hide them.
    protected boolean mShowNonAcceptedActions;

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
        this(new SimpleTitleProvider<M>(titleResId), displayDialogForSingleAction, false, actions);
    }

    /**
     * Specific type of action which can contain a few other actions, show them as menu items,
     * and fire an action, if corresponding item clicked.
     *
     * @param titleResId                   resource id for corresponding menu item's title
     * @param actions                      action item, which contains menu item titles and actions,
     *                                     which will be fired if corresponding menu item selected
     * @param displayDialogForSingleAction True for show a menu, false for fire action directly
     *                                     if there is only single action in a menu.
     * @param showNonAcceptedActions       true for show non accepted actions in menu as disabled,
     *                                     false to hide them
     */
    public CompositeAction(@StringRes int titleResId, boolean displayDialogForSingleAction,
                           boolean showNonAcceptedActions, ActionItem... actions) {
        this(new SimpleTitleProvider<M>(titleResId), displayDialogForSingleAction, showNonAcceptedActions, actions);
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
        this(new SimpleTitleProvider<M>(titleResId), true, false, actions);
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
        this(titleProvider, true, false, actions);
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
        this(titleProvider, displayDialogForSingleAction, false, actions);
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
     * @param showNonAcceptedActions       true for show non accepted actions in menu as disabled,
     *                                     false to hide them
     */
    public CompositeAction(TitleProvider<M> titleProvider, boolean displayDialogForSingleAction,
                           boolean showNonAcceptedActions, ActionItem... actions) {
        if (actions == null) throw new InvalidParameterException("Provide at least one action");
        mActions = actions;
        mTitleProvider = titleProvider;
        mDisplayDialogForSingleAction = displayDialogForSingleAction;
        mShowNonAcceptedActions = showNonAcceptedActions;
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
     * Flag for settings how a single action item should be fired.
     *
     * @param displayDialogForSingleAction true for show a menu, false for fire action directly.
     */
    public void setDisplayDialogForSingleAction(boolean displayDialogForSingleAction) {
        mDisplayDialogForSingleAction = displayDialogForSingleAction;
    }

    /**
     * Flag for settings how non accepted action item should be showed in the menu.
     *
     * @param showNonAcceptedActions true for show non accepted actions in menu as disabled, false to hide them.
     */
    public void setShowNonAcceptedActions(boolean showNonAcceptedActions) {
        mShowNonAcceptedActions = showNonAcceptedActions;
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
    private int getAcceptedActionCount(@Nullable Object model) {
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
    @Nullable
    private ActionItem getFirstAcceptedActionItem(@Nullable Object model) {
        for (ActionItem action : mActions) {
            if (action.action.isModelAccepted(model)) return action;
        }
        return null;
    }

    @Override
    public void onFireAction(@NonNull ActionArgs args) {
        if (!mDisplayDialogForSingleAction && getAcceptedActionCount(args.params.model) == 1) {
            final ActionItem actionItem = getFirstAcceptedActionItem(args.params.model);
            if (actionItem != null) {
                fireActionItem(args, actionItem);
            }
        } else {
            showMenu(args);
        }
    }

    private void fireActionItem(@NonNull ActionArgs args, @NonNull ActionItem actionItem) {
        if (!interceptActionFire(args.params, actionItem.actionType, actionItem.action)) {
            notifyOnActionFired(args);
            actionItem.action.onFireAction(new ActionArgs(args.params, actionItem.actionType));
        }
    }

    /**
     * Show menu with list of actions, which can handle this {@param model}.
     *
     * @param args The action params, which appointed to the view
     *             and type of the action which was actually executed.
     */
    protected void showMenu(@NonNull final ActionArgs args) {

        // prepare menu items
        final List<ActionItem> menuItems = prepareMenuListItems(args.params.model);

        if (mShowAsPopupMenuEnabled) {
            final View view = args.params.tryGetView();
            if (view == null) {
                notifyOnActionError(args, new Exception("Attempt to show PopupMenu without target view"));
                return;
            }
            //show as popup menu
            PopupMenu popupMenu = buildPopupMenu(args, view, menuItems);
            popupMenu.show();
        } else {
            //show as alert dialog with single choice items
            //noinspection unchecked
            String title = mTitleProvider.getTitle(args.params.appContext, (M) args.params.model);
            AlertDialog.Builder builder = buildAlertDialog(args, menuItems, title);
            final AlertDialog dialog = builder.create();
            if (mShowNonAcceptedActions) {
                final AdapterView.OnItemClickListener clickListener = dialog.getListView().getOnItemClickListener();
                if (clickListener != null) {
                    dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (menuItems.get(position).action.isModelAccepted(args.params.model)) {
                                clickListener.onItemClick(parent, view, position, id);
                            }
                        }
                    });
                }
            }
            if (title == null) {
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            }
            dialog.show();
        }
    }

    /**
     * Prepare menu items to show in a menu
     *
     * @param model The model which should be handled by the action.
     * @return list of menu items
     */
    @NonNull
    protected List<ActionItem> prepareMenuListItems(@Nullable Object model) {
        if (mShowNonAcceptedActions) return Arrays.asList(mActions);
        int count = mActions.length;
        final List<ActionItem> menuItems = new ArrayList<>(count);
        for (final ActionItem item : mActions) {
            if (item.action.isModelAccepted(model)) {
                menuItems.add(item);
            }
        }
        return menuItems;
    }

    /**
     * Prepares popup menu to show given menu items
     *
     * @param args      The action params, which appointed to the view and actually actionType
     * @param view      The View, which can be used for prepare any visual effect (like animation),
     *                  Generally it is that view which was clicked and initiated action to fire.
     * @param menuItems list of items which will be shown in a menu
     * @return popup menu to show given menu items
     */
    protected PopupMenu buildPopupMenu(@NonNull final ActionArgs args, @NonNull View view, @NonNull final List<ActionItem> menuItems) {
        Context context = view.getContext();
        final PopupMenu popupMenu = new PopupMenu(context, view);
        final Menu menu = popupMenu.getMenu();
        int count = menuItems.size();
        for (int index = 0; index < count; index++) {
            final ActionItem item = menuItems.get(index);
            //noinspection unchecked
            menu.add(0, index, 0, item.titleProvider.getTitle(context, args.params.model));
            if (mShowNonAcceptedActions) {
                menu.getItem(index).setEnabled(item.action.isModelAccepted(args.params.model));
            }
        }
        final AtomicBoolean activated = new AtomicBoolean(false);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                activated.set(true);
                final ActionItem actionItem = menuItems.get(item.getItemId());
                if (item.isEnabled()) {
                    fireActionItem(args, actionItem);
                }
                return true;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                if (!activated.get()) {
                    notifyOnActionDismiss(args, "CompositeAction menu dismissed");
                }
            }
        });
        return popupMenu;
    }

    /**
     * Prepares alert dialog to show given menu items
     *
     * @param args      The action params, which appointed to the view and actually actionType
     * @param menuItems list of items which will be shown in a menu
     * @param title     The title of the dialog.
     * @return alert dialog builder to show given menu items
     */
    protected AlertDialog.Builder buildAlertDialog(
            @NonNull final ActionArgs args,
            @NonNull final List<ActionItem> menuItems,
            @Nullable String title
    ) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(args.params.getViewOrAppContext())
                .setTitle(title);

        final Object model = args.params.model;
        MenuItemsAdapter adapter = new MenuItemsAdapter(
                getMenuItemLayoutResId(),
                menuItems,
                model,
                mShowNonAcceptedActions
        );
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final ActionItem actionItem = menuItems.get(which);
                if (actionItem.action.isModelAccepted(model)) {
                    fireActionItem(args, actionItem);
                }
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                notifyOnActionDismiss(args, "CompositeAction menu cancelled");
            }
        });
        return builder;
    }

    /**
     * Returns layout res id for menu item.
     * Has to contain at least TextView with id "@android:id/text1" and ImageView with id "@android:id/icon".
     *
     * @return the layout res id for menu item.
     */
    protected int getMenuItemLayoutResId() {
        return R.layout.item_menu_composit_action;
    }

    @Override
    public void addActionFiredListener(OnActionFiredListener listener) {
        super.addActionFiredListener(listener);
        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                ((BaseAction) item.action).addActionFiredListener(listener);
            }
        }
    }

    @Override
    public void addActionErrorListener(OnActionErrorListener listener) {
        super.addActionErrorListener(listener);
        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                ((BaseAction) item.action).addActionErrorListener(listener);
            }
        }
    }

    @Override
    public void addActionDismissListener(OnActionDismissListener listener) {
        super.addActionDismissListener(listener);
        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                ((BaseAction) item.action).addActionDismissListener(listener);
            }
        }
    }

    @Override
    public void addActionFireInterceptor(ActionFireInterceptor interceptor) {
        super.addActionFireInterceptor(interceptor);
        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                ((BaseAction) item.action).addActionFireInterceptor(interceptor);
            }
        }
    }

    @Override
    public void removeActionFireListener(OnActionFiredListener listener) {
        super.removeActionFireListener(listener);
        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                ((BaseAction) item.action).removeActionFireListener(listener);
            }
        }
    }

    @Override
    public void removeAllActionFireListeners() {
        super.removeAllActionFireListeners();
        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                ((BaseAction) item.action).removeAllActionFireListeners();
            }
        }
    }

    @Override
    public void removeActionErrorListener(OnActionErrorListener listener) {
        super.removeActionErrorListener(listener);
        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                ((BaseAction) item.action).removeActionErrorListener(listener);
            }
        }
    }

    @Override
    public void removeAllActionErrorListeners() {
        super.removeAllActionErrorListeners();
        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                ((BaseAction) item.action).removeAllActionFireListeners();
            }
        }
    }

    @Override
    public void removeActionDismissListener(OnActionDismissListener listener) {
        super.removeActionDismissListener(listener);
        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                ((BaseAction) item.action).removeActionDismissListener(listener);
            }
        }
    }

    @Override
    public void removeAllActionDismissListeners() {
        super.removeAllActionDismissListeners();
        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                ((BaseAction) item.action).removeAllActionDismissListeners();
            }
        }
    }

    @Override
    public void removeActionFireInterceptor(ActionFireInterceptor interceptor) {
        super.removeActionFireInterceptor(interceptor);
        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                ((BaseAction) item.action).removeActionFireInterceptor(interceptor);
            }
        }
    }

    @Override
    public void removeAllActionFireInterceptors() {
        super.removeAllActionFireInterceptors();
        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                ((BaseAction) item.action).removeAllActionFireListeners();
            }
        }
    }

    @Override
    public void removeAllActionListeners() {
        super.removeAllActionListeners();
        for (ActionItem item : mActions) {
            if (item.action instanceof BaseAction) {
                ((BaseAction) item.action).removeAllActionListeners();
            }
        }
    }

    /**
     * Action item
     */
    public static class ActionItem<M> {
        /**
         * Provider for the title associated with this item.
         */
        public final TitleProvider<M> titleProvider;
        /**
         * Provider for the icon associated with this item.
         */
        public final IconProvider<M> iconProvider;
        /**
         * Action type associated with this item.
         */
        public final String actionType;
        /**
         * Action associated with this item.
         */
        public final Action action;

        /**
         * @param actionType         The action type associated with this item.
         * @param action             The action associated with this item.
         * @param menuItemTitleResId The resource id for the title associated with this item.
         */
        public ActionItem(String actionType, Action action, @StringRes int menuItemTitleResId) {
            this(actionType, action, null, new SimpleTitleProvider<M>(menuItemTitleResId));
        }

        /**
         * @param actionType         The action type associated with this item.
         * @param action             The action associated with this item.
         * @param iconResId          The icon res id associated with this item.
         * @param iconTintColorResId The icon tint color res id associated with this item.
         * @param menuItemTitleResId The resource id for the title associated with this item.
         */
        public ActionItem(String actionType, Action action, @DrawableRes int iconResId, @ColorRes int iconTintColorResId, @StringRes int menuItemTitleResId) {
            this(actionType, action, iconResId == 0 ? null : new SimpleIconProvider<M>(iconResId, iconTintColorResId), new SimpleTitleProvider<M>(menuItemTitleResId));
        }

        /**
         * @param actionType    The action type associated with this item.
         * @param action        The action associated with this item.
         * @param titleProvider provider for corresponding menu item's title
         */
        public ActionItem(String actionType, Action action, TitleProvider<M> titleProvider) {
            this(actionType, action, null, titleProvider);
        }

        /**
         * @param actionType         The action type associated with this item.
         * @param action             The action associated with this item.
         * @param iconResId          The icon res id associated with this item.
         * @param iconTintColorResId The icon tint color res id for the icon.
         * @param titleProvider      The provider for corresponding menu item's title
         */
        public ActionItem(String actionType, Action action, @DrawableRes int iconResId, @ColorRes int iconTintColorResId, TitleProvider<M> titleProvider) {
            this(actionType, action, iconResId == 0 ? null : new SimpleIconProvider<M>(iconResId, iconTintColorResId), titleProvider);
        }

        /**
         * @param actionType    The action type associated with this item.
         * @param action        The action associated with this item.
         * @param iconProvider  The provider for icon associated with this item.
         * @param titleProvider The provider for corresponding menu item's title
         */
        public ActionItem(String actionType, Action action, IconProvider<M> iconProvider, TitleProvider<M> titleProvider) {
            this.iconProvider = iconProvider;
            this.actionType = actionType;
            this.action = action;
            this.titleProvider = titleProvider;
        }
    }

    /**
     * Provide adjustable title
     *
     * @param <M> model type
     */
    public interface TitleProvider<M> {
        /**
         * Provide adjustable title
         *
         * @param ctx   The Context
         * @param model The model, which should be handled
         * @return the title, suitable for given model
         */
        @Nullable
        String getTitle(@NonNull Context ctx, @Nullable M model);
    }

    /**
     * Provide icon drawable
     *
     * @param <M> model type
     */
    public interface IconProvider<M> {
        /**
         * Provide icon drawable
         *
         * @param context The Context
         * @param model   The model, which should be handled
         * @return the icon drawable, suitable for given item
         */
        @Nullable
        Drawable getIconDrawable(@NonNull Context context, @Nullable M model);
    }

    /**
     * Simple title provider which give just static title
     *
     * @param <M> model type
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
        public String getTitle(@NonNull Context context, M model) {
            return mTitleResId == 0 ? null : context.getString(mTitleResId);
        }
    }

    /**
     * Simple icon provider which give just static icon
     *
     * @param <M> model type
     */
    public static class SimpleIconProvider<M> implements IconProvider<M> {

        /**
         * Resource id for the icon.
         */
        private final int mIconResId;

        /**
         * Resource id for icon's tint color.
         */
        private final int mIconTintResId;

        /**
         * @param iconResId     Resource id for the icon.
         * @param iconTintResId Resource id for tint color for the icon.
         */
        public SimpleIconProvider(int iconResId, int iconTintResId) {
            mIconResId = iconResId;
            mIconTintResId = iconTintResId;
        }

        @Override
        public Drawable getIconDrawable(@NonNull Context context, M model) {
            if (mIconResId != 0) {
                Drawable drawable = ContextCompat.getDrawable(context, mIconResId);
                if (drawable != null) {
                    drawable = DrawableCompat.wrap(drawable.mutate());
                    if (mIconTintResId != 0) {
                        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, mIconTintResId));
                    }
                }
                return drawable;
            }
            return null;
        }
    }

    private static class MenuItemsAdapter extends BaseAdapter {
        private final int mItemLayoutResId;
        private final List<ActionItem> mItems;
        private final Object mModel;
        private final boolean mShowNonAcceptedActions;
        private final boolean mHasIcons;

        public MenuItemsAdapter(@LayoutRes int itemLayoutResId, List<ActionItem> menuItems, Object model, boolean showNonAcceptedActions) {
            mItemLayoutResId = itemLayoutResId;
            mItems = menuItems;
            mModel = model;
            mShowNonAcceptedActions = showNonAcceptedActions;
            mHasIcons = checkHasIcons(mItems);
        }

        private boolean checkHasIcons(List<ActionItem> items) {
            for (final ActionItem item : items) {
                if (item.iconProvider != null) return true;
            }
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Context context = parent.getContext();
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(mItemLayoutResId, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(R.id.viewHolder, viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag(R.id.viewHolder);
            }

            ActionItem item = mItems.get(position);

            boolean modelAccepted = true;
            if (mShowNonAcceptedActions) {
                modelAccepted = item.action.isModelAccepted(mModel);
                viewHolder.itemView.setEnabled(modelAccepted);
            }
            //noinspection unchecked
            final String label = item.titleProvider.getTitle(context, mModel);
            viewHolder.textView.setText(label);

            if (item.iconProvider != null) {
                //noinspection unchecked
                Drawable icon = item.iconProvider.getIconDrawable(context, mModel);
                if (icon != null) {
                    viewHolder.imageView.setVisibility(View.VISIBLE);
                    viewHolder.imageView.setImageDrawable(icon);
                    if (mShowNonAcceptedActions) {
                        viewHolder.imageView.setAlpha(modelAccepted ? 1.0f : 0.3f);
                    }
                } else {
                    viewHolder.imageView.setVisibility(mHasIcons ? View.INVISIBLE : View.GONE);
                }
            } else {
                viewHolder.imageView.setVisibility(mHasIcons ? View.INVISIBLE : View.GONE);
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return mItems != null ? mItems.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private static class ViewHolder {
            final View itemView;
            final TextView textView;
            final ImageView imageView;

            public ViewHolder(View itemView) {
                this.itemView = itemView;
                this.textView = (TextView) itemView.findViewById(android.R.id.text1);
                this.imageView = (ImageView) itemView.findViewById(android.R.id.icon);
            }
        }
    }
}
