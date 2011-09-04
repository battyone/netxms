package org.netxms.ui.android.main.activities;

import org.netxms.ui.android.R;
import org.netxms.ui.android.service.ClientConnectorService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Abstract base class for all activities in the client. Implements
 * common functionality for connecting to service and handling common items
 * in options menu.
 */
public abstract class AbstractClientActivity extends Activity implements ServiceConnection
{
	protected ClientConnectorService service;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		onCreateStep2(savedInstanceState);
		startService(new Intent(this, ClientConnectorService.class));
		bindService(new Intent(this, ClientConnectorService.class), this, 0);
	}
	
	/**
	 * Called by AbstractClientActivity.onCreate before service binding
	 * to allow inherited classes to do initialization before onServiceConnected call
	 * 
	 * @param savedInstanceState
	 */
	protected abstract void onCreateStep2(Bundle savedInstanceState);

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.settings:
				startActivity(new Intent(this, ConsolePreferences.class));
				return true;
			case R.id.disconnect:
				if (!(service == null)) 
				{
					service.clearNotifications();
					service.stopSelf();
					System.exit(0);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/* (non-Javadoc)
	 * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
	 */
	@Override
	public void onServiceConnected(ComponentName name, IBinder binder)
	{
		service = ((ClientConnectorService.ClientConnectorBinder)binder).getService();
	}

	/* (non-Javadoc)
	 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
	 */
	@Override
	public void onServiceDisconnected(ComponentName name)
	{
	}
}
