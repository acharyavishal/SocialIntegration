package com.android.vishalacharya.indusuniversity.beans;

/**
 * Created by vishal_ACHARYA on 3/25/2017.
 */

public class PostComplaint {
    
    private String title;
    private String message;
    private String location;
    private String latitude;
    private String longitude;
    private String img_1;
    private String img_2;
    private String img_3;
    private String created;
    private String modified;
    private String uid;
    private String status;
    private String fire_id;


    public String getFire_id() {
        return fire_id;
    }

    public void setFire_id(String fire_id) {
        this.fire_id = fire_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getImg_1() {
        return img_1;
    }

    public void setImg_1(String img_1) {
        this.img_1 = img_1;
    }

    public String getImg_2() {
        return img_2;
    }

    public void setImg_2(String img_2) {
        this.img_2 = img_2;
    }

    public String getImg_3() {
        return img_3;
    }

    public void setImg_3(String img_3) {
        this.img_3 = img_3;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    public PostComplaint(){
        this( "title"," message"," location"," latitude"," longitude"," img_1"," img_2"," img_3"," created"," modified","uid","status","fire_id");
    }

    public PostComplaint(String title, String message, String location, String latitude, String longitude, String img_1, String img_2, String img_3, String created, String modified, String uid, String status, String fire_id) {
        this.title = title;
        this.message = message;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.img_1 = img_1;
        this.img_2 = img_2;
        this.img_3 = img_3;
        this.created = created;
        this.modified = modified;
        this.uid = uid;
        this.status = status;
        this.fire_id = fire_id;
    }

    @Override
    public String toString() {
        return "PostComplaint{" +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", location='" + location + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", img_1='" + img_1 + '\'' +
                ", img_2='" + img_2 + '\'' +
                ", img_3='" + img_3 + '\'' +
                ", created='" + created + '\'' +
                ", modified='" + modified + '\'' +
                ", uid='" + uid + '\'' +
                ", status='" + status + '\'' +
                ", fire_id='" + fire_id + '\'' +
                '}';
    }
}
