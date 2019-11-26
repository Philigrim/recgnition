package org.tensorflow.demo;

import com.mapbox.geojson.Point;

public class Sign {
    private int category_id;
    private int sign_id;
    private String sign_name;
    private Point point;
    private String unique_sign_id = "Source";
    private String unique_sign_layer_id = "Layer";

    Sign(int category_id, int sign_id, String sign_name, Point point, int unique_sign_id, int unique_sign_layer_id){
        this.category_id = category_id;
        this.sign_id = sign_id;
        this.sign_name = sign_name;
        this.point = point;
        this.unique_sign_id += unique_sign_id;
        this.unique_sign_layer_id += unique_sign_layer_id;
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

    public String getUnique_sign_id() {
        return unique_sign_id;
    }

    public String getUnique_sign_layer_id() {
        return unique_sign_layer_id;
    }
}
