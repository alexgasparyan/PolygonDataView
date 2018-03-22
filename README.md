# PolygonDataView

Simple android `View` which draws charts based on given data points. The view is written in `Kotlin` however it can be used in `Java` if `Kotlin` is configured in the project. 

![image](https://raw.githubusercontent.com/alexgasparyan/PolygonDataView/master/sample.gif)  

## Advandatges ##
* Extremely simple
* Lightweight
* Dynamic for any kind/size of data

## Usage ##

Add dependency in app module gradle file:

```gradle
implementation 'com.armdroid:polygondataview:1.0.0'
```

Add the view in your `Activity` layout:
 
 ```xml
 <com.armdroid.polygondataview.PolygonDataView
         android:id="@+id/polygon_view"
         android:layout_width="match_parent"
         android:layout_height="120dp"
         polygon:vertexType="sharp"
         polygon:color="@color/colorLightGray"
         polygon:strokeWidth="0dp"
         polygon:highlightColor="@color/colorLightBlue"
         polygon:highlightStrokeColor="@color/colorDarkBlue"
         polygon:highlightStrokeWidth="0dp" />
```

* There are two values for `polygon:vertexType`
    * *sharp* - straight lines between data points
    * *curved* - curved lines between data points
* `polygon:color` sets the background of chart. `polygon:highlightColor` sets the background of filter chart if some data is highlighted   
* `polygon:filterStrokeWidth` and `polygon:highlightStrokeWidth` set the width of strokes of polygons. Default value for stroke parameters is `0f` i.e. no stroke
* All attributes can be supplied dynamically:

```kotlin
    polygonDataView.setColor(Color.BLUE)
    polygonDataView.setHighlightStrokeWidth(3f)
```

Set data points to `PolygonDataView` as a `List` of `kotlin.Pair<Float, Float>`. If using `Kotlin`, you can use typealias `Coordinate` as a substitute.
 ```kotlin
 override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_main)
         
         val polygonDataView = findViewById<PolygonDataView>(R.id.polygon_data_view)
         
         val dataSet = listOf(1f to 5f, 5f to 12f, 8f to 7f, 15f to 18f)
         polygonDataView.setData(dataSet)
    
         //lower and upper bounds of x axis. Note that data points out of this range will be left out
         polygonDataView.setRange(5f, 20f)
     
         //highlight the segment of chart between these bounds
         polygonDataView.setHighlightRange(8f, 12f)
     }
```
If you want to remove the highlight simply call `polygonDataView.clearHighlightRange()`

That's it. Enjoy!



