package com.intellicontrol.bpalvarado.robotcontroller;

import android.media.Image;

/**
 * Created by bpalvarado on 02/06/2015.
 */
public class Gesture {
    public Image expressionImage;
    public int expressionId;
    public String expressionName;

    public Gesture(int expressionId, String expressionName, Image image){
        this.expressionImage = image;
        this.expressionId = expressionId;
        this.expressionName = expressionName;
    }
}
