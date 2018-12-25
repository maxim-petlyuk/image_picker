# Pick images from Gallery & Camera

# Download
[![](https://jitpack.io/v/mpetlyuk/image_picker.svg)](https://jitpack.io/#mpetlyuk/image_picker)

## Gradle

#### Step 1:
Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

#### Step 2:
Add the dependency
```groovy
dependencies {
    implementation 'com.github.mpetlyuk:image_picker:$latest_version'
}
```

# Features
- encapsulated the logic with permissions. No need to write boilerplate code in your project;
- work with FileProvider is already in the library. Don`t worry about it;
- support the mode **"don`t keep activity"**
- can be used from fragment or activity

# Usage
The main class that has control over the pick process is **RxFilePicker**. 

For now it supports only a few source:
```
enum class FileSourceType {
    GALLERY,
    CAMERA
}
```

You can choose the source via the next method:
```
open class RxFilePicker {

    fun fromSource(sourceType: FileSourceType): RxFilePicker
}
```

To initiate the pick action use one of the available methods:
```
open class RxFilePicker {
    fun pickFile(activity: Activity)

    fun pickFile(fragment: Fragment)
}
```

Don`t forget to call a few lifecycle methods:
```
    ...
    private RxFilePicker mRxFilePicker = new RxFilePicker();

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mRxFilePicker.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mRxFilePicker.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mRxFilePicker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        mRxFilePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
```# Pick images from Gallery & Camera
   
   # Download
   [![](https://jitpack.io/v/mpetlyuk/image_picker.svg)](https://jitpack.io/#mpetlyuk/image_picker)
   
   ## Gradle
   
   #### Step 1:
   Add it in your root build.gradle at the end of repositories:
   ```groovy
   allprojects {
       repositories {
           maven { url 'https://jitpack.io' }
       }
   }
   ```
   
   #### Step 2:
   Add the dependency
   ```groovy
   dependencies {
       implementation 'com.github.mpetlyuk:image_picker:$latest_version'
       
       implementation 'io.reactivex.rxjava2:rxkotlin://v2+'
       implementation 'io.reactivex.rxjava2:rxandroid://v2+'
   }
   ```
   
   # Features
   - encapsulated the logic with permissions. No need to write boilerplate code in your project;
   - work with FileProvider is already in the library. Don`t worry about it;
   - support the mode **"don`t keep activity"**
   - can be used from fragment or activity
   
   # Usage
   The main class that has control over the pick process is **RxFilePicker**. 
   
   For now it supports only a few source:
   ```
   enum class FileSourceType {
       GALLERY,
       CAMERA
   }
   ```
   
   You can choose the source via the next method:
   ```
   open class RxFilePicker {
   
       fun fromSource(sourceType: FileSourceType): RxFilePicker
   }
   ```
   
   To initiate the pick action use one of the available methods:
   ```
   open class RxFilePicker {
       fun pickFile(activity: Activity)
   
       fun pickFile(fragment: Fragment)
   }
   ```
   
   Don`t forget to call a few lifecycle methods:
   ```
       ...
       private RxFilePicker mRxFilePicker = new RxFilePicker();
   
       @Override
       protected void onRestoreInstanceState(Bundle savedInstanceState) {
           super.onRestoreInstanceState(savedInstanceState);
   
           mRxFilePicker.onRestoreInstanceState(savedInstanceState);
       }
   
       @Override
       protected void onSaveInstanceState(Bundle outState) {
           super.onSaveInstanceState(outState);
   
           mRxFilePicker.onSaveInstanceState(outState);
       }
   
       @Override
       protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
           super.onActivityResult(requestCode, resultCode, data);
   
           mRxFilePicker.onActivityResult(requestCode, resultCode, data);
       }
   
       @Override
       public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
           super.onRequestPermissionsResult(requestCode, permissions, grantResults);
   
           mRxFilePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
       }
   ```
   
   The repository contains full worked example. Please, use it as sample when you want to implement the library into your project!