package com.intellicontrol.bpalvarado.robotcontroller;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.DropBoxManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ConnectionActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, SensorEventListener, SocketNodeInterface, UDPClientInterface {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private SocketNode connection;
    private UDPClient udpConnection;
    private ViewGroup currentFrame;
    private ArrayList<Gesture> gestures;

    private SensorManager sensorManager;
    private Sensor sensor;

    private ImageButton activateDeactivateCamera;
    private ImageView imageViewStream;


    private Button lockVelocities;

    private Button buttonForward;
    private Button buttonBackward;
    private Button buttonLeft;
    private Button buttonRight;

    private BaseLoaderCallback  mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    activateDeactivateCamera = (ImageButton) findViewById(R.id.imageButtonCamera);
                    activateDeactivateCamera.setEnabled(true);

                break;
                case LoaderCallbackInterface.INIT_FAILED:
                    activateDeactivateCamera = (ImageButton) findViewById(R.id.imageButtonCamera);
                    activateDeactivateCamera.setEnabled(false);
                    break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        connection = new SocketNode("192.168.1.101", 14004, this);
        connection.startConnection();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connection.sendMsg((byte)0x00, "");
        currentFrame = (LinearLayout)findViewById(R.id.expressions_frame);

        gestures = new ArrayList<Gesture>();
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        udpConnection = null;

        final ConnectionActivity self = this;
        activateDeactivateCamera = (ImageButton)findViewById(R.id.imageButtonCamera);
        activateDeactivateCamera.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (udpConnection == null) {
                        int port = 0;
                        udpConnection = new UDPClient("", port, self);
                        port = udpConnection.getPort();
                        udpConnection.startConnection();
                        connection.sendMsg((byte) 0x21, "1:" + port);
                    } else {
                        connection.sendMsg((byte) 0x22, "");
                        try {
                            udpConnection.closeConnection();
                        } catch (InterruptedException e) {
                        }
                        udpConnection = null;
                    }
                }
                return true;
            }
        });
        if(!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mOpenCVCallBack)) {

        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {

        currentFrame.setVisibility(View.INVISIBLE);
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section_expressions);
                currentFrame = (LinearLayout)findViewById(R.id.expressions_frame);
                currentFrame.setVisibility(View.VISIBLE);
                break;
            case 2:
                mTitle = getString(R.string.title_section_navigation);
                currentFrame = (RelativeLayout)findViewById(R.id.navigation_frame);
                currentFrame.setVisibility(View.VISIBLE);
                break;
            case 3:
                mTitle = getString(R.string.title_section_emotions);
                currentFrame = (LinearLayout)findViewById(R.id.emotions_frame);
                currentFrame.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.connection, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnection() {

    }

    @Override
    public void onMessageReceived(byte opr, String data) {
        switch (opr){
            case 0:
                fillExpressionsList(data);
                break;
            case 1:
                break;
        }
    }
    private void fillExpressionsList(String data){
        ListView expressionsList = (ListView)findViewById(R.id.listViewExpressions);
        String[] facesSplited = data.split("\\|");
        gestures.clear();
        for(int i = 0; i < facesSplited.length; i++){
            if(!facesSplited[i].isEmpty()){
                String[] faceIdName = facesSplited[i].split(",");
                int gestureId = Integer.parseInt(faceIdName[0]);
                gestures.add(new Gesture(gestureId, faceIdName[1], null));

            }
        }
        ArrayAdapter<Gesture> adapter = new GesturesAdapter(this, gestures);
        expressionsList.setAdapter(adapter);
        expressionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Gesture selected = gestures.get(position);
                connection.sendMsg((byte)0x07, Integer.toString(selected.expressionId));
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /*EditText linVelocity = (EditText)findViewById(R.id.editTextLinearVelocity);
        EditText angVelocity = (EditText)findViewById(R.id.editTextAngularVelocity);
        if(velocitiesLocked){

            int linVel = 0;
            int angVel = 0;
            if(event.values[2]>(accelerometerZOffset + 2.5)) {
                linVel = Integer.parseInt(linVelocity.getText().toString());
            } else if(event.values[2] < (accelerometerZOffset - 2.5)){
                linVel = -Integer.parseInt(linVelocity.getText().toString());
            }

            if(event.values[1]>1.5) {
                angVel = Integer.parseInt(angVelocity.getText().toString());
            } else if(event.values[1] < -1.5){
                angVel = -Integer.parseInt(angVelocity.getText().toString());
            }
            connection.sendMsg((byte)0x10, Integer.toString(linVel) + "," + Integer.toString(angVel));
        } else {
            connection.sendMsg((byte)0x10, "0,0");
        }
        TextView accValues = (TextView)findViewById(R.id.textViewAccValues);
        accValues.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + (event.values[2]));*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onUDPMessageReceived(byte[] data) {
        MatOfByte dataToMat = new MatOfByte(data);
        Mat dataDecoded = Highgui.imdecode(dataToMat, Highgui.CV_LOAD_IMAGE_COLOR);
        final Bitmap bitmapImage = Bitmap.createBitmap(dataDecoded.cols(), dataDecoded.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dataDecoded, bitmapImage);
        imageViewStream = (ImageView)findViewById(R.id.imageViewStream);
        imageViewStream.post(new Runnable() {
            @Override
            public void run() {
                imageViewStream.setImageBitmap(bitmapImage);
            }
        });
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((ConnectionActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
