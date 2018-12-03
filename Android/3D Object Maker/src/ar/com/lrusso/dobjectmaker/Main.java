package ar.com.lrusso.dobjectmaker;

import android.text.Html;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity
	{
	private WebView webView;
	private static ValueCallback<Uri> mUploadMessage;  
	private static ValueCallback<Uri[]> mUploadMessage5;
	private final static int FILECHOOSER_RESULTCODE=1;
	private Context myContext;
	private Activity myActivity;

	@Override protected void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		myContext = this;
		myActivity = this;
		
		showLowResDeviceMessage();

		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        
		// LOADING THE HTML DOCUMENT
		String resultHTML = loadAssetTextAsString("3DObjectMaker.htm");
        
		// LOADING THE WEBVIEW
		webView.loadDataWithBaseURL(null, resultHTML, null, "utf-8", null);
        
        webView.setWebViewClient(new myWebClient());
        webView.setWebChromeClient(new WebChromeClient()
        	{
        	// For Android 3.0+
        	public void openFileChooser(ValueCallback<Uri> uploadMsg)
        		{  
                mUploadMessage = uploadMsg;  
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);  
                i.addCategory(Intent.CATEGORY_OPENABLE);  
                i.setType("*/*");  
                startActivityForResult(Intent.createChooser(i,"File Chooser"), FILECHOOSER_RESULTCODE);  
        		}

            // For Android 3.0+
        	public void openFileChooser(ValueCallback uploadMsg, String acceptType)
        		{
        		mUploadMessage = uploadMsg;
        		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        		i.addCategory(Intent.CATEGORY_OPENABLE);
        		i.setType("*/*");
        		startActivityForResult(Intent.createChooser(i,"File Browser"),FILECHOOSER_RESULTCODE);
        		}

            //For Android 4.1
        	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
        		{
        		mUploadMessage = uploadMsg;  
        		Intent i = new Intent(Intent.ACTION_GET_CONTENT);  
        		i.addCategory(Intent.CATEGORY_OPENABLE);  
        		i.setType("*/*");  
        		startActivityForResult(Intent.createChooser(i,"File Chooser"),FILECHOOSER_RESULTCODE);
        		}

            // For Android 5.0
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams)
            	{
        		mUploadMessage5 = uploadMsg;
        		Intent i = new Intent(Intent.ACTION_GET_CONTENT);  
        		i.addCategory(Intent.CATEGORY_OPENABLE);  
        		i.setType("*/*");  
        		startActivityForResult(Intent.createChooser(i,"File Chooser"),FILECHOOSER_RESULTCODE);
                return true;
            	}
            
        	@Override public boolean onConsoleMessage(ConsoleMessage consoleMessage)
        		{
        		String stringMessage = consoleMessage.message();
        		if (stringMessage.startsWith("STLFILE---") || stringMessage.startsWith("SCENEFILE---"))
        			{
            	    String path = Environment.getExternalStorageDirectory() + File.separator  + "3DObjectMaker";

            	    // Creates the folder
            	    File folder = new File(path);
            	    folder.mkdirs();
            	    
            	    // Getting the format and content that the file will have
            		String fileFormat = "";
            		String fileContent = "";
            		if (stringMessage.startsWith("STLFILE---"))
        				{
            			fileFormat = ".stl";
            			fileContent = stringMessage.substring(10,stringMessage.length());
        				}
            		else if (stringMessage.startsWith("SCENEFILE---"))
        				{
            			fileFormat = ".scene";
            			fileContent = stringMessage.substring(12,stringMessage.length());
        				}

            	    // Getting the name that the file will have
        			String fileName = getResources().getString(R.string.textFileName);
            		boolean fileCanBeCreated = false;
            		int counter = 0;
            		while(fileCanBeCreated==false)
            			{
            			if (counter==0)
            				{
                	    	File fileChecker = new File(folder, fileName + fileFormat);
                	    	if (fileChecker.exists()==false)
                	    		{
                	    		fileName = fileName + fileFormat;
                	    		fileCanBeCreated = true;
                	    		}
                	    		else
                	    		{
                	    		counter = counter + 1;
                	    		}
            				}
            			else
            				{
                	    	File fileChecker = new File(folder, fileName + "(" + counter + ")" + fileFormat);
                	    	if (fileChecker.exists()==false)
                	    		{
                	    		fileName = fileName + "(" + counter + ")" + fileFormat;
                	    		fileCanBeCreated = true;
                	    		}
                	    		else
                	    		{
                	    		counter = counter + 1;
                	    		}
            				}
            			}
            		
            		// Writing the file
            	    try
        	    		{
            	    	File file = new File(folder, fileName);
            	    	file.createNewFile();
            	    	FileOutputStream fOut = new FileOutputStream(file);
            	    	OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            	    	myOutWriter.append(fileContent);
            	    	myOutWriter.close();
            	    	fOut.flush();
            	    	fOut.close();
        	    		}
        	    		catch(Exception e)
        	    		{
        	    		}            		
            		
            		// Toast message
            		Toast.makeText(myContext, myContext.getResources().getString(R.string.textFileSaved), Toast.LENGTH_SHORT).show();
        			}
                return true;
        		}
        	});
        
        if (android.os.Build.VERSION.SDK_INT>=23) //MARSHMALLOW
        	{
        	try
        		{
        		iniciarVerificacionMarshmallow();	
        		}
				catch(Exception e)
				{
				}
        	}
		}

	 @Override protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	 	{
		if (resultCode == RESULT_OK)
			{
			if(requestCode==FILECHOOSER_RESULTCODE)  
				{  
				if (android.os.Build.VERSION.SDK_INT>=21) //LOLLIPOP
	        		{
					try
	        			{
						Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
						mUploadMessage5.onReceiveValue(new Uri[]{result});  
						mUploadMessage5 = null;  
	        			}
	        			catch(Exception e)
	        			{
	        			}
	        		}
	        	}
	        	else
	        	{
	        	try
	        		{
		       		if (null == mUploadMessage) return;
		       		Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();  
		       		mUploadMessage.onReceiveValue(result);  
		       		mUploadMessage = null;  
	        		}
	        		catch(Exception e)
	        		{
	        		}
	        	}
	        }
			else
			{
			try
				{
				if (mUploadMessage5 != null)
					{
					mUploadMessage5.onReceiveValue(null);
					mUploadMessage5 = null;
					}

				if (mUploadMessage != null)
					{
					mUploadMessage.onReceiveValue(null);
					mUploadMessage = null;
					}
				}
				catch(Exception e)
				{
				}
			}
	 	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
		{
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
		}

	@Override public boolean onOptionsItemSelected(MenuItem item)
		{
	    switch (item.getItemId())
    		{
    		case R.id.action_settings:
    		View menuItemView = findViewById(R.id.action_settings);
    		PopupMenu popupMenu = new PopupMenu(this, menuItemView); 
    		popupMenu.inflate(R.menu.popup_menu);
    		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
				{  
    			public boolean onMenuItemClick(MenuItem item)
    				{
        			if (item.getTitle().toString().contains(getResources().getString(R.string.textZoomOut)))
        				{
        				try
        					{
            				webView.getSettings().setUseWideViewPort(false);
            				webView.getSettings().setUseWideViewPort(true);
            				webView.setInitialScale(1);        				
        					}
        					catch(Exception e)
        					{
        					}
        				}
        			else if (item.getTitle().toString().contains(getResources().getString(R.string.textPrivacy)))
						{
	    				clickInPrivacy();
						}
	    			else if (item.getTitle().toString().contains(getResources().getString(R.string.textAbout)))
    					{
    					clickInAbout();
    					}
    				return true;  
    				}
				});
    		popupMenu.show();
    		return true;
    		
    		default:
    		return super.onOptionsItemSelected(item);
    		}
		}
	
	@Override public boolean onKeyUp(int keyCode, KeyEvent event)
		{
		try
			{
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
				{
				clickInExit();
				return false;
				}
			}
			catch(NullPointerException e)
			{
			}
		return super.onKeyUp(keyCode, event);
		}
	
	private void clickInExit()
		{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
			{
			public void onClick(DialogInterface dialog, int which)
				{
				switch (which)
					{
					case DialogInterface.BUTTON_POSITIVE:
					myActivity.finish();
					System.exit(0);
					break;
		
					case DialogInterface.BUTTON_NEGATIVE:
					break;
					}
				}
			};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.textExit).setPositiveButton(R.string.textYes, dialogClickListener).setNegativeButton(R.string.textNo, dialogClickListener).show();
		}
	
	private void clickInPrivacy()
		{
		LayoutInflater inflater = LayoutInflater.from(this);
		View view=inflater.inflate(R.layout.privacy, null);

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);  
		alertDialog.setTitle(getResources().getString(R.string.textPrivacy));  
		alertDialog.setView(view);
		alertDialog.setPositiveButton(getResources().getString(R.string.textOK), new DialogInterface.OnClickListener()
			{
			public void onClick(DialogInterface dialog, int whichButton)
				{
				}
			});
		alertDialog.show();
		}
	
	private void clickInAbout()
		{
		String years = "";
		String value = getResources().getString(R.string.textAboutMessage);
		int lastTwoDigits = Calendar.getInstance().get(Calendar.YEAR) % 100;
		if (lastTwoDigits<=5)
			{
			years = "2005";
			}
			else
			{
			years ="2005 - 20" + String.valueOf(lastTwoDigits).trim();
			}
	
		value = value.replace("ANOS", years);
	
		TextView msg = new TextView(this);
		msg.setText(Html.fromHtml(value));
		msg.setPadding(10, 20, 10, 25);
		msg.setGravity(Gravity.CENTER);
		float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
		float size = new EditText(this).getTextSize() / scaledDensity;
		msg.setTextSize(size);

		new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.textAbout)).setView(msg).setIcon(R.drawable.ic_launcher).setPositiveButton(getResources().getString(R.string.textOK),new DialogInterface.OnClickListener()
			{
			public void onClick(DialogInterface dialog,int which)
				{
				}
			}).show();
		}
		
	private String loadAssetTextAsString(String name)
		{
        BufferedReader in = null;
        try
        	{
            StringBuilder buf = new StringBuilder();
            InputStream is = getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str=in.readLine())!=null)
            	{
                if (isFirst)
                	{
                    isFirst = false;
                	}
                	else
                    {
                	buf.append("\n");
                    }
                buf.append(str);
            	}
            return buf.toString();
        	}
        	catch (IOException e)
        	{
        	}
        	finally
        	{
            if (in!=null)
            	{
                try
                	{
                    in.close();
                	}
                	catch (IOException e)
                	{
                	}
            	}
        	}
        return null;
		}

	@TargetApi(Build.VERSION_CODES.M)
	public void iniciarVerificacionMarshmallow()
		{
		if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
			{
			String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
											Manifest.permission.WRITE_EXTERNAL_STORAGE};
			requestPermissions(PERMISSIONS_STORAGE, 123);
			}
		}
		
	@TargetApi(Build.VERSION_CODES.M)
	@Override public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
		{
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==123)
        	{
        	if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        		{
        		}
        	}
		}
	
	private String getImportantNoteShowed()
		{
		String result = "";
		DataInputStream in = null;
		try
			{
			in = new DataInputStream(openFileInput("note.cfg"));
			for (;;)
    			{
				result = result + in.readUTF();
    			}
			}
			catch (Exception e)
			{
			}
		try
			{
			in.close();
			}
			catch(Exception e)
			{
			}
		return result;
		}

	private void setImportantNoteShowed()
		{
		try
			{
			DataOutputStream out = new DataOutputStream(openFileOutput("note.cfg", Context.MODE_PRIVATE));
			out.writeUTF("123");
			out.close();
			}
    		catch(Exception e)
    		{
    		}
		}

	public void showLowResDeviceMessage()
		{
		if (getImportantNoteShowed()=="")
			{
			ContextThemeWrapper themedContext = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
			new AlertDialog.Builder(themedContext).setCancelable(false).setTitle(getResources().getString(R.string.textZoomOutNoteTitle)).setMessage(getResources().getString(R.string.textZoomOutNoteText)).setPositiveButton(getResources().getString(R.string.textOK),new DialogInterface.OnClickListener()
				{
				public void onClick(DialogInterface dialog,int which)
					{
					setImportantNoteShowed();
					}
				}).show();
			}
		}
	}