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

package com.drextended.actionhandler;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drextended.actionhandler.util.ProgressBarController;
import com.drextended.actionhandler.util.AUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ActionParams {

    @NonNull
    public final Context appContext;
    @NonNull
    public final WeakReference<View> weakView;
    @NonNull
    public final String actionType;
    @Nullable
    public final Object model;
    @Nullable
    public final Object tag;
    @Nullable
    private Map<Object, Object> payload;

    public ActionParams(
            @NonNull Context context,
            @Nullable View clickView,
            @NonNull String actionType,
            @Nullable Object model,
            @Nullable Object actionTag
    ) {
        this.appContext = context.getApplicationContext();
        this.weakView = new WeakReference<>(clickView);
        this.actionType = actionType;
        this.model = model;
        this.tag = actionTag;
    }

    @NonNull
    public Object requireModel() {
        if (model == null) {
            throw new IllegalStateException("model is null");
        }
        return model;
    }

    @Nullable
    public View tryGetView() {
        return weakView.get();
    }

    @NonNull
    public Context getViewOrAppContext() {
        View view = weakView.get();
        if (view != null) {
            Context viewContext = view.getContext();
            if (viewContext != null) {
                Activity activity = AUtils.getActivity(viewContext);
                if (activity == null || !(activity.isFinishing() || activity.isDestroyed())) {
                    return viewContext;
                }
            }
        }
        return appContext;
    }

    @Nullable
    public Activity tryGetActivity() {
        View view = weakView.get();
        if (view != null) {
            return AUtils.getActivity(view.getContext());
        }
        return null;
    }

    @Nullable
    public <T> T getTag(@NonNull Class<T> clazz) {
        if (clazz.isInstance(tag)) {
            //noinspection unchecked
            return (T) tag;
        }
        return null;
    }

    @Nullable
    public Map<Object, Object> getPayload() {
        return payload;
    }

    public void setPayload(@Nullable Map<Object, Object> payload) {
        this.payload = payload;
    }

    public void putPayload(@NonNull Object key, @NonNull String value) {
        requirePayload().put(key, value);
    }

    public void removePayload(@NonNull Object key) {
        if (payload != null) payload.remove(key);
    }

    @Nullable
    public Object getPayload(@NonNull Object key) {
        if (payload != null) {
            return payload.get(key);
        }
        return null;
    }

    @Nullable
    public <T> T getPayload(@NonNull Object key, @NonNull Class<T> clazz) {
        return getPayloadInternal(key, clazz, null);
    }

    @NonNull
    public <T> T getPayload(@NonNull Object key, @NonNull Class<T> clazz, @NonNull T defaultValue) {
        //noinspection ConstantConditions
        return getPayloadInternal(key, clazz, defaultValue);
    }

    @Nullable
    private <T> T getPayloadInternal(@NonNull Object key, @NonNull Class<T> clazz, @Nullable T defaultValue) {
        if (payload != null) {
            Object value = payload.get(key);
            if (value != null && clazz.isInstance(value)) {
                //noinspection unchecked
                return (T) value;
            }
        }
        return defaultValue;
    }

    @NonNull
    public Map<Object, Object> requirePayload() {
        if (payload == null) {
            payload = new HashMap<>();
        }
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionParams that = (ActionParams) o;
        return actionType.equals(that.actionType) &&
                Objects.equals(model, that.model) &&
                Objects.equals(tag, that.tag) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionType, model, tag);
    }

    @Override
    @NonNull
    public String toString() {
        return "ActionParams{" +
                "actionType='" + actionType + '\'' +
                ", model=" + model +
                ", tag=" + tag +
                ", payload=" + payload +
                '}';
    }

    public <T> T getModel(Class<T> expectedClass) {
        if (model != null && expectedClass.isInstance(expectedClass)) {
            //noinspection unchecked
            return (T) model;
        }
        return null;
    }
}
