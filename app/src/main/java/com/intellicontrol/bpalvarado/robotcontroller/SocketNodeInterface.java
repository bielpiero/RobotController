package com.intellicontrol.bpalvarado.robotcontroller;

public interface SocketNodeInterface{
    public void onConnection();
    public void onMessageReceived(char opr, String data);
}
