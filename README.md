# ActionHandler

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-ActionHandler-green.svg?style=true)](https://android-arsenal.com/details/1/3841)
[![Release](https://img.shields.io/badge/jcenter-0.1.13-blue.svg)](https://bintray.com/drstranges/android-extended/action-handler)

## Overview

This library intended to simplify the work with action handling in android projects.
Just collect actions in a handler and bind them to views.

Add library as gradle dependency

```gradle
repositories { 
    jcenter()
}
dependencies {
    compile 'com.drextended.actionhandler:actionhandler:0.1.13'
}
```

## Features
- `.IntentAction` - Action with Intent: start activity, satrt service, send broadcast.
- `.DialogAction` - Aaction which shows simple dialog before it fired.
- `.RequestAction` - Simple action which makes network request.
- `.CompositeAction` - Composite action which can contain other actions inside and shows simple menu to choose one of them when fired.
- Any custom actions...

## Example

**MainActivity.java**
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
    // and then on view click
    mActionHandler.onActionClick(view, ActionType.OPEN_NEW_SCREEN, getSampleModel());
```
with Data Binding

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
License
=======

    Copyright 2016 Roman Donchenko

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
