# SectionCircleBar

![screenshot of sample](https://github.com/alexnik0888/SectionCircleBar/blob/master/app/sectionbar.gif)

## Getting Started

### Installing

Add to your app build.gradle file next lines

```
android {
    ...
    
    repositories {
        maven { url "https://jitpack.io" }
    }

```

And then

```
dependencies {
  ...

  implementation 'com.github.alexnik0888:SectionCircleBar:v1.0'
 }
```

### Usage

How to use from xml. Note that all paddings mast be equals

```
<com.maiboroda.o.sectioncirclebar.SectionCircleBar
        android:id="@+id/wheel"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="32dp"
        app:pointerDrawable="@drawable/emotion_pointer"
        app:previewText="\?"
        app:previewTextColor="@color/alpha_white"
        app:previewTextSize="96sp"
        app:rimColor="@color/alpha_white"
        app:rimWidth="24dp"
        app:sectionColor="@color/colorAccent"
        app:sectionColors="@array/wheel"
        app:sectionCount="7"
        app:textColor="@android:color/white"
        app:textList="@array/emowheel"
        app:textSize="40sp" />
```
And from code

```
wheel.setListener { section -> Log.d(TAG, section.toString()) }
```
