package com.intellicontrol.bpalvarado.robotcontroller;

/**
 * Created by bpalvarado on 03/06/2015.
 */
public interface UDPClientInterface {
    public void onMessageReceived(byte[] data);
}
