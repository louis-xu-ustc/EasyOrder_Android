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
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.text.TextUtils.isDigitsOnly;

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
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                /* in order to save photos to App specific folder
                File photoFile = null;
               try {
                    photoFile = createImageFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    Uri photoURI = FileProvider.getUriForFile(this,
                                                  "edu.cmu.camera.fileprovider",
                                                  photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                } */
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

            CharSequence inputPrice = dishPrice.getText().toString();
            if (!isDigitsOnly(inputPrice)) {
                Toast dishPriceErr = Toast.makeText(context, "Invalid dish price!", Toast.LENGTH_SHORT);
                dishPriceErr.show();
                return;
            }
            // TODO further operation on backend API
            Toast saveButtonToast = Toast.makeText(context, "Further backend operations to save dish!", Toast.LENGTH_SHORT);
            saveButtonToast.show();
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
}