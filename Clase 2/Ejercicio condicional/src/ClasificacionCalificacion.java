public class ClasificacionCalificacion {
    public static void main(String[] args) {
        int puntaje = 76;
        char calificacion;
        if (puntaje >= 90) {
            calificacion = 'A';
        } else if (puntaje >= 80) {
            calificacion = 'B';
        } else if (puntaje >= 70) {
            calificacion = 'C';
        } else if (puntaje >= 60) {
            calificacion = 'D';
        } else {
            calificacion = 'F';
        }
        System.out.println("Calificacion = " + calificacion);
    }
}