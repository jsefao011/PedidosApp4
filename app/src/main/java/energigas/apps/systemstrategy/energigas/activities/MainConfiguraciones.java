package energigas.apps.systemstrategy.energigas.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import energigas.apps.systemstrategy.energigas.R;
import energigas.apps.systemstrategy.energigas.adapters.ServidoresAdapter;
import energigas.apps.systemstrategy.energigas.asyntask.AsynObtenerDatosGenerales;
import energigas.apps.systemstrategy.energigas.entities.CajaLiquidacion;
import energigas.apps.systemstrategy.energigas.entities.ConfiguracionFireBase;
import energigas.apps.systemstrategy.energigas.entities.Servidores;
import energigas.apps.systemstrategy.energigas.fragments.ConfiguracionesProgressDialogFragment;
import energigas.apps.systemstrategy.energigas.fragments.DialogGeneral;
import energigas.apps.systemstrategy.energigas.interfaces.DialogGeneralListener;
import energigas.apps.systemstrategy.energigas.interfaces.OnAsyntaskListener;
import energigas.apps.systemstrategy.energigas.utils.Constants;
import energigas.apps.systemstrategy.energigas.utils.Session;
import energigas.apps.systemstrategy.energigas.utils.Utils;

public class MainConfiguraciones extends AppCompatActivity implements ServidoresAdapter.OnServidoresClickListener, OnAsyntaskListener, ConfiguracionesProgressDialogFragment.IntefaceFirebaseListener {

    @BindView(R.id.recycler_view)

    RecyclerView recyclerView;

    @BindView(R.id.btnImportarDatosGenerales)
    Button buttonImportar;

    private ProgressDialog progressDialog;
    private String vstr_CelIMEI;
    private Servidores vobj_servidores;
    private String vstr_servidor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        vstr_CelIMEI = telephonyManager.getDeviceId();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_configuraciones);
        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Importando");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initViews();
    }

    private void initViews() {

        List<Servidores> servidores = Servidores.listAll(Servidores.class);
        ServidoresAdapter servidoresAdapter = new ServidoresAdapter(servidores, this, this);
        recyclerView.setAdapter(servidoresAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        buttonImportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                new AsynObtenerDatosGenerales(MainConfiguraciones.this).execute();
            }
        });
    }

    @Override
    public void onServidoresClickListener(Servidores servidores, View view, int typeClick) {
        initDialog(servidores);
    }

    private void initDialog(final Servidores servidores) {
        Log.d("MainConfiguraciones", servidores.getName());
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_editar_servidores, null);
        dialogBuilder.setView(dialogView);
        TextView textViewTitulo = (TextView) dialogView.findViewById(R.id.textTitle);
        final EditText editText = (EditText) dialogView.findViewById(R.id.editDireccion);
        editText.setText(servidores.getDescription());
        Button buttonCancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        Button buttonOk = (Button) dialogView.findViewById(R.id.btn_ok);
        textViewTitulo.setText(servidores.getName());



        final AlertDialog alertDialog = dialogBuilder.create();
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().equals("")) {
                   // servidores.setDescription(editText.getText().toString());
                   // servidores.save();
                    String mstr_Text = editText.getText().toString();
                    iniDialogValidarCambio();
                    vobj_servidores = servidores;
                    vstr_servidor = mstr_Text;
               }

                alertDialog.dismiss();
                //initViews();
            }
        });

        alertDialog.show();
    }

    private void iniDialogValidarCambio(){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_validarcambios_servidores, null);
        dialogBuilder.setView(dialogView);
        final EditText editTextPassw = (EditText) dialogView.findViewById(R.id.inputPassw);
        Button buttonCancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        Button buttonOk = (Button) dialogView.findViewById(R.id.btn_ok);
        final AlertDialog alertDialog = dialogBuilder.create();
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mstr_Password = editTextPassw.getText().toString();
                startLogin(mstr_Password,vstr_CelIMEI);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public void setMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onLoadSuccess(String message, CajaLiquidacion cajaLiquidacion) {
        progressDialog.dismiss();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoadError(String message) {
        progressDialog.dismiss();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoadErrorProcedure(String message) {
        progressDialog.dismiss();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void startLogin(String clave, String vstr_CelIMEI) {
        ConfiguracionesProgressDialogFragment.newIntance(clave, vstr_CelIMEI).show(getSupportFragmentManager(), "xd");
    }

    @Override
    public void onLoginConfiguracion() {
        vobj_servidores.setDescription(vstr_servidor);
        vobj_servidores.save();
        setMessage("Se guardo los cambios");;
        initViews();
    }
}
