package energigas.apps.systemstrategy.energigas.entities;

/**
 * Created by SCIEV on 19/05/2017.
 */

public class ConfiguracionFireBase {
    public String printerMAC;
    public String celIMEI;
    public String clave;
    public ConfiguracionFireBase(){}

    public ConfiguracionFireBase(String PrinterMAC, String CelIMEI, String Clave)
    {
        printerMAC = PrinterMAC;
        celIMEI = CelIMEI;
        clave = Clave;
    }
    public String getPrinterMAC() {
        return printerMAC;
    }

    public void setPrinterMAC(String printerMAC) {
        this.printerMAC = printerMAC;
    }

    public String getCelIMEI() {
        return celIMEI;
    }

    public void setCelIMEI(String celIMEI) {
        this.celIMEI = celIMEI;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

}
