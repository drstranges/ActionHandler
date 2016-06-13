# ActionHandler
This library intended to allow you easy manage action handling in your android project. Just collect all actions in a handler and bind it to the views.

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
                
    mActionHandler.onActionClick(v, ActionType.OPEN_NEW_SCREEN, getSampleModel());
