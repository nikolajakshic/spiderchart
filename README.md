# SpiderChart

An Android library that makes it easy to visualize your data.

[![](https://jitpack.io/v/nikolajakshic/spiderchart.svg)](https://jitpack.io/#nikolajakshic/spiderchart)

<img src="https://github.com/nikolajakshic/spiderchart/blob/master/art/art1.png" width="300">  

## Download

In your project's top-level `build.gradle` file, ensure that JitPack's Maven repository is included:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Then, in your app-level `build.gradle` file, declare `SpiderChart` as a dependency:

```groovy
dependencies {
    implementation 'com.github.nikolajakshic:spiderchart:1.0.0'
}
```

## Usage

Define it in the layout file:

```xml
<com.nikola.jakshic.spiderchart.SpiderChart
    android:id="@+id/spider_chart"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

Then capture an instance from the layout:

```java
SpiderChart spiderChart = findViewById(R.id.spider_chart);
```
Add data:

```java
float[] values1 = {45f, 82f, 76f, 55f, 55f, 55f};
float[] values2 = {85f, 72f, 41f, 75f, 75f, 75f};

int color1 = Color.argb(125, 193, 230, 219);
int color2 = Color.argb(125, 209, 217, 234);

SpiderData data1 = new SpiderData(values1, color1);
SpiderData data2 = new SpiderData(values2, color2);

List<String> labels = Arrays.asList("ART0", "ART1", "ART2", "ART3", "ART4", "ART5");
List<SpiderData> data = Arrays.asList(data1, data2);

spiderChart.setLabels(labels);
spiderChart.setData(data);

spiderChart.refresh(); // apply changes
```

Use styling functions:

```java
spiderChart.setLabelSize(13f);
spiderChart.setLabelColor(Color.GRAY);
spiderChart.setLabelMarginSize(10);
spiderChart.setWebColor(Color.GRAY);
spiderChart.setWebBackgroundColor(Color.WHITE);
spiderChart.setWebStrokeWidth(1);
spiderChart.setWebEdgeColor(Color.BLACK);
spiderChart.setWebEdgeStrokeWidth(1.5f);
spiderChart.setRotationAngle(90f);

spiderChart.refresh(); // apply changes
```

## License

```
MIT License

Copyright (c) 2019 Nikola Jakšić

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
