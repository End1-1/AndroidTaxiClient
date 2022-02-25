package com.example.yelloclient.classes;

import android.graphics.Bitmap;

import java.util.LinkedList;
import java.util.List;


public class CarClass {
    public int class_id;
    public String name;
    public String currency;
    public double min_price;
    public double coin;
    public String image;
    public int selected;
    public Bitmap _image ;
    public List<CarOption> car_options = new LinkedList<>();
    public List<Integer> rent_times = new LinkedList<>();
}
