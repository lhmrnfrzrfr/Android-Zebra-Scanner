package com.pemi.pemibarcodescanner.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class BarcodeResponse {

    @SerializedName("status")
    private Boolean status;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private ArrayList<Barcode> barcode;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<Barcode> getBarcode() {
        return barcode;
    }

    public void setBarcode(ArrayList<Barcode> barcode) {
        this.barcode = barcode;
    }

    @Override
    public String toString(){
        return "BarcodeResponse {" +
                "data='" + barcode + '\'' +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

}
