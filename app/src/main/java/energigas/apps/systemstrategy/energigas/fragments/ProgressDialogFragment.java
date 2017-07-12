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
import energigas.apps.systemstrategy.energigas.interfaces.OnLoginAsyntaskListener;
import energigas.apps.systemstrategy.energigas.utils.Constants;
import energigas.apps.systemstrategy.energigas.utils.Session;
import energigas.apps.systemstrategy.energigas.utils.Utils;

/**
 * Created by kelvi on 10/08/2016.
 */

public class ProgressDialogFragment extends DialogFragment implements OnLoginAsyntaskListener {
    private static final String TAG = "ProgressDialogFragment";

    private boolean allowStateLoss = false;
    private boolean shouldDismiss = false;
    private Context context;
    private DatabaseReference mDatabase;
    private DatabaseReference myRef;
    public static ProgressDialogFragment newIntance(@NonNull String usuario, @NonNull String clave, @NonNull String vstr_CelIMEI) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString("usuario", usuario);
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
        myRef = mDatabase.child(Constants.FIREBASE_CHILD_USUARIO);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setRetainInstance(true);
        setCancelable(false);
        String usuario = getArguments().getString("usuario");
        String clave = getArguments().getString("clave");
        final String mstr_CelIMEI = getArguments().getString("CelIMEI");
        Log.d(TAG, "get CelIMEI : " + mstr_CelIMEI);
        List<Usuario> ss = Usuario.listAll(Usuario.class);
        Log.d(TAG, "COUNT : " + ss.size());

        new LoginTask(this).execute(usuario, clave);

        //region Comentado
        /*
        if (ss.size() > 0) {
            final List<Usuario> dbUsuarioa = Usuario.find(Usuario.class, " usu_V_Usuario = ?  and usu_V_Password = ? ", new String[]{usuario, clave});
            // Log.d(TAG, "LOGIN : " + dbUsuarioa.get(0).getUsuVUsuario());
            if (dbUsuarioa.size() > 0) {

                int mint_UsuarioId = dbUsuarioa.get(0).getUsuIUsuarioId();

                myRef.child(mint_UsuarioId+"").addListenerForSingleValueEvent(
                        new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                DispositivosFireBase dispositivosFireBase = dataSnapshot.getValue(DispositivosFireBase.class);
                                Log.d(TAG, "dispositivosFireBase : " + dispositivosFireBase.getCelIMEI());
                                if (mstr_CelIMEI.equals(dispositivosFireBase.getCelIMEI()))
                                {
                                    //validacion de firte base
                                    Persona persona = Persona.findById(Persona.class, dbUsuarioa.get(0).getUsuIPersonaId());
                                    dbUsuarioa.get(0).setPersona(persona);
                                    Usuario objUsuario = dbUsuarioa.get(0);
                                    /Guardar la sesion/
                                    Session.saveSession(getActivity(), objUsuario);
                                    initMain();
                                    dismiss();
                                }
                                else
                                {
                                    setMessage("Dispositivo Restringido");
                                    dismiss();
                                }
                            }
                            @Override
                            public void onCancelled (DatabaseError databaseError)
                            {
                                setMessage("Dispositivo sin acceso a la red");
                                dismiss();
                            }
                        });
            } else {
                new LoginTask(this).execute(usuario, clave);
            }

        } else {
            new LoginTask(this).execute(usuario, clave);
        }

        */
        //endregion

        return dialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_dialog_progress, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }


    @Override
    public void onError(@NonNull String message) {
        Log.d(TAG, "onError" + " " + message);
        setMessage(message);
        dismiss();
    }

    @Override
    public void onSuccess(final CajaLiquidacion cajaLiquidacion) {

        final String mstr_CelIMEI = getArguments().getString("CelIMEI");
        int mint_UsuarioId = cajaLiquidacion.getUsuarioId();
        if(mint_UsuarioId == 0)
        {
            Utils.saveStateLogin(context, true);
            Session.saveCajaLiquidacion(getActivity(), cajaLiquidacion);
            initMain();
            dismiss();
            getActivity().finish();
            return;
        }
        myRef.child(mint_UsuarioId+"").addListenerForSingleValueEvent(
                new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        ConfiguracionFireBase configuracionFireBase = dataSnapshot.getValue(ConfiguracionFireBase.class);
                        if(configuracionFireBase == null)
                        {
                            setMessage("Usuario sin IMEI");
                            dismiss();

                        }else if (mstr_CelIMEI.equals(configuracionFireBase.getCelIMEI()))
                        {
                            Utils.saveStateLogin(context, true);
                            Session.saveCajaLiquidacion(getActivity(), cajaLiquidacion);
                            initMain();
                            dismiss();
                            getActivity().finish();
                        }
                        else
                        {
                            setMessage("Dispositivo Restringido");
                            dismiss();
                        }
                    }
                    @Override
                    public void onCancelled (DatabaseError databaseError)
                    {
                        setMessage("Dispositivo sin acceso a la red");
                        dismiss();
                    }
                });





    }

    @Override
    public void onErrorProcedure(@NonNull String message) {
        setMessage(message);
        dismiss();

    }

    private void setMessage(String message) {


        Toast.makeText(context, "" + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCredentialsFail() {
        dismiss();
        setMessage("Credenciales erroneas");
    }

    @Override
    public Context getContextActivity() {
        return getActivity();
    }

    private void initMain() {

        startActivity(new Intent(getActivity(), MainActivity.class));

        // getActivity().finish();
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
