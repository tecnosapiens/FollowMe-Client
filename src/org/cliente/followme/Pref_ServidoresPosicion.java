package org.cliente.followme;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Pref_ServidoresPosicion extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings2);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
        menu.add(Menu.NONE, 0, 0, "Show current settings");
        return super.onCreateOptionsMenu(menu);
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case 0:
                startActivity(new Intent(this, MenuMensajesSinPanico.class));
//            	Toast.makeText(getBaseContext(), "sleccionada opcion", 
//                        Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }
    
}//fin de clase