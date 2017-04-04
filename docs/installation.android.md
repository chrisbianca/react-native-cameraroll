# Android Installation

The simplest way of installing on Android is to use the react-native link CLI command & rebuild the project:

```
react-native link react-native-cameraroll
```

## Manually

To install `react-native-cameraroll` manually in our project, we'll need to import the package from `com.chrisbianca.cameraroll` in our project's `android/app/src/main/java/com/[app name]/MainApplication.java` and list it as a package for ReactNative in the `getPackages()` function:

```java
package com.youcompany.application;
// ...
import com.chrisbianca.cameraroll.RNCameraRollPackage;
// ...
public class MainApplication extends Application implements ReactApplication {
    // ...

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new RNCameraRollPackage()  // <-- Add this line
      );
    }
  };
  // ...
}
```

We'll also need to list it in our `android/app/build.gradle` file as a dependency that we want React Native to compile. In the `dependencies` listing, add the `compile` line:

```java
dependencies {
  compile project(':react-native-cameraroll')
}
```

Add the project path to `android/settings.gradle`:

```
include ':react-native-cameraroll'
project(':react-native-cameraroll').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-cameraroll/android')
```
