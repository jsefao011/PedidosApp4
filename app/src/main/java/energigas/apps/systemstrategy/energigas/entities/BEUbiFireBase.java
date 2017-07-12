package energigas.apps.systemstrategy.energigas.entities;

/**
 * Created by SCIEV on 26/04/2017.
 */

public class BEUbiFireBase {
    private int idAgente;
    private String mLatitud;
    private String mLong;
    private int idVehiculo;

    public BEUbiFireBase(int IdAgente,String MLatitud,String MLong,int IdVehiculo) {
        idAgente = IdAgente;
        mLatitud= MLatitud;
        mLong = MLong;
        idVehiculo = IdVehiculo;
    }

    public BEUbiFireBase() {
    }
    public int getIdAgente() {
        return idAgente;
    }

    public void setIdAgente(int idAgente) {
        this.idAgente = idAgente;
    }

    public String getmLatitud() {
        return mLatitud;
    }

    public void setmLatitud(String mLatitud) {
        this.mLatitud = mLatitud;
    }

    public String getmLong() {
        return mLong;
    }

    public void setmLong(String mLong) {
        this.mLong = mLong;
    }

    public int getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(int idVehiculo) {
        this.idVehiculo = idVehiculo;
    }
}
