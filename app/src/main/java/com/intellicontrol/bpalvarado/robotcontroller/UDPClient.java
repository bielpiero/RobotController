package com.intellicontrol.bpalvarado.robotcontroller;

import android.os.AsyncTask;

import java.io.*;
import java.net.*;

/**
 * Created by bpalvarado on 03/06/2015.
 */
public class UDPClient extends AsyncTask<Void, Void, Void> implements UDPClientInterface {
    public static int SOCKET_SERVER = 0;
    public static int SOCKET_CLIENT = 1;
    public static int THREAD_ALIVE = 1;
    public static int THREAD_STOPPED = 0;
    public static int THREAD_STOPPING = 2;
    public static int BUFFER_SIZE = 65535;
    private DatagramSocket socket_conn;
    private SocketAddress remoteAddress;
    private int threadStatus;
    private int type;
    private byte[] bufferIn;

    private UDPClientInterface target;

    UDPClient(String address, int port, UDPClientInterface target) {
        threadStatus = THREAD_STOPPED;
        this.type = SOCKET_CLIENT;
        if(address.isEmpty()){
            this.type = SOCKET_SERVER;
        }
        if(this.type == SOCKET_SERVER) {
            try{
                this.socket_conn = new DatagramSocket(port);
            } catch (IOException e){

            }
        } else {
            remoteAddress = new InetSocketAddress(address, port);
            try {
                this.socket_conn = new DatagramSocket();
            } catch(Exception sockEx){}
        }
        this.target = target;
    }

    int getPort(){
        return socket_conn.getLocalPort();
    }

    int sendData(byte[] data){
        try {
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, remoteAddress);
            socket_conn.send(sendPacket);
        } catch (Exception datagramEx){}
        return 0;
    }

    int receiveData(){
        bufferIn = new byte[BUFFER_SIZE];
        DatagramPacket receiveData = new DatagramPacket(bufferIn, BUFFER_SIZE);
        try {
            socket_conn.receive(receiveData);
            bufferIn = new byte[receiveData.getLength()];
            System.arraycopy(receiveData.getData(),0, bufferIn, 0, receiveData.getLength());
        } catch(Exception receiveEx) {}
        return 0;
    }

    public void startConnection(){
        this.threadStatus = THREAD_ALIVE;
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected Void doInBackground(Void... params) {
        while(threadStatus == THREAD_ALIVE){
            if(receiveData() == 0) {
                onUDPMessageReceived(bufferIn);
            }
            /*try {
                Thread.sleep(100);
            } catch (InterruptedException e) { }*/
        }
        threadStatus = THREAD_STOPPED;
        return null;
    }

    @Override
    public void onUDPMessageReceived(byte[] data) {
        target.onUDPMessageReceived(data);
    }

    public void closeConnection() throws InterruptedException{
        if(threadStatus == THREAD_ALIVE){
            threadStatus = THREAD_STOPPING;
            while(threadStatus != THREAD_STOPPED) {
                Thread.sleep(100);
            }
        }
        if(socket_conn != null) {
            socket_conn.close();
        }
    }
}
