# ActionHandler

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-ActionHandler-green.svg?style=true)](https://android-arsenal.com/details/1/3841)
[![Download](https://api.bintray.com/packages/drstranges/android-extended/action-handler/images/download.svg)](https://bintray.com/drstranges/android-extended/action-handler/_latestVersion)

## Overview

This library intended to simplify the work with action handling in android projects.
Just collect actions in a handler and bind them to views.

Add library as gradle dependency

```gradle
repositories { 
    jcenter()
}
dependencies {
    //implement 'com.drextended.actionhandler:actionhandler:1.2.0' // <= compiled with android.databinding.enableV2=false
    implement 'com.drextended.actionhandler:actionhandler:2.1.0' // <= compiled with android.databinding.enableV2=true
    //implementation "com.android.support:appcompat-v7:27.1.1"
    //implementation "io.reactivex.rxjava2:rxandroid:2.0.1"
    //implementation "io.reactivex.rxjava2:rxjava:2.1.8"
}
```

## Features
- `.IntentAction` - Action with Intent: start activity, satrt service, send broadcast.
- `.DialogAction` - Aaction which shows simple dialog before it fired.
- `.RequestAction` - Simple action which makes network request.
- `.RxRequestAction` - Simple action which makes network requests with RxJava observable calls.
- `.CompositeAction` - Composite action which can contain other actions inside and shows simple menu to choose one of them when fired.
- Any custom actions...

## Usage

**MainActivity.java**
```java
    mActionHandler = new ActionHandler.Builder()
                .addAction(null, new SimpleAnimationAction()) // Applied for any actionType
                .addAction(null, new TrackAction()) // Applied for any actionType
                .addAction(ActionType.OPEN_NEW_SCREEN, new OpenSecondActivity())
                .addAction(ActionType.FIRE_ACTION, new ShowToastAction())
                .addAction(ActionType.FIRE_DIALOG_ACTION, DialogAction.wrap(getString(R.string.action_dialog_message), new ShowToastAction()))
                .addAction(ActionType.FIRE_REQUEST_ACTION, new SampleRequestAction())
                .addAction(ActionType.MENU,
                        new CompositeAction<String>(new CompositeAction.TitleProvider<String>() {
                            @Override
                            public String getTitle(Context context, String model) {
                                return "Title (" + model + ")";
                            }
                        },
                                new ActionItem(ActionType.OPEN_NEW_SCREEN, new OpenSecondActivity(), R.string.menu_item_1),
                                new ActionItem(ActionType.FIRE_ACTION, new ShowToastAction(), R.drawable.icon, R.color.tint, R.string.menu_item_2),
                        ))
                .addActionInterceptor(this)
                .addActionFiredListener(this)
                .addActionErrorListener(this)
                .addActionDismissListener(this)
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
        app:model="@{user}"
        app:modelLongClick="@{user}">
        <!-- modelLongClick is optional, will be used model if null -->
        
        <!-- Other Views -->
    </FrameLayout>

</layout>
```
**Note:** RequestAction and RxRequestAction can show simple progress dialog. By default they use ProgressBarController, which should be initialized with Application instance to avoid WindowLeaked Errors.

```
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ProgressBarController.init(this);
    }
}
```
or
```
// Somewhere before first usage
ProgressBarController.init((Application) getApplicationContext());
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
