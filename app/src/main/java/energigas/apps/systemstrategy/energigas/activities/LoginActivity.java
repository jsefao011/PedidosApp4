package energigas.apps.systemstrategy.energigas.activities;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;

import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import butterknife.BindView;
import butterknife.ButterKnife;
import energigas.apps.systemstrategy.energigas.R;
import energigas.apps.systemstrategy.energigas.entities.CajaLiquidacionDetalle;
import energigas.apps.systemstrategy.energigas.entities.Concepto;
import energigas.apps.systemstrategy.energigas.entities.Establecimiento;
import energigas.apps.systemstrategy.energigas.entities.NotificacionCajaDetalle;
import energigas.apps.systemstrategy.energigas.entities.PedidoDetalle;
import energigas.apps.systemstrategy.energigas.entities.Servidores;
import energigas.apps.systemstrategy.energigas.fragments.DialogGeneral;
import energigas.apps.systemstrategy.energigas.fragments.ProgressDialogFragment;
import energigas.apps.systemstrategy.energigas.interfaces.DialogGeneralListener;
import energigas.apps.systemstrategy.energigas.utils.Constants;
import energigas.apps.systemstrategy.energigas.utils.Session;
import energigas.apps.systemstrategy.energigas.utils.Utils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.input_email)
    EditText editTextEmail;
    @BindView(R.id.input_password)
    EditText editTextPassword;
    @BindView(R.id.btn_login)
    AppCompatButton buttonLogin;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private String vstr_CelIMEI;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        if (!Session.getsaveServidores(this)) {

            Servidores distribucion = new Servidores("Distribucion", "192.168.0.158/ServiciosMovil");
            Servidores facturacion = new Servidores("Facturacion", "192.168.0.92/SW");
            distribucion.save();
            facturacion.save();
            Session.setServidores(this, true);
        }
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        vstr_CelIMEI = telephonyManager.getDeviceId();

        ButterKnife.bind(this);
        setToolbar();
        getSupportActionBar().setTitle((Html.fromHtml("<font color=\"#FFFFFF\">" + "Energigas" + "</font>")));

        buttonLogin.setOnClickListener(this);
        checkLogin();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        Toast.makeText(this, "Tiene que iniciar session", Toast.LENGTH_SHORT).show();
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            startActivity(new Intent(getApplicationContext(), MainConfiguraciones.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void conectlogin() {
        String mEmail = editTextEmail.getText().toString();
        String mPassword = editTextPassword.getText().toString();

        if (Concepto.listAll(Concepto.class).size() <= 0)
        {
            Toast.makeText(getApplicationContext(), "Por favor, importe datos generales", Toast.LENGTH_LONG).show();
        }
        else if (mEmail.length() <= 0 || mPassword.length() <= 0)
        {
            Toast.makeText(getApplicationContext(), "Usuario o password Incorrecto", Toast.LENGTH_LONG).show();
        }
        else if(vstr_CelIMEI.isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Codigo IMEI ilegible", Toast.LENGTH_LONG).show();
        }
        else{
            startLogin(mEmail, mPassword,vstr_CelIMEI);
        }
    }

    private void startLogin(String usuario, String clave, String vstr_CelIMEI) {
        ProgressDialogFragment.newIntance(usuario, clave, vstr_CelIMEI).show(getSupportFragmentManager(), "xd");
    }

    private void checkLogin() {
        if (Utils.isSignin(this)) {
            startActivity(new Intent(this, MainActivity.class));
            LoginActivity.this.finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                conectlogin();
                break;
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }


}

