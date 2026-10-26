package excepciones;

public class DatosInvalidosException extends Exception { //checked
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
