package com.intellicontrol.bpalvarado.robotcontroller;

/**
 * Created by bpalvarado on 01/06/2015.
 */

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

import android.os.AsyncTask;
import android.util.Log;

public class SocketNode extends AsyncTask<Void, Void, Void> implements SocketNodeInterface{
    public static int SOCKET_SERVER = 0;
    public static int SOCKET_CLIENT = 1;
    public static int THREAD_ALIVE = 1;
    public static int BUFFER_SIZE = 1048576;
    public static int THREAD_STOPPED = 0;
    public static int THREAD_STOPPING = 2;
    public static int MAX_LISTEN_CONNECTIONS = 5;

    private OutputStream out;
    private InputStream in;
    private int threadStatus;
    private Socket socket_conn;
    private ServerSocket socket_server;
    private SocketAddress remoteAddress;
    private int type;

    SocketNode(String address, int port) {
        threadStatus = THREAD_STOPPED;
        this.type = SOCKET_CLIENT;
        if(address.isEmpty()){
            this.type = SOCKET_SERVER;
        }
        if(this.type == SOCKET_SERVER) {
            try{
                this.socket_server = new ServerSocket(port);
            } catch (IOException e){

            }
        } else {
            remoteAddress = new InetSocketAddress(address, port);
            this.socket_conn = new Socket();
        }

    }

    public boolean isConnected(){
        boolean connStatus = false;
        try{
            connStatus = socket_conn.isConnected();
        } catch (NullPointerException e){
            connStatus = false;
        }
        return connStatus;
    }

    public int sendMsg(char opr, String data){
        int ret = 0;
        if(isConnected()){
            byte[] dataInBytes = data.getBytes(Charset.forName("UTF-8"));
            int length = dataInBytes.length + 1;
            byte[] bufferOut = new byte[dataInBytes.length + 6];
            bufferOut[0] = 57;
            bufferOut[1] = 48;
            if ((length / 256) >= 256)
            {
                int divi = length / 256;
                bufferOut[2] = (byte)(length % 256);
                bufferOut[3] = (byte)(divi % 256);
                bufferOut[4] = (byte)(divi / 256);
            }
            else
            {
                bufferOut[2] = (byte)(length % 256);
                bufferOut[3] = 0;
                bufferOut[4] = (byte)(length / 256);
            }
            bufferOut[5] = (byte)opr;
            System.arraycopy(dataInBytes, 0, bufferOut, 6, dataInBytes.length);

            try{
                out.write(bufferOut);
            } catch (IOException e) { ret = -2; }
        } else { ret = -1; }
        return ret;
    }

    public int sendBytes(String data){

        return 0;
    }

    public int receiveMsg(char opr, String data, int length){

        return 0;
    }

    public int receiveBytes(String data, int length) throws IOException {


        return 0;
    }

    private void handleConnection() throws InterruptedException, IOException {
        if(this.type == SOCKET_SERVER){
            if (this.socket_server != null) {
                try {
                    socket_conn = socket_server.accept();
                    socket_conn.setReuseAddress(true);
                    socket_conn.setReceiveBufferSize(BUFFER_SIZE);
                    socket_conn.setSendBufferSize(BUFFER_SIZE);

                    in = socket_conn.getInputStream();
                    out = socket_conn.getOutputStream();
                } catch (IOException e) {
                    this.close();
                }
                onConnection();
            }
        } else {
            try {
                socket_conn.connect(remoteAddress);
                socket_conn.setReuseAddress(true);
                socket_conn.setReceiveBufferSize(BUFFER_SIZE);
                socket_conn.setSendBufferSize(BUFFER_SIZE);

                in = socket_conn.getInputStream();
                out = socket_conn.getOutputStream();
            } catch (IOException e) {
                this.close();
            }
            onConnection();
        }
    }

    @Override
    public void onMessageReceived(char opr, String data) {

    }

    @Override
    public void onConnection(){

    }

    public void startConnection(){
        this.threadStatus = THREAD_ALIVE;
        this.execute();
    }

    public void close() throws InterruptedException, IOException {
        if(threadStatus == THREAD_ALIVE){
            threadStatus = THREAD_STOPPING;
            while(threadStatus != THREAD_STOPPED) {
                wait(100);
            }
        }
        if(socket_conn != null) {
            socket_conn.shutdownInput();
            socket_conn.shutdownOutput();
            socket_conn.close();
        }
    }



    @Override
    protected Void doInBackground(Void... params) {
        //SocketNode self = params[0];
        while(threadStatus == THREAD_ALIVE){
            if(!isConnected()){
                try {
                    handleConnection();
                } catch (IOException e) {

                } catch (InterruptedException e) {

                }
            } else {
                char opr = 0;
                String data = "";
                if(receiveMsg(opr, data, BUFFER_SIZE) == 0) {
                    onMessageReceived(opr, data);
                }
            }
        }
        threadStatus = THREAD_STOPPED;
        return null;
    }
}

