package org.cliente.followme;

import java.util.List;

import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.TextView;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager ;
import android.content.BroadcastReceiver;
import android.widget.Toast;

// Tutorial: http://mobiforge.com/developing/story/sms-messaging-android

public class FollowMeActivity extends Activity implements LocationListener
{
	private LocationManager mgr;
	private TextView output;
	private String best;
	
	private String phone;
	private String message;
	private boolean semaforo;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	
		mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
		output = (TextView) findViewById(R.id.output);
		
		log("Location providers:" );
		dumpProviders();
	
		Criteria criteria = new Criteria();
		best = mgr.getBestProvider(criteria, true);
		log("\nBest provider is: " + best);
	
		log("\nLocations (starting with last known):" );
		Location location = mgr.getLastKnownLocation(best);
		dumpLocation(location);
		
		
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		// Start updates (doc recommends delay >= 60000 ms)
		mgr.requestLocationUpdates(best, 60000, 1, this);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		// Stop updates to save power while app paused
		mgr.removeUpdates(this);
	}
	
	public void onLocationChanged(Location location)
	{
		dumpLocation(location);
	}
	
	public void onProviderDisabled(String provider)
	{
		log("\nProvider disabled: " + provider);
	}
	
	public void onProviderEnabled(String provider) 
	{
		log("\nProvider enabled: " + provider);
	}
	
	public void onStatusChanged(String provider, int status,Bundle extras)
	{
		log("\nProvider status changed: " + provider + ", status="+ S[status] + ", extras=" + extras);
		
	}
	
	// Define human readable names
	private static final String[] A = { "invalid" , "n/a" , "fine" , "coarse" };
	private static final String[] P = { "invalid" , "n/a" , "low" , "medium" ,	"high" };
	private static final String[] S = { "out of service" ,"temporarily unavailable" , "available" };
	private static final int FORMAT_DEGREES = 0;
	
	/** Write a string to the output window */
	private void log(String string)
	{
		output.append(string + "\n" );
	}
	
	/** Write information from all location providers */
	private void dumpProviders() 
	{
		List<String> providers = mgr.getAllProviders();
		for (String provider : providers) 
		{
			dumpProvider(provider);
		}
	}
	
	/** Write information from a single location provider */
	private void dumpProvider(String provider)
	{
		LocationProvider info = mgr.getProvider(provider);
		StringBuilder builder = new StringBuilder();
		builder.append("LocationProvider[" )
		.append("name=" )
		.append(info.getName())
		.append(",enabled=" )
		.append(mgr.isProviderEnabled(provider))
		.append(",getAccuracy=" )
		.append(A[info.getAccuracy() + 1])
		.append(",getPowerRequirement=" )
		.append(P[info.getPowerRequirement() + 1])
		.append(",hasMonetaryCost=" )
		.append(info.hasMonetaryCost())
		.append(",requiresCell=" )
		.append(info.requiresCell())
		.append(",requiresNetwork=" )
		.append(info.requiresNetwork())
		.append(",requiresSatellite=" )
		.append(info.requiresSatellite())
		.append(",supportsAltitude=" )
		.append(info.supportsAltitude())
		.append(",supportsBearing=" )
		.append(info.supportsBearing())
		.append(",supportsSpeed=" )
		.append(info.supportsSpeed())
		.append("]" );
		log(builder.toString());
	}
	
	/** Describe the given location, which might be null */
	private void dumpLocation(Location location)
	{
		if (location == null)
		{
			log("\nLocation[unknown]" );
		}
		else
		{
			log("\n" + location.toString());
			
			/** send Message Geographics Position from Client**/
			phone = "5556"; //2292423424";
			message = "\nROCA: " + location.toString();// esta a punto de terminar programa de localizacion de personas. Esto es una prueba";
			message = createPosGeoMSN(location).toString();
			sendSMSMonitor(phone, message);
			
		}
	}
	
	/** Create a Position Geographic Message from Client**/
	private StringBuilder createPosGeoMSN(Location location)
	{
		String time = Long.toString(location.getTime());
		String latitude = Double.toString(location.getLatitude());
		String longitude = Double.toString(location.getLongitude());
		String provider = location.getProvider();
		
		
		
		StringBuilder builder = new StringBuilder();
		builder.append("$+id,")
		.append(",")
		.append(time)
		.append(",")
		.append(latitude)
		.append(",")
		.append(longitude)
		.append(",")
		.append(provider);
		//builder.append("LocationProvider[" )
		
		
		return builder;
	}
	
	//---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String msg)
    {        
    	// make sure the fields are not empty
        if (phoneNumber.length()>0 && msg.length()>0)
        {
        	// call the sms manager
            PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, FollowMeActivity.class), 0);
                SmsManager sms = SmsManager.getDefault();
                // this is the function that does all the magic
                sms.sendTextMessage(phoneNumber, null, msg, pi, null);
                
                log("\nMensaje Enviado a[ " + phone + " ]" );
        }
        else
        {
        	// display message if text fields are empty
            Toast.makeText(getBaseContext(),"All field are required",Toast.LENGTH_SHORT).show();
        	log("\nPor alguna razon tu mensaje no se envio");
        }       
    }    

    //---sends an SMS message to another device---
    private void sendSMSMonitor(String phoneNumber, String message)
    {        
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
 
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
            new Intent(SENT), 0);
 
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
            new Intent(DELIVERED), 0);
 
        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", 
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));
 
        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;                        
                }
            }
        }, new IntentFilter(DELIVERED));        
 
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);        
    }
    
   
    
}//Fin de Clase

