## 2.1.3 (2018-06-07)

* add ActionFactory for lazy instantiation actions

## 2.1.2 (2018-04-18)

* fix bug introduced in v2.1.1

## 2.1.1 (2018-04-18)

* add global callback to help you set all listeners and interceptors using just one method. Use `addCallback(callback)`.
* fix: call `interceptActionFire` with action which it was added with. 
* fix bug when listeners was called more than once if the same instance of action was added few times in different CompositeActions or DialogWrappers.

## 2.1.0 (2018-04-06)

* Update android support libs version to 27.1.1
* Recompiled with android.databinding.enableV2=true and gradle plugin 3.1.0

## 2.0.0 (2018-02-21)

* Update android support libs version to 25.4.0 and rxjava to 2.1.8
* Recompiled with android.databinding.enableV2=true

## 2.0.1 (2018-02-21)
* Fix complation for android.databinding.enableV2=true