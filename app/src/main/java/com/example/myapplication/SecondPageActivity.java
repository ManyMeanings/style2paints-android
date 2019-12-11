package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import com.dingmouren.colorpicker.ColorPickerDialog;
import com.dingmouren.colorpicker.OnColorPickerListener;

import java.io.FileNotFoundException;
import java.util.List;

public class SecondPageActivity extends AppCompatActivity {

    private Button mBtnColorPicker, mBtnPhoto,mBtnSubmit,mBtnDelete;
    private ColorPickerDialog mColorPickerDialog = null;
    private boolean supportAlpha = true;//颜色是否支持透明度
    private boolean activatedButton = false;//点击delete按钮按钮会变色并一直保持
    MyApp myApp = new MyApp();
    int ColorSave;
    int flag = 0;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_second_page);
        MyApp.reset = true; //清除颜色点
        MyApp.delete = false;
        mBtnColorPicker = findViewById(R.id.btn_change_color);
        mBtnPhoto = findViewById(R.id.btn_change_photo);
        mBtnDelete = findViewById(R.id.btn_delete);
        mBtnSubmit = findViewById(R.id.btn_submit); // 点击submit将颜色点原本的屏幕坐标转换成图片坐标
        imageView = findViewById(R.id.image);
        setListeners();
    }

    private void setListeners() {
        OnClick onClick = new OnClick();
        mBtnColorPicker.setOnClickListener(onClick);
        mBtnPhoto.setOnClickListener(onClick);
        mBtnDelete.setOnClickListener(onClick);
        mBtnSubmit.setOnClickListener(onClick);
    }

    private class OnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()) {
                case R.id.btn_change_color:
                    if (flag == 0) {
                        mColorPickerDialog = new ColorPickerDialog(SecondPageActivity.this, getResources().getColor(R.color.colorWhite, null), supportAlpha, mOnColorPickerListener).show();
                    } else {
                        ColorSave = (Integer) MysharedPreferences.getColor(SecondPageActivity.this);
                        mColorPickerDialog = new ColorPickerDialog(SecondPageActivity.this, ColorSave, supportAlpha, mOnColorPickerListener).show();
                    }
                    break;
                case R.id.btn_change_photo:
                    selectPic();
                    break;
                case R.id.btn_delete:
                    if(!activatedButton){
                        activatedButton = true;
                        mBtnDelete.setActivated(activatedButton);
                    }else {
                        activatedButton = false;
                        mBtnDelete.setActivated(activatedButton);
                    }
                    MyApp.delete = !(MyApp.delete);// 点击delete按钮后，点击要删除的圆点即可删除
                    break;

                    // 打算在submit按钮将图片传到数据库
                case R.id.btn_submit:
                    List<Circle> circles = CircleView.getCircles();
                    changeXY(circles, imageView);
                    break;
            }
        }
    }
    // 改颜色
    private OnColorPickerListener mOnColorPickerListener = new OnColorPickerListener() {
        @Override
        public void onColorCancel(ColorPickerDialog dialog) {

        }

        @Override
        public void onColorChange(ColorPickerDialog dialog, int color) {
            Boolean bool = MysharedPreferences.setColor(color, SecondPageActivity.this);
            if (bool) {
                flag = 1;
            } else {
                flag = 0;
            }
        }

        @Override
        public void onColorConfirm(ColorPickerDialog dialog, int color) {
            if (mBtnColorPicker != null) {
                myApp.setColorTips(color);
            }
        }
    };
    // 改颜色
    public static class MysharedPreferences {

        public static SharedPreferences share(Context context) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
            return sharedPreferences;
        }

        public static Object getColor(Context context) {
            return share(context).getInt("color", 0);
        }

        public static boolean setColor(int color, Context context) {
            SharedPreferences.Editor e = share(context).edit();
            e.putInt("color", color);
            Boolean bool = e.commit();
            return bool;
        }
    }

    // 将原本的屏幕坐标换成对应图片的坐标
    private void changeXY(List<Circle> circles, View view) {
        for (Circle circle : circles) {
            float eventX = circle.x;
            float eventY = circle.y;
            float[] eventXY = new float[]{eventX, eventY};
            Matrix invertMatrix = new Matrix();
            ((ImageView) view).getImageMatrix().invert(invertMatrix);
            invertMatrix.mapPoints(eventXY);
            Drawable imgDrawable = ((ImageView)view).getDrawable();
            Bitmap bitmap = ((BitmapDrawable)imgDrawable).getBitmap();
            circle.imgWidth = bitmap.getWidth();
            circle.imgHeight = bitmap.getHeight();
            // 得到圆点在图片上的比例
            circle.imgx = eventXY[0]/circle.imgWidth;
            circle.imgy = (circle.imgHeight-eventXY[1])/circle.imgHeight;
            // 将颜色值(int)转换为RGB形式
            circle.red = (circle.color & 0xff0000) >> 16;
            circle.green = (circle.color & 0x00ff00) >> 8;
            circle.blue = (circle.color & 0x0000ff);
            Log.e("xy", "x:" + circle.imgx + " y:" + circle.imgy + " red:" + circle.red + " green:" + circle.green + " blue:" + circle.blue);
        }
    }

    // 打开相册选择图片
    private void selectPic(){
        MyApp.reset = true;// 清除颜色点
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    // 将选择的图片，赋值给imageView(差不多，没仔细研究)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            Uri uri = data.getData();
            Log.e("uri",uri.toString());
            ContentResolver cr = this.getContentResolver();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                imageView.setImageBitmap(bitmap);
            }catch (FileNotFoundException e){
                Log.e("Exception",e.getMessage(),e);
            }
        }else {
            Log.i("MainActivity","operation error");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
