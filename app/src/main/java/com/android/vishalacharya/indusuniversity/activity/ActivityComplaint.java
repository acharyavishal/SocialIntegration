package com.android.vishalacharya.indusuniversity.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.LightingColorFilter;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.android.vishalacharya.indusuniversity.Manifest;
import com.android.vishalacharya.indusuniversity.R;
import com.android.vishalacharya.indusuniversity.adapters.SpinnerAdapter;
import com.android.vishalacharya.indusuniversity.beans.Complaint;
import com.android.vishalacharya.indusuniversity.beans.PostComplaint;
import com.android.vishalacharya.indusuniversity.model.SpinnerModel;
import com.android.vishalacharya.indusuniversity.preferences.PrefIndus;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mindorks.paracamera.Camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import include.NotificationUtils;

import static com.android.vishalacharya.indusuniversity.activity.BaseActivityForGoogleService.REQUEST_CHECK_SETTINGS;
import static include.Functions.*;


public class ActivityComplaint extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener,View.OnClickListener,ValueEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult> {


    protected static final String TAG = "ActivityComplaint";
    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    protected final static String KEY_LOCATION = "location";
    protected final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    // UI Widgets.
    protected LinearLayout mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;

    // Labels.
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected String mLastUpdateTimeLabel;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    private Toolbar toolbar;
    private Spinner spinner;
    public  ArrayList<SpinnerModel> spinnerModelList=new ArrayList<>();
    private SpinnerAdapter spinnerAdapter;
    private Activity activity;
    private PostComplaint posts;
    private PrefIndus prefIndus;
    private String locationSelected;
    private final int IMAGE_TAKE=100;
    private final int IMAGE_BROWSE=200;
    private StorageReference storageReference;
    private StorageReference storageRef;
    private Uri filePath=null;
    private UploadTask uploadTask;
    private int uploadAction;
    private Camera camera;
    private ImageView imageViewCamera;
    private ImageView imageViewGallery;
    private ProgressDialog progressDialog;
    private final int MAX_UPLOAD=3;
    private boolean isUploadMax=true;
    private LinearLayout complaint_item_row;
    private LinearLayout input_location_gps;
    private ImageView complaint_image_item;
    private double latitude;
    private double longitude;
    private String department;
    private String module;
    private String student;
    private final static  String complaint="complaint";
    private final static String STUDENT="student";
    private final static String MODULE="module";


    private String[] permissionsRequired={
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };


    @InjectView(R.id.input_message)EditText edtMessage;
    @InjectView(R.id.btn_submit)Button btnSubmit;
    @InjectView(R.id.input_message_item)EditText inputMessageItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        activity=this;
        prefIndus=new PrefIndus(this);
        department="CE_14";//TODO get dep code and year from email
        module="complaints";
        student="students";
        posts=new PostComplaint();
        ButterKnife.inject(activity);
        FirebaseApp.initializeApp(activity);

        storageReference= FirebaseStorage.getInstance().getReference();
        imageViewGallery=(ImageView)findViewById(R.id.input_upload_gallery);
        imageViewCamera=(ImageView)findViewById(R.id.input_upload_camera);
        imageViewCamera.setOnClickListener(this);
        imageViewGallery.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
            setTitle("New Complaints");
        }



        setupSpinnerData();
        Resources resources=getResources();
        spinner=(Spinner)findViewById(R.id.input_location_tags_spinner);
        spinner.setPrompt("Select Location");
        spinnerAdapter=new SpinnerAdapter(activity,R.layout.sppiner_popup_layout,spinnerModelList,resources);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(0);


        progressDialog=new ProgressDialog(activity,android.R.style.Theme_Material_Light_Dialog);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(1);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Taking Your Complaint...");

        input_location_gps=(LinearLayout)findViewById(R.id.input_location_gps);
        input_location_gps.setOnClickListener(this);
        complaint_image_item=(ImageView)findViewById(R.id.complaint_image_item);
        complaint_item_row=(LinearLayout)findViewById(R.id.complaint_item_row);
        complaint_item_row.setVisibility(View.GONE);


        // Locate the UI widgets.
        mStartUpdatesButton = (LinearLayout) findViewById(R.id.input_location_gps);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);

        // Set labels.
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        //TODO plase optimize code for firebase dtatbase loaction
        //setup firebase database location

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference("locations");
        GeoFire geoFire=new GeoFire(databaseReference);
        geoFire.getLocation("vishal", new com.firebase.geofire.LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    Log.d("onLocationResult", "key:"+key+"location"+location.latitude+"location"+location.longitude);

                } else {
                    Log.d("onLocationResult", "Location:Null");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                    Log.d("onLocationResult", "databaseError"+databaseError.toString());


            }
        });


        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(22.9489473, 72.6452372), 0.5);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Toast.makeText(getApplicationContext(),key,Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(),"onKeyEntered:key:"+key+"location"+location.latitude+"location"+location.longitude,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onKeyExited(String key) {
                Toast.makeText(getApplicationContext(),"onKeyExited:key:"+key,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;

            case IMAGE_TAKE:
                switch (resultCode){
                    case Activity.RESULT_OK:{

                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        complaint_item_row.setVisibility(View.VISIBLE);
                        complaint_image_item.setImageBitmap(photo);
                        Uri tempUri = getImageUri(getApplicationContext(), photo);
                        filePath=tempUri;
                        File finalFile = new File(getRealPathFromURI(tempUri));
                         }
                }
                break;

            case IMAGE_BROWSE:
                switch (resultCode){
                    case Activity.RESULT_OK:{
                        try {
                            filePath=data.getData();
                            Bitmap fileBitmap= MediaStore.Images.Media.getBitmap(activity.getContentResolver(),filePath);
                            complaint_item_row.setVisibility(View.VISIBLE);
                            complaint_image_item.setImageBitmap(fileBitmap);

                        }catch (Exception e){
                            e.printStackTrace();
                        }


                    }
                }
                break;

        }

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        locationSelected=((TextView)view.findViewById(R.id.location_name)).getText().toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case  R.id.input_location_gps:{
                displayLocationSettingsRequest(this);
                break;
            }
            case  R.id.btn_submit:{
                showProgress("Sending your complaint...");
                checkIsUploadMax();
                break;
            }
            case R.id.input_upload_camera:{
                Intent intentCamera=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(intentCamera.resolveActivity(activity.getPackageManager())!=null){
                    startActivityForResult(intentCamera.createChooser(intentCamera, "Take Image"),IMAGE_TAKE);
                    uploadAction=IMAGE_TAKE;
                }
                break;

            }
            case R.id.input_upload_gallery:{
                Intent intentGallery=new Intent();
                intentGallery.setType("image/*");
                intentGallery.setAction(Intent.ACTION_GET_CONTENT);
                if(intentGallery.resolveActivity(activity.getPackageManager())!=null){
                    startActivityForResult(intentGallery.createChooser(intentGallery, "Select Image"),IMAGE_BROWSE);
                    uploadAction=IMAGE_BROWSE;
                }
                break;
            }

            default:{

            }
        }
    }



    private void submitComplaint(){

        boolean action=false;
        if(preparePosts()!=null){

            final PostComplaint postComplaint=preparePosts();

            if(postComplaint.getUid()!=null){

                final String studentId        = postComplaint.getUid();
                final String storagePath      = postComplaint.getImg_1();
                final String complaintCreated = postComplaint.getCreated();

                final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = firebaseDatabase.getReference(student).child(department).child(studentId).child(module).child(complaintCreated);
                final DatabaseReference databaseReferenceComplaint = firebaseDatabase.getReference(complaint);

                try {


                    if(filePath!=null){

                        storageRef= storageReference.child(storagePath);
                        uploadTask=storageRef.putFile(filePath);
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<UploadTask.TaskSnapshot> task) {
                                final String imgPath=task.getResult().getDownloadUrl().toString();
                                postComplaint.setImg_1(imgPath);
                                if(task.isSuccessful()){
                                    final String fireid= databaseReference.push().getKey();
                                    postComplaint.setFire_id(fireid);
                                    postComplaint.setUid(studentId);
                                    final DatabaseReference databaseReferenceLocation=firebaseDatabase.getReference("locations");
                                    final GeoFire geoFire=new GeoFire(databaseReferenceLocation);
                                    geoFire.setLocation(postComplaint.getLocation(),new GeoLocation(latitude,longitude));
                                    databaseReferenceComplaint.push().setValue(postComplaint, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        }
                                    });

                                    databaseReference.push().setValue(postComplaint, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            sendNotification(postComplaint);
                                            onSuccess();
                                        }

                                    });

                                }else{
                                    onFailed();
                                }
                            }
                        });

                    }else{

                        final String fireid= databaseReference.push().getKey();
                        postComplaint.setFire_id(fireid);
                        databaseReferenceComplaint.push().setValue(postComplaint, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            }
                        });
                        databaseReference.push().setValue(postComplaint, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                sendNotification(postComplaint);
                                onSuccess();
                            }
                        });

                    }





                }catch (Exception e){
                    e.printStackTrace();
                }


            }

        }

        stopProgress();
    }//camera image



    private void setupSpinnerData(){
        String[] locations={"General","Bhawarbuilding","Campus","Main Building","Canteen","Ground area","Library","Financial"};

        int[] locations_images={R.drawable.zalakvyas,R.drawable.zalakvyas,R.drawable.zalakvyas,R.drawable.zalakvyas,R.drawable.zalakvyas,R.drawable.zalakvyas,R.drawable.zalakvyas,R.drawable.zalakvyas};

        for (int i=0;i<locations.length;i++){
            SpinnerModel spinnerModel=new SpinnerModel();
            spinnerModel.setLocation(locations[i]);
            spinnerModel.seturl(locations_images[i]);
            spinnerModelList.add(spinnerModel);

        }
    }



    private String getUniquePath(){
        SimpleDateFormat pathExtenstion = new SimpleDateFormat("yyyyMMdd");
        String pathExt= pathExtenstion.format(new Date());
        SimpleDateFormat extention = new SimpleDateFormat("yyyyMMddhhmmss");
        String ext= extention.format(new Date());
        String username=new PrefIndus(this).getUserEmail().toString();
        String path=username+"/images/"+pathExt+"/"+ext+".jpg";
        return path;
    }


    private PostComplaint preparePosts(){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat timeStamp        = new SimpleDateFormat("yyyy-MM-dd");
        String title,  message,  location,  latitude,  longitude,  img_1,  img_2,  img_3,  created,  modified, uid,fire_id,status;

        title           =prefIndus.getUserName();
        fire_id         ="";
        uid             =prefIndus.getUserId();
        message         =edtMessage.getText().toString();
        location        =locationSelected;
        latitude        =String.valueOf(this.latitude);
        longitude       =String.valueOf(this.longitude);
        img_1           ="";
        img_2           ="";
        img_3           ="";
        created         =timeStamp.format(new Date());
        modified        =created;
        status        ="0";

        

        PostComplaint postComplaint=new PostComplaint(title, message, location, latitude, longitude, img_1, img_2, img_3, created, modified,uid,status,fire_id);
        return postComplaint;

    }



    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    private void showProgress(final String message)
    {
        progressDialog.setMessage(message);
        progressDialog.show();


    }
    private void stopProgress(){
        progressDialog.dismiss();
    }
    private void sendNotification(PostComplaint postComplaint){
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        new NotificationUtils(getApplicationContext()).showNotificationMessage("Complaint","Your Complaint in process...",postComplaint.getCreated(),intent,postComplaint.getImg_1());
    }
    private void onSuccess(){
        stopProgress();
        Toast.makeText(this,"Complaint sent successfully",Toast.LENGTH_LONG).show();
        startActivity(new Intent(this,MainActivity.class));
        finish();

    }
    private void onFailed(){
        stopProgress();
        Toast.makeText(this,"Something went wronf",Toast.LENGTH_LONG).show();

    }
    private boolean validate(){
        boolean valid=false;
        return valid;
    }
    private void checkIsUploadMax(){

        boolean valid=false;
        PostComplaint postComplaint= preparePosts();
        String studentId=postComplaint.getUid();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference(student).child(department).child(studentId).child(module).child(postComplaint.getCreated());
        databaseReference.addListenerForSingleValueEvent(this);

    }


    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if(dataSnapshot.getChildrenCount()>=3){
            Toast.makeText(getApplicationContext(),"Max 3 complaint per a day Excessed,Complaint saved as draf",Toast.LENGTH_LONG).show();
            stopProgress();
            return;
        }else{
            submitComplaint();
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
    private void getUserGpsLocation(){

    }

    private void getUserPermission(){


//        if(ActivityCompat.checkSelfPermission(ActivityComplaint.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
//                || ActivityCompat.checkSelfPermission(ActivityComplaint.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
//                || ActivityCompat.checkSelfPermission(ActivityComplaint.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED){
//            if(ActivityCompat.shouldShowRequestPermissionRationale(ActivityComplaint.this,permissionsRequired[0])
//                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityComplaint.this,permissionsRequired[1])
//                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityComplaint.this,permissionsRequired[2])){
//                //Show Information about why you need the permission
//                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityComplaint.this);
//                builder.setTitle("Need Multiple Permissions");
//                builder.setMessage("This app needs Camera and Location permissions.");
//                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                        ActivityCompat.requestPermissions(ActivityComplaint.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
//                    }
//                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//                builder.show();
//            } else if (true) {
//                //Previously Permission Request was cancelled with 'Dont Ask Again',
//                // Redirect to Settings after showing Information about why you need the permission
//                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityComplaint.this);
//                builder.setTitle("Need Multiple Permissions");
//                builder.setMessage("This app needs Camera and Location permissions.");
//                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
////                        sentToSettings = true;
//                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        Uri uri = Uri.fromParts("package", getPackageName(), null);
//                        intent.setData(uri);
//                        startActivityForResult(intent, 101);
//                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant  Camera and Location", Toast.LENGTH_LONG).show();
//                    }
//                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//                builder.show();
//            }  else {
//                //just request the permission
//                ActivityCompat.requestPermissions(ActivityComplaint.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
//            }
//
////            txtPermissions.setText("Permissions Required");
//
//            SharedPreferences.Editor editor = permissionStatus.edit();
//            editor.putBoolean(permissionsRequired[0],true);
//            editor.commit();
//        } else {
//            //You already have the permission, just go ahead.
//            proceedAfterPermission();
//        }
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();


        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:{


                        Log.i("CompaintActivity", "All location settings are satisfied.");
                        break;
                    }


                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("CompaintActivity", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(ActivityComplaint.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("CompaintActivity", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("CompaintActivity", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    /*location implematations*/

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
             // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
            updateUI();
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
     */
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link com.google.android.gms.location.LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(ActivityComplaint.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }



    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void startUpdatesButtonHandler(View view) {
        checkLocationSettings();
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates.
     */
    public void stopUpdatesButtonHandler(View view) {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        stopLocationUpdates();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = true;
                setButtonsEnabledState();
            }
        });

    }

    /**
     * Updates all UI fields.
     */
    private void updateUI() {
        setButtonsEnabledState();
        updateLocationUI();
    }

    /**
     * Disables both buttons when functionality is disabled due to insuffucient location settings.
     * Otherwise ensures that only one button is enabled at any time. The Start Updates button is
     * enabled if the user is not requesting location updates. The Stop Updates button is enabled
     * if the user is requesting location updates.
     */
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            longitude= mCurrentLocation.getLongitude();
            latitude= mCurrentLocation.getLatitude();
            Toast.makeText(getApplicationContext(),"Latitude"+mCurrentLocation.getLatitude(),Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(),"Latitude"+mCurrentLocation.getLongitude(),Toast.LENGTH_LONG).show();

        }else{
            Toast.makeText(getApplicationContext(),"Latitude:null",Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(),"Latitude:null",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
                setButtonsEnabledState();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateLocationUI();
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateLocationUI();
        Toast.makeText(this, getResources().getString(R.string.location_updated_message),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.complaints,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_send_complaints_camera:{
                Intent intentCamera=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(intentCamera.resolveActivity(activity.getPackageManager())!=null){
                    startActivityForResult(intentCamera.createChooser(intentCamera, "Take Image"),IMAGE_TAKE);
                    uploadAction=IMAGE_TAKE;
                }
                return true;
            }
            case R.id.action_send_complaints_gallery:{
                Intent intentGallery=new Intent();
                intentGallery.setType("image/*");
                intentGallery.setAction(Intent.ACTION_GET_CONTENT);
                if(intentGallery.resolveActivity(activity.getPackageManager())!=null){
                    startActivityForResult(intentGallery.createChooser(intentGallery, "Select Image"),IMAGE_BROWSE);
                    uploadAction=IMAGE_BROWSE;
                }
                return true;
            }
            default:{
                return super.onOptionsItemSelected(item);
            }
        }
    }
}





