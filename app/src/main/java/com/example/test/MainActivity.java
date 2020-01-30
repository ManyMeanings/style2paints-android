package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import androidx.annotation.Nullable;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Window;
import android.view.WindowManager;
import com.dingmouren.colorpicker.ColorPickerDialog;
import com.dingmouren.colorpicker.OnColorPickerListener;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    String  TAG = "MainActivity";
    private Uri imageUri;
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES) // connect timeout
            .writeTimeout(5, TimeUnit.MINUTES) // write timeout
            .readTimeout(5, TimeUnit.MINUTES) // read timeout
            .build();
    String base64Str = "";
    String base64StrFront = "data:image/png;base64,";
    String roomId = "";
    String stepId = "";
    String reasultId = "";
    //String options ="{\"alpha\":0,\"points\":[[0.29776206044619474,0.8727889040258305,0,0,0,2],[0.5234600523842253,0.8322409705759309,0,0,0,2],[0.3880412572214069,0.4151765122341054,245,222,179,2],[0.27519226125239166,0.10237816847773623,245,222,179,2],[0.42064207827912253,0.14871866384905025,245,222,179,2]],\"method\":\"colorization\",\"lineColor\":[0,0,0],\"line\":false,\"hasReference\":false}";
    String options ="{\"alpha\":0,\"method\":\"colorization\",\"lineColor\":[0,0,0],\"line\":false,\"hasReference\":false";
    String points = "";
    String testImgUrl = "http://192.168.31.107:8080/rooms/Dec16H23M05S30R227/result.H23M05S41.jpg";

    private ColorPickerDialog mColorPickerDialog = null;
    private boolean supportAlpha = true;//颜色是否支持透明度
    private boolean activatedButton = false;//点击delete按钮按钮会变色并一直保持
    MyApp myApp = new MyApp();
    int ColorSave;
    int flag = 0;
    private ImageView imageView;
    private Button mBtnColorPicker, mBtnPhoto,mBtnSubmit,mBtnDelete;

    final Handler hander = new Handler();
    final Runnable showResult = new Runnable() {
        public void run() {
            Log.d(TAG, "show image_result: " + roomId + " " + reasultId);
            String result_url = "http://192.168.31.107:8080/rooms/" + roomId+ "/result." + reasultId + ".jpg";
            imageView.setImageBitmap(getURLimage(result_url));
        }
    };



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        MyApp.reset = true; //清除颜色点
        MyApp.delete = false;

        mBtnColorPicker = findViewById(R.id.btn_change_color);
        mBtnPhoto = findViewById(R.id.btn_change_photo);
        mBtnDelete = findViewById(R.id.btn_delete);
        mBtnSubmit = findViewById(R.id.btn_submit);
        imageView = findViewById(R.id.image);

        mBtnColorPicker.setOnClickListener(this);
        mBtnPhoto.setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mBtnSubmit.setOnClickListener(this);

        StrictMode.setThreadPolicy(new
                StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());



    }


    public void onClick(View v) {
        if (v.getId() == R.id.btn_change_color) {
            if (flag == 0) {
                mColorPickerDialog = new ColorPickerDialog(MainActivity.this, getResources().getColor(R.color.colorWhite, null), supportAlpha, mOnColorPickerListener).show();
            } else {
                ColorSave = (Integer) MysharedPreferences.getColor(MainActivity.this);
                mColorPickerDialog = new ColorPickerDialog(MainActivity.this, ColorSave, supportAlpha, mOnColorPickerListener).show();
            }

        }else if(v.getId() == R.id.btn_submit){
            List<Circle> circles = CircleView.getCircles();
            changeXY(circles, imageView);
            getReasult();
        }else if(v.getId() == R.id.btn_change_photo){
            chooseSketch();
        }else if(v.getId() == R.id.btn_delete){
            if(!activatedButton){
                activatedButton = true;
                mBtnDelete.setActivated(activatedButton);
            }else {
                activatedButton = false;
                mBtnDelete.setActivated(activatedButton);
            }
            MyApp.delete = !(MyApp.delete);// 点击delete按钮后，点击要删除的圆点即可删除

        }
    }

    private OnColorPickerListener mOnColorPickerListener = new OnColorPickerListener() {
        @Override
        public void onColorCancel(ColorPickerDialog dialog) {

        }

        @Override
        public void onColorChange(ColorPickerDialog dialog, int color) {
            Boolean bool = MysharedPreferences.setColor(color, MainActivity.this);
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
        points = ",\"points\":[";
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
            Log.d("xy", "x:" + circle.imgx + " y:" + circle.imgy + " red:" + circle.red + " green:" + circle.green + " blue:" + circle.blue);
            if(circle.imgx >=0 && circle.imgy>=0){
                points= points + "["+ circle.imgx +"," + circle.imgy + "," + circle.red + "," + circle.green + "," + circle.blue + ",0],";
            }
        }
        points = points.substring(0,points.length()-1);
        points = points + "]}";
        Log.d(TAG, "points: " + points);
    }


    private void chooseSketch(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.
                PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new
                    String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
        } else {
            openAlbum();
        }
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.
                        PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                        handleImageOnKitKat(data);
                    }
                break;
            default:
                break;
        }

    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.
                    getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                        imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                   // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                   // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        base64Str = imageToBase64(imagePath);
        base64Str = base64StrFront + base64Str;

        Log.d(TAG, "img's base64Str: "+ base64Str);
        Log.d(TAG, "img's length: "+ base64Str.length());

        uploadSketchWithOkHttp();
        displayImage(imagePath); // 根据图片路径显示图片

    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.
                        Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            MyApp.reset = true;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap getURLimage(String url) {
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private void getReasult(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                   // OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("room", roomId)
                            .add("step", stepId)
                            .add("reference", "none")
                            .add("options", options + points)
                            .build();
                    Request request = new Request.Builder()
                            .url("http://192.168.31.107:8080/request_result")
                            //.url("http://10.66.213.5:8080/request_result")
                            //.url("http://10.66.64.168:8080")
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    String[] temp;
                    String delimeter = "_";
                    temp = responseData.split(delimeter);
                    roomId = temp[0];
                    reasultId = temp[1];
                    hander.post(showResult);
                    Log.d(TAG, "reasult ID: "+roomId+"  "+reasultId);
                    Log.d(TAG, "getReasult() : " + responseData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void uploadSketchWithOkHttp(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("room", "new")
                            .add("step", "new")
                            .add("sketch", base64Str)
                            .add("method", "colorization")
                            .build();
                    Request request = new Request.Builder()
                            .url("http://192.168.31.107:8080/upload_sketch")
                            //.url("http://10.66.213.5:8080/upload_sketch")
                            //.url("http://10.66.64.168:8080/upload_sketch")
                            .post(requestBody)
                            .header("Content-Type","application/x-www-form-urlencoded;")
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    String[] temp;
                    String delimeter = "_";
                    temp = responseData.split(delimeter);
                    roomId = temp[0];
                    stepId = temp[1];
                    Log.d(TAG, "ID: "+roomId+"  "+stepId);
                    Log.d(TAG, "uploadSketchWithOkHttp() : " + responseData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void sendRequestWithOkHttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://192.168.31.107:8080")
                            //.url("http://10.66.213.5:8080")
                            //.url("http://10.66.64.168:8080")
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Log.d(TAG, "sendRequestWithOkHttp() : " + responseData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String imageToBase64(String path){
        Bitmap bitmap=BitmapFactory.decodeFile(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //读取图片到ByteArrayOutputStream
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos); //参数如果为100那么就不压缩
        byte[] bytes = baos.toByteArray();

        return Base64.encodeToString(bytes,Base64.NO_WRAP);

    }

}
