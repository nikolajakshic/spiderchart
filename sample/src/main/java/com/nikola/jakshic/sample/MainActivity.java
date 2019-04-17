package com.nikola.jakshic.sample;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.nikola.jakshic.spiderchart.SpiderChart;
import com.nikola.jakshic.spiderchart.SpiderData;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpiderChart spiderChart = findViewById(R.id.spider_chart);

        float[] values1 = {45f, 82f, 76f, 55f, 55f, 55f};
        float[] values2 = {85f, 72f, 41f, 75f, 75f, 75f};

        int color1 = Color.argb(125, 193, 230, 219);
        int color2 = Color.argb(125, 209, 217, 234);

        SpiderData data1 = new SpiderData(values1, color1);
        SpiderData data2 = new SpiderData(values2, color2);

        List<String> labels = Arrays.asList("ART0", "ART1", "ART2", "ART3", "ART4", "ART5");
        List<SpiderData> data = Arrays.asList(data1, data2);

        spiderChart.setLabelSize(13f);
        spiderChart.setLabelColor(Color.GRAY);
        spiderChart.setLabelMarginSize(10);
        spiderChart.setWebColor(Color.GRAY);
        spiderChart.setWebBackgroundColor(Color.WHITE);
        spiderChart.setWebStrokeWidth(1);
        spiderChart.setWebEdgeColor(Color.BLACK);
        spiderChart.setWebEdgeStrokeWidth(1.5f);
        spiderChart.setRotationAngle(90f);

        spiderChart.setLabels(labels);
        spiderChart.setData(data);

        spiderChart.refresh(); // apply changes
    }
}