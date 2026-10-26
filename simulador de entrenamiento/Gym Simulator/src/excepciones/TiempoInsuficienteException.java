package excepciones;

public class TiempoInsuficienteException extends RuntimeException{ //unchecked
    public TiempoInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
