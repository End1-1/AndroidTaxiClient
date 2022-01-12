package com.example.yelloclient.classes;

import java.util.LinkedList;
import java.util.List;

public class CarClasses {
    public List<CarClass> car_classes = new LinkedList<>();

    public CarClass getCurrent() {
        for (CarClass c: car_classes) {
            if (c.selected != 0) {
                return c;
            }
        }
        return null;
    }
}
