package alg2obligatorio;

import alg2obligatorio.Retorno.Resultado;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Sistema implements ISistema {
    private ABBEmpresa empresas;
    private int cantPuntos;
    private int contadorPuntos;
    private GrafoPuntos mapa;
    private ArrayList<Ciudad> ciudades;
    private ArrayList<DC> datacenters;
    public enum TipoPunto {CIUDAD,DATACENTER};
    private Pattern pat = Pattern.compile("^[_a-z0-9-]+(.[_a-z0-9-]+)*@[a-z0-9-]+(.[a-z0-9-]+)*(.[a-z]{2,4})$");

    @Override
    public Retorno inicializarSistema(int cantPuntos) {
        if(cantPuntos<=0)
            return new Retorno(Resultado.ERROR_1);
        else{ 
            this.cantPuntos=cantPuntos;
            contadorPuntos=0;
            empresas= new ABBEmpresa();
            mapa = new GrafoPuntos(cantPuntos);
            ciudades=new ArrayList<>();
            datacenters= new ArrayList<>();
            return new Retorno(Resultado.OK);}
    }

    @Override
    public Retorno destruirSistema() {
        // TODO Auto-generated method stub
        return new Retorno(Resultado.NO_IMPLEMENTADA);
    }

    @Override
    public Retorno registrarEmpresa(String nombre, String direccion,
                    String pais, String email_contacto, String color) {
        Matcher mat = pat.matcher(email_contacto);
        if(!mat.matches()){
            return new Retorno(Resultado.ERROR_1);
        }
        if(empresas.existe(nombre)) return new Retorno(Resultado.ERROR_2);
        else{
            empresas.insertar(nombre, email_contacto, direccion, pais, color);
            return new Retorno(Resultado.OK);
        }    
    }

    @Override
    public Retorno registrarCiudad(String nombre, Double coordX, Double coordY) {
        if(contadorPuntos<cantPuntos){            
            Punto unP = new Punto(coordX,coordY);
            if(mapa.existePunto(unP)){
                return new Retorno(Resultado.ERROR_2);
            }else{                
                mapa.insertarPunto(unP,TipoPunto.CIUDAD);                
                ciudades.add(new Ciudad(nombre,unP));
                contadorPuntos++;
                return new Retorno(Resultado.OK);
            }            
        }else{
            return new Retorno(Resultado.ERROR_1);
        }        
    }

    @Override
    public Retorno registrarDC(String nombre, Double coordX, Double coordY,
                    String empresa, int capacidadCPUenHoras, int costoCPUporHora) {
        if(contadorPuntos<cantPuntos){     
            if(capacidadCPUenHoras<=0) return new Retorno(Resultado.ERROR_2);
            Punto unP = new Punto(coordX,coordY);
            if(mapa.existePunto(unP)){
                return new Retorno(Resultado.ERROR_3);
            }else{                
                mapa.insertarPunto(unP,TipoPunto.DATACENTER);                   
                NodoEmpresaABB emp = empresas.Buscar(empresas.getRaiz(), empresa);
                if(emp==null){
                    return new Retorno(Resultado.ERROR_4);
                }else{
                    datacenters.add(new DC(nombre, emp, capacidadCPUenHoras, capacidadCPUenHoras, unP));
                    contadorPuntos++;
                    return new Retorno(Resultado.OK);
                }                
            }            
        }else{
            return new Retorno(Resultado.ERROR_1);
        }        
    }

    @Override
    public Retorno registrarTramo(Double coordXi, Double coordYi,
                    Double coordXf, Double coordYf, int peso) {
        if(peso<=0) return new Retorno(Resultado.ERROR_1);
        Punto aux = new Punto(coordXi,coordYi);
        Punto aux2 = new Punto(coordXf,coordYf);        
        if(!mapa.existePunto(aux)||!mapa.existePunto(aux2)){
            return new Retorno(Resultado.ERROR_2);
        }
        if(mapa.existeTramo(aux, aux2))
            return new Retorno(Resultado.ERROR_3);
        mapa.registrarTramo(aux, aux2, peso);
        return new Retorno(Resultado.OK);
    }

    @Override
    public Retorno eliminarTramo(Double coordXi, Double coordYi,
                    Double coordXf, Double coordYf) {
        Punto aux = new Punto(coordXi,coordYi);
        Punto aux2 = new Punto(coordXf,coordYf);        
        if(!mapa.existePunto(aux)||!mapa.existePunto(aux2)){
            return new Retorno(Resultado.ERROR_1);
        }
        if(!mapa.existeTramo(aux, aux2))
            return new Retorno(Resultado.ERROR_2);
        mapa.eliminarTramo(aux, aux2);
        return new Retorno(Resultado.OK);
    }

    @Override
    public Retorno eliminarPunto(Double coordX, Double coordY) {
        // TODO Auto-generated method stub
        //A LA CIUDAD O AL DATACENTER HAY QUE ELIMINARLA TAMBIEN
        Punto aux =new Punto(coordX, coordY);
        if(!mapa.existePunto(aux)) 
            return new Retorno(Resultado.ERROR_1);
        else{           
            int hasta=mapa.getVertices().length;
            for(int i =0; i<hasta;i++){
                if(mapa.existeTramo(aux, mapa.getVertices()[i])){
                    mapa.eliminarTramo(aux, mapa.getVertices()[i]);
                }           
            }            
            for(Ciudad c: ciudades){
                if(c.getMisCoord().equals(aux))
                    ciudades.remove(c);
            }
            for(DC dc: datacenters){
                if(dc.getMisCoord().equals(datacenters))
                    datacenters.remove(dc);
            }
            mapa.eliminarPunto(aux);
            return new Retorno(Resultado.OK);
        }
    }

    @Override
    public Retorno mapaEstado() {
        // TODO Auto-generated method stub
        String url = "//maps.googleapis.com/maps/api/staticmap?center=Montevideo,Uruguay&zoom=13&size=1200x600&maptype=roadmap";
        int j=1;
        for(int i=0;i<ciudades.size();i++){
            j=i+1;
            url+="&markers=color:yellow%7Clabel:"+j+"%7C"+ciudades.get(i).getMisCoord().toString();   
        }
        for(int p=0;p<datacenters.size();p++){
            j++;
            url+="&markers=color:"+datacenters.get(p).getEmpresa().getColor().toString()+"%7Clabel:"+j+"%7C"+datacenters.get(p).getMisCoord().toString();            
        }      
        url+="&sensor=false";        
        try {            
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " +"http://"+url);
        } catch (IOException ex) {
            Logger.getLogger(Sistema.class.getName()).log(Level.SEVERE, null, ex);
        }       
        return new Retorno(Resultado.OK);
    }

    @Override
    public Retorno procesarInformacion(Double coordX, Double coordY,
                    int esfuerzoCPUrequeridoEnHoras) {
            // TODO Auto-generated method stub
        if(!mapa.existePunto(new Punto(coordX,coordY)))return new Retorno(Resultado.ERROR_1);
        
        return new Retorno(Resultado.NO_IMPLEMENTADA);
    }

    @Override
    public Retorno listadoRedMinima() {
        // TODO Auto-generated method stub
        return new Retorno(Resultado.NO_IMPLEMENTADA);
    }

    @Override
    public Retorno listadoEmpresas() {
        // TODO Auto-generated method stub
        String ret=empresas.mostrar();
        return new Retorno(Resultado.OK, ret,0);
    }


}
