package cs3205.subsystem3.health.ui.camera;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import cs3205.subsystem3.health.R;
import cs3205.subsystem3.health.common.utilities.SessionManager;
import cs3205.subsystem3.health.common.utilities.UploadHandler;
import cs3205.subsystem3.health.data.source.remote.RemoteDataSource;


public class UploadPageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_LOAD_VIDEO = 2;


    private ImageView imageToUpload, videoToUpload;
    private VideoView videoToPreview;
    private Button bUploadImage, bUploadVideo;
    private TextView uploadImageName, uploadVideoName;

    private String selectedPath = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_page);

        imageToUpload = (ImageView) findViewById(R.id.imageToUpload);
        bUploadImage = (Button) findViewById(R.id.buttonUploadImage);
        uploadImageName = (TextView) findViewById(R.id.imageName);

        videoToUpload = (ImageView) findViewById(R.id.videoToUpload);
        bUploadVideo = (Button) findViewById(R.id.buttonUploadVideo);
        uploadVideoName = (TextView) findViewById(R.id.videoName);
        videoToPreview = (VideoView) findViewById(R.id.videoToPreview);

        imageToUpload.setOnClickListener(this);
        videoToUpload.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);
        bUploadVideo.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionManager.isTimerSet()) {
            SessionManager.cancelTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SessionManager.isTimerSet()) {
            SessionManager.resetTimer(this);
        } else {
            SessionManager.setTimer(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageToUpload:
                Intent imageGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imageGalleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(imageGalleryIntent,"Select Picture"), RESULT_LOAD_IMAGE);
                break;
            case R.id.buttonUploadImage:
                UploadHandler imageUploader = new UploadHandler(selectedPath, RemoteDataSource.Type.IMAGE, this);
                if(selectedPath==null){
                    break;
                }else {
                    imageUploader.startUpload();
                    break;
                }
            case R.id.videoToUpload:
                Intent VideoGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(VideoGalleryIntent, RESULT_LOAD_VIDEO);
                break;
            case R.id.buttonUploadVideo:
                UploadHandler videoUploader = new UploadHandler(selectedPath, RemoteDataSource.Type.VIDEO,this);
                if(selectedPath==null){
                    break;
                }
                else {
                    videoUploader.startUpload();
                    break;
                }


        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            selectedPath = getPath(this, selectedImageUri);
            uploadImageName.setText(selectedPath);
            imageToUpload.setImageURI(selectedImageUri);


        } else if (requestCode == RESULT_LOAD_VIDEO && resultCode == RESULT_OK && data != null) {
            Uri selectedVideoUri = data.getData();
            selectedPath = getPath(this, selectedVideoUri);
            uploadVideoName.setText(selectedPath);
            videoToUpload.setVisibility(View.GONE);
            videoToPreview.setVideoURI(selectedVideoUri);
            videoToPreview.setVisibility(View.VISIBLE);

        }

    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
