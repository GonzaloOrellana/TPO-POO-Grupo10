package excepciones;

public class TiempoInsuficienteException extends RuntimeException { // unchecked

    private static final long serialVersionUID = 1L;

    public TiempoInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
