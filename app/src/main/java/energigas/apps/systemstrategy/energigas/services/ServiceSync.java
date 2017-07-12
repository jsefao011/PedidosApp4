package energigas.apps.systemstrategy.energigas.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import energigas.apps.systemstrategy.energigas.R;
import energigas.apps.systemstrategy.energigas.activities.CuentaResumenActivity;
import energigas.apps.systemstrategy.energigas.activities.MainActivity;
import energigas.apps.systemstrategy.energigas.activities.MainStationActivity;
import energigas.apps.systemstrategy.energigas.asyntask.AsynTaskUbicacion;
import energigas.apps.systemstrategy.energigas.broadcast.ScreenReceiver;
import energigas.apps.systemstrategy.energigas.entities.AlertaEntity;
import energigas.apps.systemstrategy.energigas.entities.CajaLiquidacion;
import energigas.apps.systemstrategy.energigas.entities.Concepto;
import energigas.apps.systemstrategy.energigas.entities.Dispositivo;
import energigas.apps.systemstrategy.energigas.entities.ConfiguracionFireBase;
import energigas.apps.systemstrategy.energigas.entities.NotificacionCajaDetalle;
import energigas.apps.systemstrategy.energigas.entities.NotificacionPedido;
import energigas.apps.systemstrategy.energigas.entities.NotificacionSaldoInicial;
import energigas.apps.systemstrategy.energigas.entities.Pedido;
import energigas.apps.systemstrategy.energigas.entities.PedidoDetalle;
import energigas.apps.systemstrategy.energigas.entities.Usuario;
import energigas.apps.systemstrategy.energigas.entities.Vehiculo;
import energigas.apps.systemstrategy.energigas.interfaces.MyLocationListener;
import energigas.apps.systemstrategy.energigas.utils.Constants;
import energigas.apps.systemstrategy.energigas.utils.NotificacionManagerUtils;
import energigas.apps.systemstrategy.energigas.utils.Session;

/**
 * Created by kelvi on 25/12/2016.
 */


public class ServiceSync extends Service implements MyLocationListener.mUbicacionLT {

    private BroadcastReceiver mReceiver;

    private DatabaseReference mDatabase;
    private DatabaseReference myRefFondos;
    private DatabaseReference mAtenPedidosPrecios;
    private DatabaseReference mAtenPedidosAgregados;
    private DatabaseReference mUsuarios;
    private CajaLiquidacion cajaLiquidacion;
    private static final String TAG = "ServiceSync";
    private NotificacionManagerUtils notificar;
    private ChildEventListener childEventListener;
    private ChildEventListener childEventListenerPrecios;
    private ChildEventListener childEventListenerPedidosAgregados;
    private ChildEventListener childEventListenerUsuario;
    private Concepto conceptoIGV;
    private Dispositivo dispositivo;

    /*Variables Gps*/
    private LocationManager mlocManager;
    private MyLocationListener mlocListener;
    String mLatitud;
    String mLong;
    private Context context;
    /**/


    @Override
    public void onCreate() {


        registerListenerExport();
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(getApplicationContext(), FirebaseOptions.fromResource(getApplicationContext()));

        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        cajaLiquidacion = CajaLiquidacion.getCajaLiquidacion(Session.getCajaLiquidacion(this).getLiqId() + "");
        if (cajaLiquidacion != null) {
            listenerFirebaseFondos();
            listenerFirebasePrecios();
            listenerPedidosAgregados();
            hasPermissions(context);
            positionLocation();
            listenerUsuario();
            //initServiceLocation(context);
        }


        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        myRefFondos.removeEventListener(childEventListener);
        mAtenPedidosPrecios.removeEventListener(childEventListenerPrecios);
        mAtenPedidosAgregados.removeEventListener(childEventListenerPedidosAgregados);
    }


    private void registerListenerExport() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    private void listenerFirebaseFondos() {


        myRefFondos = mDatabase.child(Constants.FIREBASE_CHILD_FONDOS);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("ServiceSync_service", "EXECUTE: " + dataSnapshot.getKey());
                NotificacionSaldoInicial notificacionSaldoInicial = dataSnapshot.getValue(NotificacionSaldoInicial.class);
                CajaLiquidacion liquidacionGuardar = CajaLiquidacion.getCajaLiquidacion(cajaLiquidacion.getLiqId() + "");
                Long liqFirebase = new Long(notificacionSaldoInicial.getLiqId());
                Long liqCaja = new Long(liquidacionGuardar.getLiqId());
                if (liqFirebase.equals(liqCaja)) {

                    if (!Session.isCantidadSaldoInicial(getApplicationContext(), notificacionSaldoInicial.getSaldoInicial() + "")) {
                        liquidacionGuardar.setSaldoInicial(notificacionSaldoInicial.getSaldoInicial());
                        Long aLong = liquidacionGuardar.save();
                        Log.d(TAG, "ACTUALIZO: " + aLong);
                        AlertaEntity alertaEntity = new AlertaEntity(1, "Saldo Inicial Modificado", "Su saldo inicial se ha modificado a " + liquidacionGuardar.getSaldoInicial(), R.drawable.logo, getApplicationContext(), CuentaResumenActivity.class, "IR", R.drawable.logo, null);
                        notificar(alertaEntity);
                    }


                }


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        myRefFondos.addChildEventListener(childEventListener);

    }

    private void listenerFirebasePrecios() {
        mAtenPedidosPrecios = mDatabase.child(Constants.FIREBASE_CHILD_ATEN_PEDIDO).child(cajaLiquidacion.getLiqId() + "");
        childEventListenerPrecios = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Log.d(TAG, "PRECIO: -> " + dataSnapshot.getKey());
                //Log.d(TAG, "PRECIO: -> " + s);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                System.out.print("Hola Objeto");
                System.out.print(dataSnapshot);
                conceptoIGV = Session.getConceptoIGV();
                NotificacionCajaDetalle notificacionCajaDetalle = dataSnapshot.getValue(NotificacionCajaDetalle.class);
                //if (!Session.isUpdatePrecing(getApplicationContext(), notificacionCajaDetalle.getPrecio() + ""))
               // {
                    Log.d(TAG, "PRECIO: -> " + dataSnapshot.getKey());
                    Log.d(TAG, "PRECIO: -> " + s);
                    /*Validar que solo el precio se esta modificando*/
                    long mlon_LiquidacionId = notificacionCajaDetalle.getLiId();
                    int mint_LiquidacionDetalleId = notificacionCajaDetalle.getLidId();
                    PedidoDetalle mobj_pedidoDetalle = PedidoDetalle.findWithQuery(PedidoDetalle.class,"Select * from CAJA_LIQUIDACION_DETALLE inner join " +
                            "PEDIDO on CAJA_LIQUIDACION_DETALLE.PE_ID = PEDIDO.PE_ID inner join " +
                            "PEDIDO_DETALLE on PEDIDO.PE_ID = PEDIDO_DETALLE.PE_ID " +
                            "where  CAJA_LIQUIDACION_DETALLE.LI_ID = ? and CAJA_LIQUIDACION_DETALLE.LID_ID = ?",new String[]{mlon_LiquidacionId +"",mint_LiquidacionDetalleId+""}).get(Constants.CURRENT);


                    /*********************************************/
                    if(mobj_pedidoDetalle != null)
                    {
                        Log.d(TAG, "PRECIO: -> EE=" + mobj_pedidoDetalle.getPrecio() + "!=" + notificacionCajaDetalle.getPrecio());
                        if(mobj_pedidoDetalle.getPrecio() != notificacionCajaDetalle.getPrecio())
                        {
                            Pedido pedido = Pedido.getPedidoById(notificacionCajaDetalle.getPeId() + "");
                            Bundle bundle = new Bundle();
                            bundle.putLong("ESTABLECIMIENTO", pedido.getEstablecimientoId());
                            double precioUnitario = (Double.parseDouble(conceptoIGV.getDescripcion()) * notificacionCajaDetalle.getPrecio()) + notificacionCajaDetalle.getPrecio();
                            PedidoDetalle pedidoDetalle = PedidoDetalle.getPedidoDetalleByPedido(pedido.getPeId() + "").get(0);
                            pedidoDetalle.setPrecio(notificacionCajaDetalle.getPrecio());
                            pedidoDetalle.setPrecioUnitario(precioUnitario);
                            Long estadoPedidoDetalle = pedidoDetalle.save();
                            // bundle.putLong("PEDIDO", pedido.getPeId());
                            if (estadoPedidoDetalle > 0)
                            {
                                notificar(new AlertaEntity(2, "Precio Modificado", "Nuevo precio para el pedido: " + pedido.getSerie() + "-" + pedido.getNumero(), R.drawable.logo, getApplicationContext(), MainStationActivity.class, "Ir", R.drawable.logo, bundle));
                            }
                        }
                    }

                //}
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mAtenPedidosPrecios.addChildEventListener(childEventListenerPrecios);
    }

    private void listenerPedidosAgregados() {

        mAtenPedidosAgregados = mDatabase.child(Constants.FIREBASE_CHILD_ATEN_ADD_PEDIDO);
        childEventListenerPedidosAgregados = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "CantidadEjecuacion: KEY " + dataSnapshot.getKey());
                Long cajaLiqui = new Long(cajaLiquidacion.getLiqId());
                NotificacionPedido notificacionPedido = dataSnapshot.getValue(NotificacionPedido.class);

                if (notificacionPedido.getCajaLiquidacion().equals(cajaLiqui)) {
                    if (!Session.isAddPedido(getApplicationContext(), notificacionPedido.getCantidad())) {
                        Intent intentExportService = new Intent(getApplicationContext(), ServiceImport.class);
                        intentExportService.setAction(Constants.ACTION_IMPORT_SERVICE);
                        startService(intentExportService);
                    }

                }


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mAtenPedidosAgregados.addChildEventListener(childEventListenerPedidosAgregados);
    }

    private void listenerUsuario()
    {
        mUsuarios = mDatabase.child(Constants.FIREBASE_CHILD_USUARIO).child(cajaLiquidacion.getUsuarioId()+"");
        childEventListenerUsuario = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "ACTUALIZO USUARIOS: " + dataSnapshot);
                String mstr_value = dataSnapshot.getValue(String.class);
                String mstr_key = dataSnapshot.getKey();
                if(mstr_key.equals("printerMAC"))
                {
                    Dispositivo mdat_dispositivo = new Dispositivo();
                    mdat_dispositivo = mdat_dispositivo.getDispositivoTipoUsua(Constants.TIPO_ID_DEVICE_IMPRESORA, cajaLiquidacion.getUsuarioId());
                    Log.d(TAG, "ACTUALIZO INPRESORA: " + Constants.TIPO_ID_DEVICE_IMPRESORA +"-" +cajaLiquidacion.getUsuarioId());
                    if(mdat_dispositivo != null)
                    {
                        String PrintMac = mdat_dispositivo.getmAC();
                        if(!PrintMac.equals(mstr_value))
                        {
                            mdat_dispositivo.setmAC(mstr_value);
                            Long mlon_estadoDisposito = mdat_dispositivo.save();
                            if (mlon_estadoDisposito > 0)
                            {
                                AlertaEntity alertaEntity = new AlertaEntity(3, "Impresora Modificada", "Se cambio de impresora a este equipo", R.drawable.logo, getApplicationContext(), MainActivity.class, "Ir", R.drawable.logo, null);
                                Log.d(TAG, "ACTUALIZO INPRESORA: " + mstr_value);
                                notificar(alertaEntity);
                            }
                        }
                    }
                }
            ///fat
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mUsuarios.addChildEventListener(childEventListenerUsuario);
    }

    private void notificar(AlertaEntity alertaEntity) {
        notificar = new NotificacionManagerUtils(alertaEntity);
        notificar.showNotificationCustom();
    }


    /*Services Position*/


    private void positionLocation() {
        int handler = 1000 * 60 * 5;//15000 seconds
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener(this);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, handler, 0, mlocListener);
    }

    @Override
    public void onLocalPosition(Location location) {
        Usuario usuario = Usuario.getUsuario(Session.getSession(this).getUsuIUsuarioId() + "");
        mLatitud = String.valueOf(location.getLatitude());
        mLong = String.valueOf(location.getLongitude());
        Vehiculo vehiculo = Vehiculo.getVehiculo(usuario.getUsuIUsuarioId() + "");
        new AsynTaskUbicacion().execute(String.valueOf(usuario.getUsuIUsuarioId()), mLong, mLatitud, String.valueOf( cajaLiquidacion.getLiqId()), String.valueOf(vehiculo.getVeId()));
    }


    /*PERMISOS ANDROID6.0 API(23)*/
    public static boolean hasPermissions(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;

    }





}
