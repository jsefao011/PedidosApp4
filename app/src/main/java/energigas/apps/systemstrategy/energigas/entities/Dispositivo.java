package energigas.apps.systemstrategy.energigas.entities;

import com.orm.SugarRecord;

import energigas.apps.systemstrategy.energigas.utils.Constants;

/**
 * Created by kelvi on 05/09/2016.
 */

public class Dispositivo extends SugarRecord {
    private String codigo;
    private String descripcion;
    private int dmId;
    private int empresaId;
    private Boolean estado;
    private String fechaActualizacion;
    private String fechaCreacion;
    private String mAC;
    private int marcaId;
    private String modelo;
    private String serie;
    private int tipoId;
    private int usuarioActualizacion;
    private int usuarioCreacion;
    public Dispositivo() {
    }

    public Dispositivo(String codigo, String descripcion, int dmId, int empresaId, Boolean estado, String fechaActualizacion, String fechaCreacion, String mAC, int marcaId, String modelo, String serie, int tipoId, int usuarioActualizacion, int usuarioCreacion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.dmId = dmId;
        this.empresaId = empresaId;
        this.estado = estado;
        this.fechaActualizacion = fechaActualizacion;
        this.fechaCreacion = fechaCreacion;
        this.mAC = mAC;
        this.marcaId = marcaId;
        this.modelo = modelo;
        this.serie = serie;
        this.tipoId = tipoId;
        this.usuarioActualizacion = usuarioActualizacion;
        this.usuarioCreacion = usuarioCreacion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getDmId() {
        return dmId;
    }

    public void setDmId(int dmId) {
        this.dmId = dmId;
    }

    public int getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(int empresaId) {
        this.empresaId = empresaId;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public String getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(String fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getmAC() {
        return mAC;
    }

    public void setmAC(String mAC) {
        this.mAC = mAC;
    }

    public int getMarcaId() {
        return marcaId;
    }

    public void setMarcaId(int marcaId) {
        this.marcaId = marcaId;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public int getTipoId() {
        return tipoId;
    }

    public void setTipoId(int tipoId) {
        this.tipoId = tipoId;
    }

    public int getUsuarioActualizacion() {
        return usuarioActualizacion;
    }

    public void setUsuarioActualizacion(int usuarioActualizacion) {
        this.usuarioActualizacion = usuarioActualizacion;
    }

    public int getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(int usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    public Dispositivo getDispositivoTipoUsua(int vint_tipoDispositivo, int vint_UsuarioId){

        Dispositivo mdat_dispositivo = Dispositivo.findWithQuery(Dispositivo.class,"Select dispositivo.* from dispositivo inner join \n" +
                                        "\t\t\t vehiculo_dispositivo on dispositivo.DM_ID = vehiculo_dispositivo.DM_ID inner join\n" +
                                        "\t\t\t vehiculo on vehiculo_dispositivo.VE_ID = vehiculo.VE_ID inner join\n" +
                                        "\t\t\t vehiculo_usuario on vehiculo.VE_ID = vehiculo_usuario.VE_ID\n" +
                                        "\t\t\t where dispositivo.TIPO_ID = ? and\n" +
                                        "\t\t\t vehiculo_usuario.USUARIO_ID = ?",new String[]{vint_tipoDispositivo+"",vint_UsuarioId+""})
                                        .get(Constants.CURRENT);

        return mdat_dispositivo;
    }

}
