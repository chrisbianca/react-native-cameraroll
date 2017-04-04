# React Native Camera Roll

[![npm version](https://img.shields.io/npm/v/react-native-cameraroll.svg)](https://www.npmjs.com/package/react-native-cameraroll)
[![License](https://img.shields.io/npm/l/react-native-cameraroll.svg)](/LICENSE)

**RNCameraRoll** is a replacement CameraRoll module for React Native that offers consistent behaviour across iOS and Android
<hr>

### Install
```
npm i react-native-cameraroll --save
```

#### Platform specific setup guides:
[![ios](https://a.fsdn.com/sd/topics/ios_64.png)](docs/installation.ios.md)   [![android](https://a.fsdn.com/sd/topics/android_64.png)](docs/installation.android.md)

<hr>

### Usage

`import RNCameraRoll from 'react-native-cameraroll';`

#### `getAssets(params: Object): Promise<Object>`

Retrieve image and/or video assets from the device's Camera Roll.

```javascript
RNCameraRoll.getAssets({ assetType: 'image', limit: 20 })
  .then(response => console.log(response))
  .catch(err => console.error(err));
```

`params` takes the following shape:
```
{
  assetType: 'image' | 'video' | 'all';
  limit: number; // How many assets to return
  start?: string | number; // The start cursor (use end_cursor from previous request)
}
```

The response takes the following shape:
```
{
  assets: [{
    filename: string;
    height: number;
    location: {
      altitude?: number;
      heading?: number;
      latitude: number;
      longitude: number;
      speed?: number;
    };
    timestamp: number;
    type: 'image' | 'video';
    uri: string;
    width: number;
  }],
  page_info: {
    end_cursor: string | number;
    has_next_page: boolean;
    start_cursor: string | number;
  };
}
```

<hr>

### Contributing

We welcome any contribution to the repository. Please ensure your changes to the JavaScript code follow the styling guides controlled by ESlint. Changes to native code should be kept clean and follow the standard of existing code.

<hr>

### License

- MIT
