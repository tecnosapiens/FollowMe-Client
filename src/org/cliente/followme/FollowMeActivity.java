package org.cliente.followme;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;



import android.R.bool;
import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.telephony.SmsManager ;
import android.text.method.ScrollingMovementMethod;
import android.content.BroadcastReceiver;
import android.widget.Toast;

// Tutorial: http://mobiforge.com/developing/story/sms-messaging-android

public class FollowMeActivity extends Activity implements LocationListener
{
	private LocationManager mgr;
	private static TextView output;
	private static Button btnPanico;
	private String best;
	
	private String[] servidores;
	private String message;
	
	private int tiempoEnvioMensajePanico;
	private int tiempoEnvioMensajeNoPanico;
	private boolean IsenviarMensajeNoPanico;
	private int distanciaActualizacionPosicion;
	
	
	private boolean IsBtnPanicoPulsado;
	
	//Obtenemos la hora actual
	Calendar calendario;

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		IsBtnPanicoPulsado = false;
		//Obtenemos la hora actual
		calendario = new GregorianCalendar();
		
			
		//se leen las preferencias del usuario
		SharedPreferences sharedPrefs =	PreferenceManager.getDefaultSharedPreferences(FollowMeActivity.this);
		
		tiempoEnvioMensajePanico = Integer.parseInt(sharedPrefs.getString("tiempo_envio_mensajes_panico", "10000"));
		tiempoEnvioMensajeNoPanico = Integer.parseInt(sharedPrefs.getString("tiempo_envio_mensaje_sin_panico", "300000"));
	  	IsenviarMensajeNoPanico = sharedPrefs.getBoolean("enviar_mensajes_sin_panico", false);
	  	distanciaActualizacionPosicion = 100;
	  	
	  	servidores = new String[3];
	  	servidores[0] = new String(sharedPrefs.getString("servidor1", "-1"));
	  	
    	
	  	  
//	  	 Log.i("Preferencias", "\n tiempoEnvioMensajePanico (sistema): " + Integer.toString(tiempoEnvioMensajePanico) );
//		 Log.i("Preferencias", "\n tiempoEnvioMensajeNoPanico (sistema): " + Integer.toString(tiempoEnvioMensajeNoPanico) );
//		 Log.i("Preferencias", "\n enviarMensajeNoPanico (sistema): " + Boolean.toString(IsenviarMensajeNoPanico) );
//		 Log.i("Preferencias", "\n servidor1 (sistema): " + servidores[0]);
			
	
		output = (TextView) findViewById(R.id.output);
		output.setMovementMethod(new ScrollingMovementMethod());
		
		
		
		
		btnPanico = (ToggleButton) findViewById(R.id.BtnPanico);
		
		

		btnPanico.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
				//Hacer rutina de gestion de posicion geografica por boton panico activado
				if (((ToggleButton) btnPanico).isChecked()) 
				{
					IsBtnPanicoPulsado = true;
					btnPanicoActivado();
					btnPanico.setBackgroundColor(getResources().getColor(R.color.panicColor));

				} else
				{
					IsBtnPanicoPulsado = false;
					btnPanicoDesactivado();
					btnPanico.setBackgroundColor(getResources().getColor(R.color.relaxColor));


				}


			}
		});
		
		if(IsenviarMensajeNoPanico)
		{
			envioMensajesPoscionNOPanico();
		}
		
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		//se leen las preferencias del usuario
		SharedPreferences sharedPrefs =	PreferenceManager.getDefaultSharedPreferences(FollowMeActivity.this);
		
		tiempoEnvioMensajePanico = Integer.parseInt(sharedPrefs.getString("tiempo_envio_mensajes_panico", "10000"));
		tiempoEnvioMensajeNoPanico = Integer.parseInt(sharedPrefs.getString("tiempo_envio_mensaje_sin_panico", "300000"));
	  	IsenviarMensajeNoPanico = sharedPrefs.getBoolean("enviar_mensajes_sin_panico", false);
	  	distanciaActualizacionPosicion = 100;
	  	
	  	servidores = new String[3];
	  	servidores[0] = new String(sharedPrefs.getString("servidor1", "-1"));
	  	
		
		// Start updates (doc recommends delay >= 60000 ms)
		if(IsenviarMensajeNoPanico)
		{
			mgr.requestLocationUpdates(best, tiempoEnvioMensajeNoPanico, distanciaActualizacionPosicion, this);
		}
		
		
		
//		//Recuperacion de las preferencias de usuarios para envio de mensajes de posicion
//		// En panico = Usuario a activado la alarma de SOS
//		// No panico = la aplicacion puede enviar mensaje de posicion periodicamente. El tiempo de envio dependera 
//		// de las preferencias de envio proporcionadas por el usuario
//		
//		SharedPreferences sharedPrefs =	PreferenceManager.getDefaultSharedPreferences(FollowMeActivity.this);
//	  	  
//    	StringBuilder builder = new StringBuilder();
//	  	 
//	  	  builder.append("\n enviar_mensajes_sin_panico (usuario): " + sharedPrefs.getBoolean("enviar_mensajes_sin_panico", false));
//	  	  builder.append("\n tiempo_envio_mensaje_sin_panico (usuario): " + sharedPrefs.getString("tiempo_envio_mensaje_sin_panico", "600000"));
//	  	  builder.append("\n tiempo_envio_mensajes_panico (usuario): " + sharedPrefs.getString("tiempo_envio_mensajes_panico", "60000"));
//	  	  builder.append("\n servidor1 (usuario): " + sharedPrefs.getString("servidor1", "-1"));
//	  	  
//	  	 Log.i("Preferencias", builder.toString() );   	
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		// Stop updates to save power while app paused
		if(!IsBtnPanicoPulsado)
		{
			mgr.removeUpdates(this);
		}
	}
	
	//******************************************************************
	//		FUNCIONES DE ADMINISTRACION DE POSICION GEOGRAFICA
	//
	//******************************************************************
	
	
	/*
	 * (non-Javadoc)
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
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
			//phone = "5554"; //2292423424";
			message = "\nROCA: " + location.toString();// esta a punto de terminar programa de localizacion de personas. Esto es una prueba";
			message = createPosGeoMSN(location).toString();
			sendSMSMonitor(servidores[0], message);
			
		}
	}
	
	
	//******************************************************************
	//		FUNCIONES DE ADMINISTRACION IMPRESION DATOS EN GUI
	//
	//******************************************************************
		
	
	// Define human readable names
	private static final String[] A = { "invalid" , "n/a" , "fine" , "coarse" };
	private static final String[] P = { "invalid" , "n/a" , "low" , "medium" ,	"high" };
	private static final String[] S = { "out of service" ,"temporarily unavailable" , "available" };
	/** Write a string to the output window */
	
	public static void log(String string)
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
	
	//******************************************************************
	//		FUNCIONES DE ADMINISTRACION DE ENVIO MENSAJES POSICION
	//
	//******************************************************************

	
	/** Create a Position Geographic Message from Client**/
	private StringBuilder createPosGeoMSN(Location location)
	{
		
		String hora = calendario.getTime().toLocaleString();
		String time = Long.toString(location.getTime());
		
		Log.i("tiempoMensajes", hora + " -----> " + time);
		
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
                
                log("\nMensaje Enviado a[ " + servidores[0] + " ]" );
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
    
  //******************************************************************
  	//		FUNCIONES DE ADMINISTRACION DE MENUS DE CONFIGURACION DE USUARIO
  	//
  	//******************************************************************
  		
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_principal, menu);
    return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) 
	    {
		    case R.id.MenuMensajes:
		    //lblMensaje.setText("Opcion 1 pulsada!");
		    	//Toast.makeText(getBaseContext(), "Menu de Mensajes pulsada",Toast.LENGTH_SHORT).show();
		    	startActivity(new Intent(this, Pref_MensajesPosicion.class));
		    return true;
		    case R.id.MenuServidores:
		    	startActivity(new Intent(this, Pref_ServidoresPosicion.class));
		    return true;
		    case R.id.MenuServicios:
		    //lblMensaje.setText("Opcion 3 pulsada!");;
		    return true;
		    default:
		    return super.onOptionsItemSelected(item);
	    }
    }
    
    
    //******************************************************************
    //		FUNCIONES DE ADMINISTRACION DE CONTROLES EN GUI
    //
    //******************************************************************

    
    private void btnPanicoActivado()
    {
    	if(IsenviarMensajeNoPanico)
    	{
    		mgr.removeUpdates(this);
    	}
    	mgr = (LocationManager) getSystemService(LOCATION_SERVICE);


    	log("Location providers:" );
    	dumpProviders();

    	Criteria criteria = new Criteria();
    	best = mgr.getBestProvider(criteria, true);
    	log("\nBest provider is: " + best);

    	mgr.requestLocationUpdates(best, tiempoEnvioMensajePanico, distanciaActualizacionPosicion, this);

    	log("\nLocations (starting with last known):" );
    	Location location = mgr.getLastKnownLocation(best);
    	dumpLocation(location);

    }
    
    private void envioMensajesPoscionNOPanico()
    {
    	mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

    	log("Location providers:" );
    	dumpProviders();

    	Criteria criteria = new Criteria();
    	best = mgr.getBestProvider(criteria, true);
    	log("\nBest provider is: " + best);

    	mgr.requestLocationUpdates(best, tiempoEnvioMensajeNoPanico, distanciaActualizacionPosicion, this);

    	log("\nLocations (starting with last known):" );
    	Location location = mgr.getLastKnownLocation(best);
    	dumpLocation(location);
    }
    
    
    private void btnPanicoDesactivado()
    {
    	mgr.removeUpdates(this);
    	
    	if(IsenviarMensajeNoPanico)
    	{
    		mgr = (LocationManager) getSystemService(LOCATION_SERVICE);


        	log("Location providers:" );
        	dumpProviders();

        	Criteria criteria = new Criteria();
        	best = mgr.getBestProvider(criteria, true);
        	log("\nBest provider is: " + best);

        	mgr.requestLocationUpdates(best, tiempoEnvioMensajeNoPanico, distanciaActualizacionPosicion, this);

        	log("\nLocations (starting with last known):" );
        	Location location = mgr.getLastKnownLocation(best);
        	dumpLocation(location);
    	}
    	
    	

    }
    
}//Fin de Clase

