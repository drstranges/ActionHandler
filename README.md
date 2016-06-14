# ActionHandler

[![Release](https://img.shields.io/badge/jcenter-0.1.1-blue.svg)](https://bintray.com/drstranges/android-extended/action-handler)

## Overview

This library intended to allow easy manage action handling in android projects.
Just collect actions in a handler and bind them to the views.

Add library as gradle dependency

```gradle
repositories { 
    jcenter()
}
dependencies {
    compile 'com.drextended.actionhandler:action-handler:0.1.1'
}
```

## Example

*MainActivity.java**
```java
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
                        ))
                .setActionInterceptor(this)
                .setActionFiredListener(this)
                .build();
                
    ...
    // and then on view Click            
    mActionHandler.onActionClick(v, ActionType.OPEN_NEW_SCREEN, getSampleModel());
```
or with Data Binding

**item_user.xml**
``` xml
<layout ...>
    <data>
        <variable
            name="user"
            type="your.package.User"/>

        <variable
            name="actionHandler"
            type="com.drextended.actionhandler.listener.ActionClickListener"/>
    </data>

    <FrameLayout ...
        app:actionHandler="@{actionHandler}"
        app:actionType="@{ActionType.SHOW_PROFILE}"
        app:actionTypeLongClick="@{ActionType.MENU}"
        app:model="@{user}">

        <!-- Other Views -->
    </FrameLayout>

</layout>
```
