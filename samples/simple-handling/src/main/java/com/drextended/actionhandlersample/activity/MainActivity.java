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

package com.drextended.actionhandlersample.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.drextended.actionhandler.ActionArgs;
import com.drextended.actionhandler.ActionHandler;
import com.drextended.actionhandler.ActionParams;
import com.drextended.actionhandler.action.CompositeAction;
import com.drextended.actionhandler.action.CompositeAction.ActionItem;
import com.drextended.actionhandler.action.DialogAction;
import com.drextended.actionhandler.listener.ActionInterceptor;
import com.drextended.actionhandler.listener.OnActionDismissListener;
import com.drextended.actionhandler.listener.OnActionErrorListener;
import com.drextended.actionhandler.listener.OnActionFiredListener;
import com.drextended.actionhandler.util.ViewOnActionClickListener;
import com.drextended.actionhandlersample.ActionType;
import com.drextended.actionhandlersample.R;
import com.drextended.actionhandlersample.action.OpenSecondActivity;
import com.drextended.actionhandlersample.action.SampleRequestAction;
import com.drextended.actionhandlersample.action.ShowToastAction;
import com.drextended.actionhandlersample.action.SimpleAnimationAction;
import com.drextended.actionhandlersample.action.TrackAction;

public class MainActivity extends AppCompatActivity implements OnActionFiredListener, ActionInterceptor, OnActionErrorListener, OnActionDismissListener {

    private static final String EXTRA_LAST_ACTION_TEXT = "EXTRA_LAST_ACTION_TEXT";

    private TextView mLabelView;
    private ActionHandler mActionHandler;
    private int mClickCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ShowToastAction showToastAction = new ShowToastAction();
        mActionHandler = new ActionHandler.Builder()
                .addAction(null, new SimpleAnimationAction()) // Applied for any actionType
                .addAction(null, new TrackAction()) // Applied for any actionType
                .addAction(ActionType.OPEN_NEW_SCREEN, new OpenSecondActivity())
//                .addAction(ActionType.OPEN_NEW_SCREEN, IntentAction.from(SecondActivity.getIntent(this, null)))
                .addAction(ActionType.FIRE_ACTION, showToastAction)
                .addAction(ActionType.FIRE_DIALOG_ACTION, DialogAction.wrap(getString(R.string.action_dialog_message), showToastAction))
                .addAction(ActionType.FIRE_REQUEST_ACTION, new SampleRequestAction())
                .addAction(ActionType.FIRE_COMPOSITE_ACTION,
                        new CompositeAction<>((ctx, model) -> "Title (" + model + ")",
                                new ActionItem(ActionType.OPEN_NEW_SCREEN, new OpenSecondActivity(), R.drawable.ic_touch_app_black_24dp, 0, R.string.fire_intent_action),
                                new ActionItem(ActionType.FIRE_ACTION, showToastAction, R.drawable.ic_announcement_black_24dp, R.color.greenLight, R.string.fire_simple_action),
                                new ActionItem(ActionType.FIRE_DIALOG_ACTION, DialogAction.wrap(getString(R.string.action_dialog_message), showToastAction), R.drawable.ic_announcement_black_24dp, R.color.amber, R.string.fire_dialog_action),
                                new ActionItem(ActionType.FIRE_REQUEST_ACTION, new SampleRequestAction(), R.drawable.ic_cloud_upload_black_24dp, R.color.red, R.string.fire_request_action)
                        ))
                .addActionInterceptor(this)
                .addActionFiredListener(this)
                .addActionErrorListener(this)
                .addActionDismissListener(this)
                .build();

        initView();
    }

    private void initView() {
        mLabelView = findViewById(R.id.label);

        findViewById(R.id.button1).setOnClickListener(
                v -> mActionHandler.onActionClick(v, ActionType.OPEN_NEW_SCREEN, getSampleModel(), null)
        );

        findViewById(R.id.button2).setOnClickListener(
                new ViewOnActionClickListener(mActionHandler, getSampleModel(), ActionType.FIRE_ACTION)
        );

        findViewById(R.id.button3).setOnClickListener(
                new ViewOnActionClickListener(mActionHandler, getSampleModel(), ActionType.FIRE_DIALOG_ACTION)
        );

        findViewById(R.id.button4).setOnClickListener(
                new ViewOnActionClickListener(mActionHandler, getSampleModel(), ActionType.FIRE_REQUEST_ACTION)
        );

        findViewById(R.id.button5).setOnClickListener(
                new ViewOnActionClickListener(mActionHandler, getSampleModel(), ActionType.FIRE_COMPOSITE_ACTION)
        );

        findViewById(R.id.button5).setOnLongClickListener(
                new ViewOnActionClickListener(mActionHandler, getSampleModel(), ActionType.FIRE_DIALOG_ACTION)
        );
    }

    @NonNull
    private String getSampleModel() {
        return "Time (" + System.currentTimeMillis() + ")";
    }

    @Override
    public boolean onInterceptAction(@NonNull ActionParams params) {
        switch (params.actionType) {
            case ActionType.OPEN_NEW_SCREEN:
                final boolean consumed = mClickCount++ % 7 == 0;
                if (consumed) {
                    Toast.makeText(getApplicationContext(), R.string.message_action_intercepted, Toast.LENGTH_SHORT).show();
                }
                return consumed;
//            case ActionType.FIRE_ACTION:
//            case ActionType.FIRE_DIALOG_ACTION:
//            case ActionType.FIRE_REQUEST_ACTION:
        }
        return false;
    }

    @Override
    public void onActionFired(@NonNull ActionArgs args, @Nullable Object result) {
        if (args.fireActionType == null) return;
        switch (args.fireActionType) {
            case ActionType.OPEN_NEW_SCREEN:
                setLastActionText("Intent Action");
                break;
            case ActionType.FIRE_ACTION:
                setLastActionText("Simple Action");
                break;
            case ActionType.FIRE_DIALOG_ACTION:
                setLastActionText("Dialog Action");
                break;
            case ActionType.FIRE_REQUEST_ACTION:
                setLastActionText("Request Action");
                break;
        }
    }

    @Override
    public void onActionError(@NonNull ActionArgs args, @Nullable Throwable throwable) {
        Toast.makeText(
                this,
                throwable == null ? "Error" : throwable.getMessage(),
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onActionDismiss(@NonNull ActionArgs args, @Nullable String reason) {
        Toast.makeText(this, "Action dismissed. Reason: " + reason, Toast.LENGTH_SHORT).show();
    }

    private void setLastActionText(String label) {
        mLabelView.setText(getString(R.string.last_action_label, label));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_LAST_ACTION_TEXT, mLabelView.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        setLastActionText(savedInstanceState.getString(EXTRA_LAST_ACTION_TEXT));
    }

    @Override
    protected void onDestroy() {
        mActionHandler.cancelAll();
        super.onDestroy();
    }
}
