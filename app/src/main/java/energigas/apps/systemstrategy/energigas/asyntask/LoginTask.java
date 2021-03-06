package energigas.apps.systemstrategy.energigas.asyntask;


import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.UiThread;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orm.SugarRecord;
import com.orm.SugarTransactionHelper;


import org.json.JSONObject;


import java.util.List;


import energigas.apps.systemstrategy.energigas.apiRest.ManipuleData;
import energigas.apps.systemstrategy.energigas.apiRest.RestAPI;
import energigas.apps.systemstrategy.energigas.entities.Acceso;
import energigas.apps.systemstrategy.energigas.entities.BEGeneral;
import energigas.apps.systemstrategy.energigas.entities.CajaLiquidacion;
import energigas.apps.systemstrategy.energigas.entities.Privilegio;
import energigas.apps.systemstrategy.energigas.entities.Rol;
import energigas.apps.systemstrategy.energigas.entities.RolAcceso;
import energigas.apps.systemstrategy.energigas.entities.RolPrivilegio;
import energigas.apps.systemstrategy.energigas.entities.RolUsuario;
import energigas.apps.systemstrategy.energigas.entities.UbicacionGeoreferencia;
import energigas.apps.systemstrategy.energigas.entities.Usuario;
import energigas.apps.systemstrategy.energigas.interfaces.OnLoginAsyntaskListener;
import energigas.apps.systemstrategy.energigas.utils.Constants;
import energigas.apps.systemstrategy.energigas.utils.NetworkUtil;
import energigas.apps.systemstrategy.energigas.utils.Session;
import energigas.apps.systemstrategy.energigas.utils.Utils;

/**
 * Created by kelvi on 09/08/2016.
 */

public class LoginTask extends AsyncTask<String, String, String> implements SugarTransactionHelper.Callback {
    private static final String TAG = "LoginTask";

    private JSONObject jsonObjectUsuario = null;

    private JSONObject jsonLiquidacion = null;

    private OnLoginAsyntaskListener aListener;
    private Context context;
    private int result = 0;
    private Usuario objUsuario;

    private CajaLiquidacion cajaLiquidacion;
    private ManipuleData manipuleData;

    public LoginTask(OnLoginAsyntaskListener loginAsyntaskListener) {

        this.aListener = loginAsyntaskListener;
        this.context = aListener.getContextActivity();
        manipuleData = new ManipuleData();

    }


    @Override
    protected String doInBackground(String... strings) {
        Log.d(TAG, "PING " + NetworkUtil.hasActiveInternetConnection(context));

    /*     if (!NetworkUtil.hasActiveInternetConnection(context)) {
            Log.d(TAG, "Error pin ");
            result = 2;

            return null;
        }
    */
        RestAPI restAPI = new RestAPI();
        String usuario = strings[0];
        String clave = strings[1];
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {

            jsonObjectUsuario = restAPI.fobj_ObtenerUsuario(usuario, clave);


            if (Utils.isSuccessful(jsonObjectUsuario)) {

                objUsuario = mapper.readValue(Utils.getJsonObResult(jsonObjectUsuario), Usuario.class);

                if (objUsuario.getUsuIUsuarioId() < 0) {
                    result = 1;
                } else {
                    jsonLiquidacion = restAPI.fobj_ObtenerLiquidacion(objUsuario.getUsuIUsuarioId());
                    Log.e(TAG,"LIQUIDACION: " +jsonLiquidacion);
                    cajaLiquidacion = mapper.readValue(Utils.getJsonObResult(jsonLiquidacion), CajaLiquidacion.class);
                    SugarTransactionHelper.doInTransaction(this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error: " + e.getMessage());
            result = 2;
        }


        return null;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        switch (result) {
            case 1://Credenciales incorrectas
                aListener.onCredentialsFail();
                break;
            case 2://Error en la conexion
                aListener.onError("Error en la conexion al servidor");
                break;
            case 3://Error al insertar data
                aListener.onError("Error en guardar los datos");
                break;
            case 4://Error en la base de datos del servidor
                aListener.onErrorProcedure("Erroe en la base de datos del servidor");
                break;
            case 5://Ejecuto correctamente
                Session.saveSession(context, objUsuario);
                aListener.onSuccess(cajaLiquidacion);

                break;
        }
    }

    @Override
    public void manipulateInTransaction() {

        objUsuario.save();

        objUsuario.getPersona().save();
        objUsuario.getPersona().getUbicacion().save();
        List<Rol> rols = objUsuario.getItemsRoles();
        SugarRecord.saveInTx(rols);
        int count = 0;
        for (Rol rol : rols) {
            List<Acceso> accesos = rol.getItemsAccesos();
            RolUsuario rolUsuario = new RolUsuario(objUsuario.getUsuIUsuarioId() + "_" + rol.getIdRol(), rol.getIdRol(), objUsuario.getUsuIUsuarioId());
            rolUsuario.save();
            SugarRecord.saveInTx(accesos);
            for (Acceso acceso : accesos) {

                RolAcceso rolAcceso = new RolAcceso(rol.getIdRol() + "_" + acceso.getIdAcceso(), rol.getIdRol(), acceso.getIdAcceso(), true);
                rolAcceso.save();
                SugarRecord.saveInTx(acceso.getItemsPrivielgios());

                for (Privilegio privilegio : acceso.getItemsPrivielgios()) {
                    RolPrivilegio rolPrivilegio = new RolPrivilegio(rol.getIdRol() + "_" + privilegio.getAccesoId(), rol.getIdRol(), privilegio.getAccesoId());
                    rolPrivilegio.save();
                    count++;
                }
            }
        }

        if (cajaLiquidacion != null) {
            if (cajaLiquidacion.getLiqId() > 0) {
                if (cajaLiquidacion.getPlanDistribucionD() != null) {
                    manipuleData.saveLiquidacion(cajaLiquidacion);
                }
            }

        }

        Usuario usuario = Usuario.find(Usuario.class, "usu_I_Usuario_Id=?", new String[]{objUsuario.getUsuIUsuarioId() + ""}).get(0);
        Session.saveSession(context, usuario);

        result = 5;
    }


    @Override
    public void errorInTransaction(String error) {
        result = 3;
        Log.d(TAG, "ERROR: " + error);
    }
}
