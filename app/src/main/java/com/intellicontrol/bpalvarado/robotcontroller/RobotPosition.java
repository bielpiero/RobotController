package com.intellicontrol.bpalvarado.robotcontroller;

import java.io.UnsupportedEncodingException;

/**
 * Created by bpalvarado on 22/06/2015.
 */
public class RobotPosition implements UDPClientInterface {
    private UDPClient listener;
    private int port;
    private double x;
    private double y;
    private double theta;
    private double transSpeed;
    private double rotSpeed;

    RobotPosition(){
        port = 0;
        listener = new UDPClient("", port, this);
        port = listener.getPort();
        listener.startConnection();
    }
    @Override
    public void onUDPMessageReceived(byte[] data) {
        String decoded;
        try {
            decoded = new String(data, "UTF-8");
            String[] positionSpeedSpllit = decoded.split("\\|");
            String[] position = positionSpeedSpllit[1].split(",");
            String[] speed = positionSpeedSpllit[2].split(",");
            x = Double.parseDouble(position[0]);
            y = Double.parseDouble(position[1]);
            theta = Double.parseDouble(position[2]);

            transSpeed = Double.parseDouble(speed[0]);
            rotSpeed = Double.parseDouble(speed[1]);
        } catch (UnsupportedEncodingException e) {  }
    }

    public int getPort(){
        return port;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public double getThDegrees(){
        return (theta * 180 / Math.PI);
    }

    public double getThRad(){
        return theta;
    }

    public double getTransSpeed(){
        return transSpeed;
    }

    public double getRotSpeed(){
        return rotSpeed;
    }
}
