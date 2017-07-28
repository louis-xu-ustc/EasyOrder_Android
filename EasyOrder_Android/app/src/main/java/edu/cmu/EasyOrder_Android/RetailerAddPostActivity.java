package edu.cmu.EasyOrder_Android;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RetailerAddPostActivity extends AppCompatActivity {
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int RESULT_LOAD_IMAGE = 2;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private String mCurrentPhotoPath;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private ImageView imageView;
    private EditText dishName, dishPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retailer_add_post);

        dishName = (EditText) findViewById(R.id.retailer_add_post_dish_name);
        dishPrice = (EditText) findViewById(R.id.retailer_add_post_dish_price);
        imageView = (ImageView) findViewById(R.id.retailer_add_post_dish_image);

        Button findPhotoButton = (Button) findViewById(R.id.retailer_add_post_find_photo_button);
        findPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //only access to top package of photos taken by Camera
                Intent gallery = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                //In order to access all documents
                //Intent gallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                //gallery.addCategory(Intent.CATEGORY_OPENABLE);
                gallery.setType("image/*");
                startActivityForResult(gallery, RESULT_LOAD_IMAGE);
            }
        });
        Button takePhotoButton = (Button) findViewById(R.id.retailer_add_post_take_photo_button);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                                5);
                    }
                }
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        Button saveDishButton = (Button) findViewById(R.id.retailer_add_post_dish_save);
        saveDishButton.setOnClickListener(saveDishButtonClicked);
    }

    View.OnClickListener saveDishButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Context context = getApplicationContext();

            String name = dishName.getText().toString();
            String priceStr = dishPrice.getText().toString();
            double price;
            if (name.trim().equals("")) {
                Toast.makeText(getApplicationContext(), "Invalid dish name specified!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (priceStr.trim().equals("")) {
                Toast.makeText(getApplicationContext(), "Invalid dish price specified!", Toast.LENGTH_SHORT).show();
                return;
            }
            price = Double.parseDouble(priceStr);

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            if (drawable == null) {
                Toast.makeText(getApplicationContext(), "Invalid dish image specified!", Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap dishImage = drawable.getBitmap();
            if (dishImage == null) {
                Toast.makeText(getApplicationContext(), "Invalid dish image specified!", Toast.LENGTH_SHORT).show();
                return;
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            dishImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

            postNewDish(name, price, encodedImage);
            setResult(Activity.RESULT_OK);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            verifyStoragePermissions(RetailerAddPostActivity.this);
            /*
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
            String imageFileName = "IMG_" + timeStamp + "_"+".jpg";
            //MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            Uri mediauri = MediaStore.Images.Media.getContentUri("EXTERNAL_CONTENT_URI");
            grantUriPermission("edu.cmu.camera",mediauri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap, imageFileName, "");
             */
            // also SAVE the image to gallery
            if (mCurrentPhotoPath != null) {
                Log.d("lets add gallery", mCurrentPhotoPath);
                try {
                    File f2 = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //galleryAddPic(); either this one or following
                //mCurrentPhotoPath needs to be set from invoking galleryIntent
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.DATA, mCurrentPhotoPath);
                getBaseContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                mCurrentPhotoPath = null;
            }
        }
        // result of find photo
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bmp = null;

            try {
                bmp = getBitmapFromUri(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bmp != null) {
                imageView.setImageBitmap(bmp);
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }


    //Checking persmission
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 5) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera
            } else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
            }
        }
    }


    private File setUpPhotoFile() throws IOException {
        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();
        return f;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),IMAGE_DIRECTORY_NAME);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void postNewDish(String name, double price, String photo) {
        JSONObject dish = new JSONObject();
        try {
            dish.put("name", name);
            dish.put("price", price);
            dish.put("photo", photo);
        } catch (JSONException eJson) {
            Log.d("Retailer post dish", "json input parse error");
        }

        Response.Listener<JSONObject> dishCallback = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(), "Dish Post Succeeded", Toast.LENGTH_SHORT).show();
                // finish this activity after getting response
                finish();
            }
        };

        RESTAPI.getInstance(getApplicationContext())
                .makeRequest(Utils.API_BASE + "/dish/",
                        Request.Method.POST,
                        dish,
                        dishCallback,
                        null);
    }
}