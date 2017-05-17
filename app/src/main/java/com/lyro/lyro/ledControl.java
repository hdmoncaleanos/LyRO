package com.lyro.lyro;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.UUID;


public class ledControl extends ActionBarActivity {

    Button btnSndA, btnSndB, btnSndC, btnDis, btnDirIzq, btnDirDer, btnBreak;
    SeekBar brightness;
    TextView lumn;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(ListaDispositivos.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the ledControl
        setContentView(R.layout.activity_led_control);

        //call the widgtes
        btnSndA = (Button)findViewById(R.id.sendA);
        btnSndB = (Button)findViewById(R.id.sendB);
        btnSndC = (Button)findViewById(R.id.sendC);
        btnDis = (Button)findViewById(R.id.sendDis);
        btnBreak = (Button)findViewById(R.id.freno);
        btnDirDer = (Button)findViewById(R.id.diDer);
        btnDirIzq = (Button)findViewById(R.id.dirIzq);

        brightness = (SeekBar)findViewById(R.id.seekBar1);
        lumn = (TextView)findViewById(R.id.lumn);

        new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        btnSndA.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                send("A0000000");      //method to turn on
            }
        });

        btnSndB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                send("B0000000");   //method to turn off
            }
        });

        btnSndC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                send("C0000000");   //method to turn off
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

        btnBreak.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                send("00001" + lumn); //close connection
            }
        });

        btnDirDer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                send("0"+lumn+"2000");   //method to turn off
            }
        });

        btnDirDer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                send("0"+lumn+"1000");   //method to turn off
            }
        });


        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser==true)
                {
                    if (progress<10)
                        lumn.setText("10");
                    else if (progress==100)
                        lumn.setText("99");
                    else
                        lumn.setText(String.valueOf(progress));


                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }

    private void send(String message)
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write(message.toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
