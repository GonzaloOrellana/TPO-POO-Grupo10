package excepciones;

public class DatosInvalidosException extends Exception { // checked

    private static final long serialVersionUID = 1L;

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
