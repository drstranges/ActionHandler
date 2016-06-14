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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.drextended.actionhandler.ActionHandler;
import com.drextended.actionhandler.action.CompositeAction;
import com.drextended.actionhandler.action.CompositeAction.ActionItem;
import com.drextended.actionhandler.action.DialogAction;
import com.drextended.actionhandler.listener.ActionInterceptor;
import com.drextended.actionhandler.listener.OnActionFiredListener;
import com.drextended.actionhandlersample.ActionType;
import com.drextended.actionhandlersample.R;
import com.drextended.actionhandlersample.action.OpenSecondActivity;
import com.drextended.actionhandlersample.action.SampleRequestAction;
import com.drextended.actionhandlersample.action.ShowToastAction;
import com.drextended.actionhandlersample.action.SimpleAnimationAction;
import com.drextended.actionhandlersample.action.TrackAction;

public class MainActivity extends AppCompatActivity implements OnActionFiredListener, ActionInterceptor {

    private static final String EXTRA_LAST_ACTION_TEXT = "EXTRA_LAST_ACTION_TEXT";

    private TextView mLabelView;
    private ActionHandler mActionHandler;
    private int mClickCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActionHandler = new ActionHandler.Builder()
                .addAction(null, new SimpleAnimationAction()) // Applied for any actionType
                .addAction(null, new TrackAction()) // Applied for any actionType
                .addAction(ActionType.OPEN_NEW_SCREEN, new OpenSecondActivity())
                .addAction(ActionType.FIRE_ACTION, new ShowToastAction())
                .addAction(ActionType.FIRE_DIALOG_ACTION, DialogAction.wrap(getString(R.string.action_dialog_message), new ShowToastAction()))
                .addAction(ActionType.FIRE_REQUEST_ACTION, new SampleRequestAction())
                .addAction(ActionType.FIRE_COMPOSITE_ACTION,
                        new CompositeAction<String>(new CompositeAction.TitleProvider<String>() {
                            @Override
                            public String getTitle(Context context, String model) {
                                return "Title (" + model + ")";
                            }
                        },
                                new ActionItem(ActionType.OPEN_NEW_SCREEN, new OpenSecondActivity(), R.string.fire_intent_action),
                                new ActionItem(ActionType.FIRE_ACTION, new ShowToastAction(), R.string.fire_simple_action),
                                new ActionItem(ActionType.FIRE_DIALOG_ACTION, DialogAction.wrap(getString(R.string.action_dialog_message), new ShowToastAction()), R.string.fire_dialog_action),
                                new ActionItem(ActionType.FIRE_REQUEST_ACTION, new SampleRequestAction(), R.string.fire_request_action)
                        ))
                .setActionInterceptor(this)
                .setActionFiredListener(this)
                .build();

        initView();
    }

    @SuppressWarnings("ConstantConditions")
    private void initView() {
        mLabelView = (TextView) findViewById(R.id.label);

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionHandler.onActionClick(v, ActionType.OPEN_NEW_SCREEN, getSampleModel());
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionHandler.onActionClick(v, ActionType.FIRE_ACTION, getSampleModel());
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionHandler.onActionClick(v, ActionType.FIRE_DIALOG_ACTION, getSampleModel());
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionHandler.onActionClick(v, ActionType.FIRE_REQUEST_ACTION, getSampleModel());
            }
        });

        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionHandler.onActionClick(v, ActionType.FIRE_COMPOSITE_ACTION, getSampleModel());
            }
        });
    }

    @NonNull
    private String getSampleModel() {
        return "Time (" + System.currentTimeMillis() + ")";
    }

    @Override
    public boolean onInterceptAction(Context context, View view, String actionType, Object model) {
        switch (actionType) {
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
    public void onClickActionFired(String actionType, Object model) {
        switch (actionType) {
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
}
