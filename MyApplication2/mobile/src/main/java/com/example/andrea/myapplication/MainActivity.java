package com.example.andrea.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity{

    List<Places> places = new ArrayList<Places>();
    ListView listView;
    EditText editText2;
    Location lastLocation;
    LocationManager locationManager;
    MyCurrentLoctionListener locationListener;
    String name;
    Integer num;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        lastLocation = null;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView)findViewById(R.id.listView);
        TabHost tabHost = (TabHost)findViewById(R.id.tabHost2);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("search");
        tabSpec.setContent(R.id.search);
        tabSpec.setIndicator("Search");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("nearbyPlaces");
        tabSpec.setContent(R.id.nearbyPlaces);
        tabSpec.setIndicator("nearbyPlaces");
        tabHost.addTab(tabSpec);
        editText2 = (EditText)findViewById(R.id.editText2);
        final Button addbtn = (Button)findViewById(R.id.button2);


        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "B9Ln0Y0RJGRtREKheCDieOS1S0yXwxNXLPSNOMDx", "zXOA90T7hNw1GNWz4W8UnZJWdSwZJrmvlxgId8rU");
        populate_list();
        editText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addbtn.setEnabled(!editText2.getText().toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Places");
                query.whereContains("Name", editText2.getText().toString());
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException e) {
                        if (e == null) {
                            TextView nameT = (TextView) findViewById(R.id.nameT);
                            nameT.setText(object.getString("Name"));
                            name = object.getString("Name");
                            TextView longT = (TextView) findViewById(R.id.longT);
                            Double longi = object.getParseGeoPoint("Location").getLatitude();
                            longT.setText(longi.toString());
                            TextView magT = (TextView) findViewById(R.id.magT);
                            Double magni = object.getParseGeoPoint("Location").getLongitude();
                            magT.setText(magni.toString());
                            TextView countT = (TextView) findViewById(R.id.countT);
                            num = object.getInt("numberOfVisits");
                            Integer apple = object.getInt("numberOfVisits");
                            countT.setText(apple.toString());
                            nameT.setVisibility(View.VISIBLE);
                            magT.setVisibility(View.VISIBLE);
                            longT.setVisibility(View.VISIBLE);
                            countT.setVisibility(View.VISIBLE);

                        } else {
                            Toast.makeText(getApplicationContext(), "Sorry we don't have " + editText2.getText().toString() + " in our database", Toast.LENGTH_SHORT).show();

                            // something went wrong
                        }
                    }
                });

            }
        });
        int thisConversationId = 12;
        Intent msgHeardIntent = new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction("com.myapp.messagingservice.MY_ACTION_MESSAGE_HEARD")
                .putExtra("conversation_id", thisConversationId);

        PendingIntent msgHeardPendingIntent =
                PendingIntent.getBroadcast(getApplicationContext(),
                        thisConversationId,
                        msgHeardIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        Intent msgReplyIntent = new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction("com.myapp.messagingservice.MY_ACTION_MESSAGE_REPLY")
                .putExtra("conversation_id", thisConversationId);

        PendingIntent msgReplyPendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                thisConversationId,
                msgReplyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

String resultKey = "Reply";
        // Build a RemoteInput for receiving voice input in a Car Notification
        RemoteInput remoteInput = new RemoteInput.Builder(resultKey)
                .setLabel(getApplicationContext().getString(R.string.notification_reply))
                .build();

// Create an unread conversation object to organize a group of messages
// from a particular sender.
        NotificationCompat.CarExtender.UnreadConversation.Builder unreadConvBuilder =
                new NotificationCompat.CarExtender.UnreadConversation.Builder("newconv")
                        .setReadPendingIntent(msgHeardPendingIntent)
                        .setReplyAction(msgReplyPendingIntent, remoteInput);



        unreadConvBuilder.addMessage(name + num)
                .setLatestTimestamp(System.currentTimeMillis());

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_launcher);


        NotificationManagerCompat msgNotificationManager =
                NotificationManagerCompat.from(getApplicationContext());
        msgNotificationManager.notify(null,1
                , notificationBuilder.build());

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyCurrentLoctionListener();



        if (locationManager != null) {
            if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,Process.myPid(),Process.myUid())==PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) locationListener);
            }
        }
    }
    private class LocationListAdapter extends ArrayAdapter<Places>{
        public LocationListAdapter(){ super(MainActivity.this, R.layout.list_viewitem, places);}
        @Override
        public View getView(int position, View view, ViewGroup parent){
            if(view == null){
                view = getLayoutInflater().inflate(R.layout.list_viewitem, parent, false);
            }
            Places currentPlace = places.get(position);
            TextView textView = (TextView) view.findViewById(R.id.textView);
            textView.setText(currentPlace.getName());
            TextView textView1 = (TextView) view.findViewById(R.id.textView2);
            textView1.setText(currentPlace.getLongi().toString());
            TextView textView2 = (TextView) view.findViewById(R.id.textView3);
            textView2.setText(currentPlace.getMag().toString());
            TextView textView3 = (TextView) view.findViewById(R.id.textView4);
            textView3.setText(currentPlace.getNumberOfvisits().toString());
            return view;
        }
    }
    private void populate_list(){
        ParseQuery<ParseObject> query  = new ParseQuery<ParseObject>("Places");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objectList, ParseException e) {
                if (e == null) {
                    for(int i = 0;i<objectList.size();i++){
                        ParseObject parseObject = objectList.get(i);
                        places.add(new Places(parseObject.getString("Name"),
                                parseObject.getParseGeoPoint("Location").getLatitude(),
                                parseObject.getParseGeoPoint("Location").getLongitude(), parseObject.getInt("numberOfVisits")));
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"Sorry we don't have "+ "Name" + " in our database",Toast.LENGTH_SHORT).show();

                }
            }

        });
        ArrayAdapter<Places> adapter = new LocationListAdapter();
        listView.setAdapter(adapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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


    public class MyCurrentLoctionListener implements LocationListener {


        public void onLocationChanged(Location location) {
            location.getLatitude();
            location.getLongitude();
            addIfNotExist(location);
            Toast.makeText(getApplicationContext(),"current"+ location.getLongitude(),Toast.LENGTH_SHORT).show();
            lastLocation = location;
            if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,Process.myPid(),Process.myUid())==PackageManager.PERMISSION_GRANTED)
                locationManager.removeUpdates(locationListener);
            locationManager = null;
        }



        public void onStatusChanged(String s, int i, Bundle bundle) {

        }


        public void onProviderEnabled(String s) {

        }

        public void onProviderDisabled(String s) {

        }
    }

    void addIfNotExist(Location loc) {
        final ParseGeoPoint point = new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Places");
        query.whereWithinKilometers("Location", point, 0.5);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    object.increment("numberOfVisits");
                    object.saveInBackground();

                } else {
                    ParseObject newObject = new ParseObject("Places");
                    newObject.put("Name", "Unknown");
                    newObject.put("numberOfVisits", 1);
                    newObject.put("Location", point);
                    newObject.saveInBackground();
                }

            }

        });
    }
}
