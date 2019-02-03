package com.otaliastudios.zoom.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

import java.util.Random;


public class ColorGridView extends GridLayout {

    private final static int ROWS = 25;
    private final static int COLS = 10;
    private final static Random R = new Random();

    private StaticLayout mText;

    public ColorGridView(@NonNull Context context) {
        this(context, null);
    }

    public ColorGridView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorGridView(@NonNull final Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        setRowCount(ROWS);
        setColumnCount(COLS);
        for (int row = 0; row < ROWS; row++) {
            Spec rowSpec = spec(row);
            for (int col = 0; col < COLS; col++) {
                Spec colSpec = spec(col);
                LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
                params.width = 250;
                params.height = 150;
                View view = createView(context);
                addView(view, params);
            }
        }
    }

    private static View createView(final Context context) {
        View view = new View(context);
        //final int r = 200 + R.nextInt(55);
        //final int g = 100 + R.nextInt(100);
        //final int b = 50 + R.nextInt(100);
        final int status = R.nextInt(100);
        int color;
        if (status > 70) {
            color = Color.parseColor("#80FF0000");
        }
        else{
            color = Color.parseColor("#80008000");
        }
        view.setBackground(new ColorDrawable(color));
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setBackgroundColor(Color.YELLOW);
                showDialog(context);
            }
        });
        return view;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mText == null) {
            TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(150f * (float) ROWS / 10f);
            mText = new StaticLayout("I Don't Know What I'm Doing",
                    paint, getWidth(), Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        }
        canvas.save();
        canvas.translate(getWidth() / 2f, (getHeight() - mText.getHeight()) / 2f);
        mText.draw(canvas);
        canvas.restore();
    }

    private static void showDialog(final Context context){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Parking Spot #");
        alert.setMessage("show some information here");
        alert.setPositiveButton("Save Spot", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "Spot Saved", Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        alert.create().show();
    }
}
