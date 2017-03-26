package com.android.vishalacharya.indusuniversity.activity;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vishalacharya.indusuniversity.R;
import com.android.vishalacharya.indusuniversity.beans.Complaint;
import com.android.vishalacharya.indusuniversity.beans.Posts;
import com.android.vishalacharya.indusuniversity.preferences.PrefIndus;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import bolts.Task;

/**
 * Created by vishal_ACHARYA on 2/7/2017.
 */

public class CompaintsDialogFragment extends DialogFragment implements TextView.OnClickListener{

    int mNum;
    private TextView txtSave;
    private TextView txtCancel;
    private EditText edtMessage;
    private Toolbar toolbar;
    private View dialogView;
    private ImageView uploadFromCamera;
    private ImageView uploadFromGallery;
    private ImageView imgPreview;
    private Bitmap uploadCamImg;
    private StorageReference storageReference;
    private Uri filePath;
    private StorageReference storageRef;
    private UploadTask uploadTask;
    private LinearLayout imgUploadContainer;
    private PrefIndus prefIndus;
    private Context context;


    private final static int IMAGE_GALLARY=100;
    private final static int IMAGE_CAMERA=200;
    private final static int VEDIO_RECORD=300;
    private int uploadAction;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static CompaintsDialogFragment newInstance(int num) {
        CompaintsDialogFragment f = new CompaintsDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNum = getArguments().getInt("num");
        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        switch ((mNum-1)%6) {
            case 1: style = DialogFragment.STYLE_NO_TITLE; break;
            case 2: style = DialogFragment.STYLE_NO_FRAME; break;
            case 3: style = DialogFragment.STYLE_NO_INPUT; break;
            case 4: style = DialogFragment.STYLE_NORMAL; break;
            case 5: style = DialogFragment.STYLE_NORMAL; break;
            case 6: style = DialogFragment.STYLE_NO_TITLE; break;
            case 7: style = DialogFragment.STYLE_NO_FRAME; break;
            case 8: style = DialogFragment.STYLE_NORMAL; break;
        }

        setStyle(style, android.R.style.Theme_Material_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_complaints, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialogView=view;
        context=getActivity().getApplicationContext();

        Toast.makeText(context,"onViewCreated called!!!",Toast.LENGTH_LONG).show();

        FirebaseApp.initializeApp(context);
        storageReference= FirebaseStorage.getInstance().getReference();
        prefIndus=new PrefIndus(context);

        txtSave=(TextView)view.findViewById(R.id.btn_save_complaints);
        txtCancel=(TextView)view.findViewById(R.id.btn_cancel_complaints);
        txtSave.setOnClickListener(this);
        txtCancel.setOnClickListener(this);

        imgUploadContainer=(LinearLayout) view.findViewById(R.id.img_upload_container);
        edtMessage=(EditText) view.findViewById(R.id.edt_message_complaint);
        imgPreview=(ImageView) view.findViewById(R.id.img_preview_complaints);
        toolbar=(Toolbar) view.findViewById(R.id.toolbar_complaints);
        /*toolabr*/



        if(toolbar!=null){
            toolbar.setTitle("Complaint");
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.action_send_complaints_camera:{

                         Intent intentCamera=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                         if(intentCamera.resolveActivity(getActivity().getPackageManager())!=null){
                             startActivityForResult(intentCamera.createChooser(intentCamera, "Take Image"),IMAGE_CAMERA);
                             uploadAction=IMAGE_CAMERA;
                         }
                            break;
                        }
                        case R.id.action_send_complaints_gallery:{

                            Intent intentGallery=new Intent();
                            intentGallery.setType("image/*");
                            intentGallery.setAction(Intent.ACTION_GET_CONTENT);
                            if(intentGallery.resolveActivity(getActivity().getPackageManager())!=null){
                                startActivityForResult(intentGallery.createChooser(intentGallery, "Select Image"),IMAGE_GALLARY);
                                uploadAction=IMAGE_GALLARY;
                            }
                            break;
                        }

                        default:{
                        }
                    }
                    return true;
                }
            });
            toolbar.inflateMenu(R.menu.complaints);
        }





    }

    @Override
    public void onResume() {
        super.onResume();
        Window window=getDialog().getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width= displayMetrics.widthPixels;
        window.setGravity(Gravity.CENTER);
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER_HORIZONTAL);
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(resultCode==getActivity().RESULT_OK&&requestCode==IMAGE_CAMERA){
            Bundle bundle=data.getExtras();
            Bitmap bitmap=(Bitmap)bundle.get("data");
            imgPreview.setImageBitmap(bitmap);
            imgUploadContainer.setVisibility(View.VISIBLE);


        }else if(resultCode==getActivity().RESULT_OK&&requestCode==IMAGE_GALLARY&&data!=null&&data.getData()!=null){
            try {
                filePath=data.getData();
                Bitmap fileBitmap= MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),filePath);
//                Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filePath.toString()), 200, 200);
                imgPreview.setImageBitmap(fileBitmap);
                imgUploadContainer.setVisibility(View.VISIBLE);
            }catch (Exception e){
                e.printStackTrace();
            }

        }


    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog=getDialog();
        if(dialog!=null){
            int dWidth=ViewGroup.LayoutParams.MATCH_PARENT;
            int dHeight=ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(dWidth,dHeight);

        }
    }


    private void sendDataToFirebase(final int action){




        new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {


              switch (action){
                  case IMAGE_CAMERA:{

                      sendDataToFirebaseByCamera();
                      break;
                  }
                  case IMAGE_GALLARY:{

                      sendDataToFirebaseByGallery();

                      break;
                  }
                  case VEDIO_RECORD:{

                      sendDataToFirebaseByVideo();

                      break;
                  }
                  default:{
                      Toast.makeText(getActivity(),"Sorry!!! Something went wrong",Toast.LENGTH_LONG).show();

                     }
                 }
              }
        });

    }
    private String getUniquePath(){
        SimpleDateFormat pathExtenstion = new SimpleDateFormat("yyyyMMdd");
        String pathExt= pathExtenstion.format(new Date());
        SimpleDateFormat extention = new SimpleDateFormat("yyyyMMddhhmmss");
        String ext= extention.format(new Date());
        String username=new PrefIndus(getActivity()).getUserEmail().toString();
        String path=username+"/images/"+pathExt+"/"+ext+".jpg";
        return path;
    }
    private void sendDataToFirebaseByGallery(){

        Toast.makeText(context,"onSaveClick",Toast.LENGTH_LONG).show();
        String email = prefIndus.getUserEmail();
        String accessToken =prefIndus.getAeviceAccesstoken();
        String messsage = edtMessage.getText().toString();
        String imgPath = "https://google.com";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        String created = simpleDateFormat.format(new Date());
        String modified = "firsttime";
        String timeStamp  = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());

        final Posts posts=new Posts("Your coomplaints is in process...", messsage, "complaints", "complaints", "complaints", imgPath, created, modified, email ,accessToken,null);
        //final Complaint complaint = new Complaint(messsage, email, created, modified, imgPath,"accessToken",null,timeStamp);

        final ProgressDialog progressDialog=new ProgressDialog(getActivity(),android.R.style.Theme_Material_Light_Dialog);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(1);
        progressDialog.setMessage("Taking Your Complaint...");
        progressDialog.show();

        try {
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = firebaseDatabase.getReference("notifications").child("complaints");
            storageRef= storageReference.child(getUniquePath());
            uploadTask=storageRef.putFile(filePath);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<UploadTask.TaskSnapshot> task) {
                    final String imgPath=task.getResult().getDownloadUrl().toString();
                    Log.d("FILE",task.getResult().getStorage().getPath());
                    Posts updated=posts;
                    updated.setFeatureImage(imgPath);



                    if(task.isSuccessful()){

                        final String uid= databaseReference.push().getKey();
                        updated.setId(uid);
                        databaseReference.push().setValue(updated, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(getActivity(),"Thank You!!! Complaint Sent to Administarator",Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                dismissDialog();
                            }

                        });
                    }
                }
            });


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void sendDataToFirebaseByCamera(){
         //FirebaseAuth.getInstance().getCurrentUser().getUid();
        Toast.makeText(context,"onSaveClick",Toast.LENGTH_LONG).show();
        String email = prefIndus.getUserEmail();
        String accessToken =prefIndus.getAeviceAccesstoken();
        String messsage = edtMessage.getText().toString();
        String imgPath = "https://google.com";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String created = simpleDateFormat.format(new Date());
        String modified = "firsttime";


        final ProgressDialog progressDialog=new ProgressDialog(getActivity(),android.R.style.Theme_Material_Light_Dialog);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(1);
        progressDialog.setMessage("Taking Your Complaint...");
        progressDialog.show();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference("notifications").child("complaints");
        final String uid= databaseReference.push().getKey();

        String timeStamp  = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        final Complaint complaint = new Complaint(messsage, email, created, modified, imgPath,"accessToken",null,timeStamp);

        complaint.setKey(uid);
        databaseReference.push().setValue(complaint, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Toast.makeText(getActivity(),"Thank You!!! Complaint Sent to Administarator",Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                dismissDialog();
            }
        });


    }


    private void sendDataToFirebaseByVideo(){}
    private void onSaveClick(){
        sendDataToFirebase(IMAGE_GALLARY);
    }
    private void onCancelClick(){
        Fragment prev = getActivity().getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_save_complaints:{
                onSaveClick();
                break;
            }
            case R.id.btn_cancel_complaints:{
                onCancelClick();
                break;
            }
            default:{

            }
        }
    }


    private void dismissDialog(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            DialogFragment dialogFragment=(DialogFragment)prev;
            dialogFragment.dismiss();
        }
    }
}
