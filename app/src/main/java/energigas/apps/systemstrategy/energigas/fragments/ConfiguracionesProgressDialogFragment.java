package energigas.apps.systemstrategy.energigas.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import butterknife.ButterKnife;
import energigas.apps.systemstrategy.energigas.R;
import energigas.apps.systemstrategy.energigas.activities.MainActivity;
import energigas.apps.systemstrategy.energigas.asyntask.LoginTask;
import energigas.apps.systemstrategy.energigas.entities.CajaLiquidacion;
import energigas.apps.systemstrategy.energigas.entities.ConfiguracionFireBase;
import energigas.apps.systemstrategy.energigas.entities.Usuario;
import energigas.apps.systemstrategy.energigas.utils.Constants;
import energigas.apps.systemstrategy.energigas.utils.Session;
import energigas.apps.systemstrategy.energigas.utils.Utils;

/**
 * Created by kelvi on 10/08/2016.
 */

public class ConfiguracionesProgressDialogFragment extends DialogFragment{
    private static final String TAG = "ProgressDialogFragment";
    private boolean allowStateLoss = false;
    private boolean shouldDismiss = false;
    private Context context;
    private DatabaseReference mDatabase;
    private DatabaseReference myRef;

    IntefaceFirebaseListener mListener;

    // Container Activity must implement this interface
    public interface IntefaceFirebaseListener {
        public void onLoginConfiguracion();
    }

    public static ConfiguracionesProgressDialogFragment newIntance(@NonNull String clave, @NonNull String vstr_CelIMEI)  {
        ConfiguracionesProgressDialogFragment fragment = new ConfiguracionesProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString("clave", clave);
        args.putString("CelIMEI", vstr_CelIMEI);
        Log.d(TAG, "set CelIMEI : " + vstr_CelIMEI);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        context = getActivity().getApplicationContext();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        myRef = mDatabase.child(Constants.FIREBASE_CHILD_DISPOSITIVOS);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
        setCancelable(false);
        final String clave = getArguments().getString("clave");
        String mstr_CelIMEI = getArguments().getString("CelIMEI");
        Log.d(TAG, "get CelIMEI : " + mstr_CelIMEI);
        List<Usuario> ss = Usuario.listAll(Usuario.class);
        Log.d(TAG, "COUNT : " + ss.size());
        mListener = (IntefaceFirebaseListener) getActivity();

        //ConfiguracionFireBase configuracion = new ConfiguracionFireBase("s", "1","123");
        //myRef.child(mstr_CelIMEI).setValue(configuracion);


        myRef.child(mstr_CelIMEI).addListenerForSingleValueEvent(
                new ValueEventListener()
                {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        ConfiguracionFireBase configuracionFireBase = dataSnapshot.getValue(ConfiguracionFireBase.class);

                        if(configuracionFireBase == null){
                            setMessage("Dispositivo sin autorizacion");
                            dismiss();
                        }
                        else if (clave.equals(configuracionFireBase.getClave()))
                        {
                            Salir();

                        }
                        else
                        {
                            Log.d(TAG, "Login : " + clave + "="+ configuracionFireBase.getClave());
                            setMessage("Contraseña incorrecta");
                            dismiss();
                        }

                    }
                    @Override
                    public void onCancelled (DatabaseError databaseError)
                    {

                        setMessage("Dispositivo sin conexion a internet");
                        dismiss();
                    }
                });


        return dialog;
    }

    public void Salir(){
        mListener.onLoginConfiguracion();
        dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_dialog_progress, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }



    private void setMessage(String message) {

        Toast.makeText(context, "" + message, Toast.LENGTH_SHORT).show();
    }

    //keeping dialog after rotation
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }


    @Override
    public void dismissAllowingStateLoss() {
        if (getActivity() != null) { // it's "safer" to dismiss
            shouldDismiss = false;
            super.dismissAllowingStateLoss();
        } else
            allowStateLoss = shouldDismiss = true;
    }

    @Override
    public void dismiss() {
        if (getActivity() != null) { // it's "safer" to dismiss
            shouldDismiss = false;
            super.dismiss();
        } else {
            shouldDismiss = true;
            allowStateLoss = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //check if we should dismiss the dialog after rotation
        if (shouldDismiss) {
            if (allowStateLoss)
                dismissAllowingStateLoss();
            else
                dismiss();
        }
    }

}
