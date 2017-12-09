package jp4r7.sendsms;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;


public class MainActivity extends AppCompatActivity {
    //get acces to location permsion
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    final private int RECEIVE_CODE_ACT = 1669;
    BluetoothSPP bt;

    Button btnSave;
    Button btnShowLoca;
    EditText edtNoPhone;
    EditText edtText;
    TextView tvStatus;
    TextView tvLocation;
    String  noPhone;
    String message;
    int messageReceive;
    String location2show;
    SharedPreferences settings;
    SharedPreferences location;

    String log,lat;

    GetLocation getLocation;

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLocation = new GetLocation(this);
        location2show = "Location : ";

        bt = new BluetoothSPP(this);

        btnSave = findViewById(R.id.btnSave);
        btnShowLoca = findViewById(R.id.btnShowLoca);
        edtNoPhone = findViewById(R.id.edtNoPhone);
        edtText = findViewById(R.id.edtText);
        tvLocation = findViewById(R.id.tvLocation);
        tvStatus = findViewById(R.id.tvStatus);

        getlocation2Show();
        loadSave();
        loadSaveLocation();

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Log.d("data",Arrays.toString(data));
                Log.d("data2",message);

                 messageReceive = Integer.parseInt(message);
                if(messageReceive  == RECEIVE_CODE_ACT){
                    loadSaveLocation();
                    tvLocation.setText("Location : "+ location2show);
                    sendSMSMessage();
                    messageReceive = 0;
                }

            }
        });



        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("NoPhone", edtNoPhone.getText().toString());
                editor.putString("Msg", edtText.getText().toString());
                // Apply the edits!
                editor.apply();
                Toast.makeText(getApplicationContext(),"บันทึกเรียบร้อย",Toast.LENGTH_SHORT).show();

            }
        });



        btnShowLoca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSaveLocation();
                tvLocation.setText("Location : "+ location2show);
            }
        });


        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }



        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                tvStatus.setText("Status : Connected "+ bt.getConnectedDeviceName());
                Toast.makeText(getApplicationContext(), "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() {
                tvStatus.setText("Status : No connected");
                Toast.makeText(getApplicationContext(), "Connection lost"
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext(), "Unable to connect"
                        , Toast.LENGTH_SHORT).show();
            }
        });

        Button btnConnect = (Button)findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    //bt.disconnected();
                    bt.disconnect();
                } else {
                   // bt.connect("98:D3:37:90:FC:3E");
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });

    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onStart() {
        super.onStart();
        if(!bt.isBluetoothEnabled()) {
            Intent intent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void setup() {

    }

    void loadSave(){
        // Get from the SharedPreferences
        settings = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        noPhone = String.valueOf(settings.getString("NoPhone", String.valueOf(Context.MODE_PRIVATE)));
        message = String.valueOf(settings.getString("Msg", String.valueOf(0)));
        edtNoPhone.setText(noPhone);
        edtText.setText(message);

    }

    void saveLocation(){
        location = getApplicationContext().getSharedPreferences("location", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorLoca = location.edit();
        editorLoca.putString("log",getLocation.getLog());
        editorLoca.putString("lat",getLocation.getLat());
        editorLoca.apply();
    }

    void loadSaveLocation(){
        location = getApplicationContext().getSharedPreferences("location",Context.MODE_PRIVATE);
        log = String.valueOf(location.getString("log",String.valueOf(0)));
        lat = String.valueOf(location.getString("lat",String.valueOf(0)));

        location2show = log +","+lat;
    }


    void getlocation2Show(){
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return ;
            }
        }
        getlocation();// init the contact list
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getlocation();// init the contact list
                } else {
                    // Permission Denied
                    Toast.makeText( this,"Denail Location" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            }

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    void getlocation(){
        GetLocation getLocation = new GetLocation(this);
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
     /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }
     */
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,1,getLocation);

    }


    protected void sendSMSMessage() {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(noPhone, null, message + ",Location:"+location2show, null, null);
            Toast.makeText(getApplicationContext(), "SMS Sent!",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }



}
