package jp4r7.sendsms;


import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {
    //get acces to location permsion
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    Button btnSave;
    Button btnShowLoca;
    EditText edtNoPhone;
    EditText edtText;
    TextView tvLocation;
    String  noPhone;
    String message;
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

        btnSave = findViewById(R.id.btnSave);
        btnShowLoca = findViewById(R.id.btnShowLoca);
        edtNoPhone = findViewById(R.id.edtNoPhone);
        edtText = findViewById(R.id.edtText);
        tvLocation = findViewById(R.id.tvLocation);

        getlocation2Show();
        loadSave();
        loadSaveLocation();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("NoPhone", Integer.parseInt(edtNoPhone.getText().toString()));
                editor.putString("Msg", edtText.getText().toString());
                // Apply the edits!
                editor.apply();

            }
        });

        btnShowLoca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSaveLocation();
                tvLocation.setText("Location : "+ location2show);
                sendSMSMessage();

            }
        });
    }

    void loadSave(){
        // Get from the SharedPreferences
        settings = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        noPhone = String.valueOf(settings.getInt("NoPhone", Context.MODE_PRIVATE));
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
