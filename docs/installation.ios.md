# iOS Installation

There are multiple ways to install RNCameraRoll dependent on how your project is currently setup:

### 1) Existing Cocoapods setup, including React Native as a pod
Simply add the following to your `Podfile`:

```ruby
pod 'RNCameraRoll', :path => '../node_modules/react-native-cameraroll'
```

### 2) Automatically with react-native-cli
React native ships with a `link` command that can be used to link the projects together, which can help automate the process of linking our package environments.

```bash
react-native link react-native-cameraroll
```

### 3) Manually

If you prefer not to use `react-native link`, we can manually link the package together with the following steps, after `npm install`:

**A.** In XCode, right click on `Libraries` and find the `Add Files to [project name]`.

**B.** Add the `node_modules/react-native-cameraroll/ios/RNCameraRoll.xcodeproj`
