package com.example.virtualwallet;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    ImageView btnwifi, btndiscover;
    Boolean wifistate;
    Socket socket;
    TextView name,balance;
    WifiManager wifiManager;
    String devicename=null;
    androidx.appcompat.app.AlertDialog alertDialog;
    WifiP2pManager mManager;
    List<Collection<WifiP2pDevice>> peers = new ArrayList<java.util.Collection<WifiP2pDevice>>();
    String[] deviceNameArray;
    static final int MESSAGE_READ=1;
    WifiP2pDevice[] deviceArray;
    WifiP2pManager.Channel mChannel;
    static public String connectedstatus,connectionstatus;
    ListView listView;
    private static final int PERMISSION_REQUEST_CODE = 200;
    BroadcastReceiver mReceiver;
    IntentFilter mFilter;
    ServerClass serverClass;
    ClientClass clientClass;
Boolean isHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        name=findViewById(R.id.textView3);
        balance=findViewById(R.id.textView4);
        listView=findViewById(R.id.listview);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        btndiscover = findViewById(R.id.imageView2);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        btnwifi = findViewById(R.id.imageView);
        wifistate = putwifistate();
        onoffwifi(wifistate);
initiateuser();
        btndiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {

                } else {
                    requestPermission();
                }
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Discovery Started", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(MainActivity.this, "Discovery failed to Start: Enable Location from Settings ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                  @Override
                      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                      Toast.makeText(MainActivity.this, "Wait for invitation on other device", Toast.LENGTH_SHORT).show();
                      final WifiP2pDevice device=deviceArray[i];
                      WifiP2pConfig config=new WifiP2pConfig();
                        config.deviceAddress=device.deviceAddress;
                        checkPermission();
                        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                connectionstatus="Connected";
                                devicename=device.deviceName;



                            }

                            @Override
                            public void onFailure(int i) {
                                showDialog(device.deviceName,"None","Failed to connect");

                            }
                        });



                       }
                });
        btnwifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent=new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivityForResult(intent,1);
            Boolean statecheck=putwifistate();
            onoffwifi(statecheck);
            }
        });


    }

    private void initiateuser() {
        DBHelper dbHelper=new DBHelper(MainActivity.this);
        Cursor cursor=dbHelper.getPersonData();
        while(cursor.moveToNext()) {
            name.setText("" + cursor.getString(0));
            balance.setText("Balance: " + cursor.getString(1));
        }
    }



    public  boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted){

                    }
                    else {

                        Toast.makeText(MainActivity.this, "You need to give permission of location services", Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mFilter);
        Boolean check=putwifistate();
        onoffwifi(check);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.removeGroup(mChannel,null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);

    }

    private void onoffwifi(Boolean wifistate) {
        if (wifistate == true) {
            btnwifi.setBackgroundResource(R.drawable.icwifion);
        } else {
            btnwifi.setBackgroundResource(R.drawable.icwifioff);
        }
    }
    public void showDialog2(View v){
        ViewGroup viewGroup = v.findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.send_layout
                , viewGroup, false);

        //Now we need an AlertDialog.Builder object
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);
        //finally creating the alert dialog and displaying it
        alertDialog= builder.create();
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        TextView textconnectionstatus=dialogView.findViewById(R.id.textView5);



    }
public void showDialog(String devicename,String connectedstatus,String connectionstatus){
    ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);

    //then we will inflate the custom alert dialog xml that we created
    View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.send_layout
            , viewGroup, false);

    //Now we need an AlertDialog.Builder object
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
    //setting the view of the builder to our custom view that we already inflated
    builder.setView(dialogView);
    //finally creating the alert dialog and displaying it
    alertDialog= builder.create();
    alertDialog.show();
    alertDialog.setCancelable(false);
    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


    TextView textconnectionstatus=dialogView.findViewById(R.id.textView5);
    ImageView cancel=dialogView.findViewById(R.id.imageView4);
    TextView textdevicename=dialogView.findViewById(R.id.textView8);
    TextView textconnectedstatus=dialogView.findViewById(R.id.textView10);
    textdevicename.setText(""+devicename);
    textconnectionstatus.setText(""+connectionstatus);
    textconnectedstatus.setText(""+connectedstatus);
    EditText money=dialogView.findViewById(R.id.editTextNumber);
    Button btnsend=dialogView.findViewById(R.id.button);
    cancel.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
    @Override
    public void onSuccess() {
    }

    @Override
    public void onFailure(int i) {

    }
});
mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure(int i) {

    }
});
            alertDialog.dismiss();
        }
    });
    btnsend.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String msg= money.getText().toString();
            DBHelper dbHelper=new DBHelper(MainActivity.this);
            Cursor cursor=dbHelper.getPersonData();
            int moneyget=0;
            while(cursor.moveToNext()){
                moneyget =Integer.parseInt(cursor.getString(1));
            }
            if(Integer.parseInt(msg)<moneyget) {
                int remaining = moneyget - Integer.parseInt(msg);
                balance.setText("Balance: " + remaining);
            }


            ExecutorService executor=Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if(msg!=null &&isHost==true){
                        int valuetopass=Integer.parseInt(msg);
                        DBHelper dbHelper=new DBHelper(MainActivity.this);
                        Cursor cursor=dbHelper.getPersonData();
                        int moneyget=0;
                        while(cursor.moveToNext()){
                            moneyget =Integer.parseInt(cursor.getString(1));
                        }
                        if(moneyget<valuetopass){
                        }
                        else{
                            int remaining=moneyget-Integer.parseInt(msg);
                            dbHelper.UpdateData(String.valueOf(remaining),name.getText().toString());
                            serverClass.write(msg.getBytes());
                        }




                    }else if(msg!=null && !isHost){
                        int valuetopass=Integer.parseInt(msg);
                        DBHelper dbHelper=new DBHelper(MainActivity.this);
                        Cursor cursor=dbHelper.getPersonData();
                        int moneyget=0;
                        while(cursor.moveToNext()){
                           moneyget =Integer.parseInt(cursor.getString(1));
                        }

                        if(moneyget<valuetopass){
                        }
                        else{
                            int remaining=moneyget-Integer.parseInt(msg);
                            dbHelper.UpdateData(String.valueOf(remaining),name.getText().toString());
                            clientClass.write(msg.getBytes());
                        }


                    }
                }
            });
        }
    });


}
    private Boolean putwifistate() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerlist) {
            if(!peerlist.equals(peers)){
                peers.clear();
                peers.add(peerlist.getDeviceList());
                deviceNameArray=new String[peerlist.getDeviceList().size()];
                deviceArray=new WifiP2pDevice[peerlist.getDeviceList().size()];
                int index=0;
                for(WifiP2pDevice device:peerlist.getDeviceList()){
                    deviceNameArray[index]=device.deviceName;
                    deviceArray[index]=device;
                    index++;
                }
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,deviceNameArray);
                listView.setAdapter(adapter);
            }
            if(peers.size()==0){
                Toast.makeText(MainActivity.this, "No Device Found!!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

WifiP2pManager.ConnectionInfoListener connectionInfoListener=new WifiP2pManager.ConnectionInfoListener() {
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        final InetAddress groupOwnerAddress=wifiP2pInfo.groupOwnerAddress;
        if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
            connectedstatus="Host";
            isHost=true;
            serverClass=new ServerClass();
            serverClass.start();
            showDialog(devicename,connectedstatus,connectionstatus);

        }
        else if(wifiP2pInfo.groupFormed){
            connectedstatus="Client";
            isHost=false;
            clientClass =new ClientClass(groupOwnerAddress);
            clientClass.start();

        }
    }
};

    private class ServerClass extends Thread {

        ServerSocket serverSocket;
        private InputStream inputStream;
        private OutputStream outputStream;
public void write(byte[] bytes){
    try {
        outputStream.write(bytes);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
        @Override
        public void run() {
            try {
                serverSocket=new ServerSocket(8888);
                socket=serverSocket.accept();
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
                ExecutorService executor=Executors.newSingleThreadExecutor();
                Handler handler=new Handler(getMainLooper());
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        byte[] buffer=new byte[1024];
                        int bytes;
                        while(socket!=null){

                            try {
                                bytes=inputStream.read(buffer);
                                if(bytes>0){
                                    int finalbytes=bytes;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            String tempmsg=new String(buffer,0,finalbytes);
                                            DBHelper dbHelper=new DBHelper(MainActivity.this);
                                            Cursor curso2=dbHelper.getPersonData();
                                            int getmoney=0;
                                            while(curso2.moveToNext()){
                                                getmoney=Integer.parseInt(curso2.getString(1));
                                            }


                                            int remaining=Integer.parseInt(tempmsg)+getmoney;
                                            Log.d("msghost",tempmsg);
                                            dbHelper.UpdateData(String.valueOf(remaining),name.getText().toString());
                                            Cursor cursor=dbHelper.getPersonData();
                                            while(cursor.moveToNext()) {
                                                name.setText("" + cursor.getString(0));
                                                balance.setText("Balance: " + cursor.getString(1));
                                            }


                                        }
                                    });
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });


        }

        private void UpdateUserData(String tempmsg) {


        }
    }




    private class ClientClass extends Thread {
        private InputStream inputStream;
        private OutputStream outputStream;

        String hostadd;
        public ClientClass(InetAddress hostAddress){
            hostadd= hostAddress.getHostAddress();
            socket=new Socket();

        }
        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostadd,8888),500);
              inputStream=socket.getInputStream();
              outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ExecutorService executor= Executors.newSingleThreadExecutor();
            Handler handler=new Handler(Looper.getMainLooper());
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer=new byte[1024];
                    int bytes;
                    while(socket!=null){
                        try {
                            bytes=inputStream.read(buffer);
                            if(bytes>0){
                                int finalbytes=bytes;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String tempmsg=new String(buffer,0,finalbytes);
                                        DBHelper dbHelper=new DBHelper(MainActivity.this);
                                        Cursor curso2=dbHelper.getPersonData();
                                        int getmoney=0;
                                        while(curso2.moveToNext()){
                                            getmoney=Integer.parseInt(curso2.getString(1));
                                        }
                                        int remaining=Integer.parseInt(tempmsg)+getmoney;
                                        Log.d("msgclient",tempmsg);
                                        dbHelper.UpdateData(String.valueOf(remaining),name.getText().toString());
                                        Cursor cursor=dbHelper.getPersonData();
                                        Toast.makeText(MainActivity.this, "Recevied: "+tempmsg, Toast.LENGTH_SHORT).show();
                                        while(cursor.moveToNext()) {
                                            name.setText("" + cursor.getString(0));
                                            balance.setText("Balance: " + cursor.getString(1));
                                        }
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        }

        private void updateuser(String tempmsg) {
            DBHelper dbHelper=new DBHelper(MainActivity.this);
            dbHelper.UpdateData(tempmsg,name.getText().toString());
            Cursor cursor=dbHelper.getPersonData();
            while(cursor.moveToNext()) {
                name.setText("" + cursor.getString(0));
                balance.setText("Balance: " + cursor.getString(1));
            }

        }
    }









}