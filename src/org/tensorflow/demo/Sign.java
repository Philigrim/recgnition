package org.tensorflow.demo;

import org.postgis.Point;

import java.util.HashMap;

public class Sign {
    private int category_id;
    private int sign_id;
    private String sign_name;
    private Point point;

    Sign(int category_id, int sign_id, String sign_name, Point point){
        this.category_id = category_id;
        this.sign_id = sign_id;
        this.sign_name = sign_name;
        this.point = point;
    }

    Sign(String sign_name){
        this.sign_name = sign_name;
    }

    public static HashMap<String, String> SignNameIdHashMap(){
        HashMap<String, String> signHashMap = new HashMap<String, String>();
        signHashMap.put("Greicio mazinimo priemone", "151");
        signHashMap.put("Stotele", "730");
        signHashMap.put("Pesciuju pereja", "533");
        signHashMap.put("Ivaziuoti draudziama", "301");
        signHashMap.put("Duoti kelia", "203");
        signHashMap.put("Pagrindinis kelias", "201");
        signHashMap.put("Sustoti draudziama", "332");
        signHashMap.put("Stovejimo vieta", "528");

        return signHashMap;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public int getSign_id() {
        return sign_id;
    }

    public void setSign_id(int sign_id) {
        this.sign_id = sign_id;
    }

    public String getSign_name() {
        return sign_name;
    }

    public void setSign_name(String sign_name) {
        this.sign_name = sign_name;
    }

    public Point getPoint(){
        return point;
    }

    public void setPoint(Point point){
        this.point = point;
    }
}
