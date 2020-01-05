package org.tensorflow.demo;

import org.postgis.Point;

import java.util.Collections;
import java.util.HashMap;

public class Sign implements Comparable {
    private int category_id;
    private int sign_id;
    private String sign_name;
    private Point point;
    private int group;

    @Override
    public String toString(){
        return this.category_id + " " + this.sign_id + " " + this.sign_name + " " + this.point + " " + this.group;
    }

    Sign(int category_id, int sign_id, String sign_name, Point point, int group){
        this.category_id = category_id;
        this.sign_id = sign_id;
        this.sign_name = sign_name;
        this.point = point;
        this.group = group;
    }

    public static HashMap<String, String> SignNameIdHashMap(){
        HashMap<String, String> signHashMap = new HashMap<String, String>();
        signHashMap.put("Greicio mazinimo priemone", "151");
        signHashMap.put("Stotele", "548");
        signHashMap.put("Pesciuju pereja", "533");
        signHashMap.put("Ivaziuoti draudziama", "301");
        signHashMap.put("Duoti kelia", "203");
        signHashMap.put("Pagrindinis kelias", "201");
        signHashMap.put("Sustoti draudziama", "332");
        signHashMap.put("Stovejimo vieta", "528");

        return signHashMap;
    }

    @Override
    public int compareTo(Object sign) {
        int compareage=((Sign)sign).getGroup();
        return this.group-compareage;
    }

    public String getSign_name() {
        return sign_name;
    }

    public Point getPoint(){
        return point;
    }

    public void setPoint(Point point){
        this.point = point;
    }

    public int getGroup() { return group; }
}
