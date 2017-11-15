package edu.hm.cs.ig.passbutler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class AccountListActivity extends AppCompatActivity {

    private static final String TAG = AccountListActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_list);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }







//        try {
//            String fileName = getString(R.string.accounts_file_name);
//            String string = "hello world!";
//            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
//            fos.write(string.getBytes());
//            fos.close();
//            Log.v(TAG, "File written");
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }
//
//
//
//
//        try {
//            String fileName = getString(R.string.accounts_file_name);
//            FileInputStream fis = openFileInput(fileName);
//
//
//
//
//
//
//            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
//            XmlPullParser xmlParser = xmlPullParserFactory.newPullParser();
//            xmlParser.setInput(fis, Xml.Encoding.UTF_8.toString());
//
//
//
//            Log.v(TAG, "File read (Input encoding:" + Xml.Encoding.UTF_8.toString());
//            Scanner scanner = new Scanner(fis);
//            while(scanner.hasNext()) {
//                Log.v(TAG, scanner.next());
//            }
//            scanner.close();
//            fis.close();
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }







//        try {
//
//
//
//
//            int event = xmlParser.getEventType();
//            while (event != XmlPullParser.END_DOCUMENT)  {
//                String name=xmlParser.getName();
//                switch (event){
//                    case XmlPullParser.START_TAG:
//                        break;
//
//                    case XmlPullParser.END_TAG:
//                        if(name.equals("temperature")){
//                            temperature = xmlParser.getAttributeValue(null,"value");
//                        }
//                        break;
//                }
//                event = xmlParser.next();
//            }
//
//        }
//        catch(XmlPullParserException e)
//        {
//            e.printStackTrace();
//        }
//        catch(IOException e)
//        {
//            e.printStackTrace();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.account_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        else if(id == R.id.password_generator_menu_item) {
            Intent intent = new Intent(this, PasswordGeneratorActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.sync_menu_item) {
            Intent intent = new Intent(this, SyncActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.settings_menu_item) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
