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

    private byte[] bufferIn;
    private int bufferLength;

    private byte finalOpr;
    private String finalData;

    private SocketNodeInterface target;

    SocketNode(String address, int port, SocketNodeInterface target) {
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
        this.target = target;
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

    public int sendMsg(byte opr, String data){
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

    public int receiveMsg(){
        int ret = 0;
        int headerSize = 5;
        byte[] header = new byte[headerSize];
        int dataLength;
        if(isConnected()){
            if(receiveBytes(headerSize) == 0){
                header = bufferIn;
                if (header[3] != 0){
                    dataLength = header[2] + (header[3] + header[4] * 256) * 256;
                } else {
                    dataLength = header[2] + header[4] * 256;
                }
                if (header[0] != 57 || header[1] != 48 || dataLength <= 0){
                    ret = -3;
                } else {
                    receiveBytes(dataLength);
                    this.finalOpr = bufferIn[0];
                    byte[] auxBufferIn = new byte[bufferIn.length - 1];
                    System.arraycopy(bufferIn, 1, auxBufferIn, 0, auxBufferIn.length);
                    try {
                        this.finalData = new String(auxBufferIn, "UTF-8");
                    } catch (UnsupportedEncodingException encEx){ ret = -4; }
                }
            } else { ret = -2; }
        } else { ret = -1; }
        return ret;
    }

    public int receiveBytes(int length) {
        int ret = 0;
        if(isConnected()){
            byte[] dataInBytes = new byte[length];
            try {
                in.read(dataInBytes, 0, dataInBytes.length);
                this.bufferIn = dataInBytes;
                this.bufferLength = dataInBytes.length;
            } catch (IOException e) {
                ret = -2;
            }
        } else { ret = -1; }

        return ret;
    }

    private void handleConnection() throws InterruptedException, IOException {
        if(this.type == SOCKET_SERVER){
            if (this.socket_server != null) {
                try {
                    socket_conn = socket_server.accept();
                    socket_conn.setReuseAddress(true);
                    socket_conn.setReceiveBufferSize(BUFFER_SIZE);
                    socket_conn.setSendBufferSize(BUFFER_SIZE);
                    socket_conn.setSoTimeout(0);

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
                socket_conn.setSoTimeout(0);

                in = socket_conn.getInputStream();
                out = socket_conn.getOutputStream();
            } catch (IOException e) {
                this.close();
            }
            onConnection();
        }
    }

    @Override
    public void onMessageReceived(byte opr, String data) {
        target.onMessageReceived(opr, data);
    }

    @Override
    public void onConnection(){
        target.onConnection();
    }

    public void startConnection(){
        this.threadStatus = THREAD_ALIVE;
        this.execute();
    }

    public void close() throws InterruptedException, IOException {
        if(threadStatus == THREAD_ALIVE){
            threadStatus = THREAD_STOPPING;
            while(threadStatus != THREAD_STOPPED) {
                Thread.sleep(100);
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
        while(threadStatus == THREAD_ALIVE){
            if(!isConnected()){
                try {
                    handleConnection();
                } catch (IOException e) {

                } catch (InterruptedException e) {

                }
            } else {
                if(receiveMsg() == 0) {
                    onMessageReceived(finalOpr, finalData);
                }
            }
            /*try {
                Thread.sleep(100);
            } catch (InterruptedException e) { }*/
        }
        threadStatus = THREAD_STOPPED;
        return null;
    }
}

