public class Principal {
    public static void main(String[] args) {
        /** 1. Creamos un objeto de nuestra clase**/
        RegistroTemperaturas registro = new RegistroTemperaturas();

        /** 2. Cargamos los datos por teclado**/
        registro.cargarTemperaturas();

        /** 3. Mostramos las temperaturas cargadas**/
        System.out.println("\n--- Temperaturas de la semana ---");
        registro.mostrarTemperaturas();

        /** 4. Calculamos y mostramos el promedio**/
        System.out.println("\n--- Promedio semanal ---");
        registro.calcularPromedio();

        /** 5. Mostramos la temperatura máxima y mínima**/
        System.out.println("\n--- Extremos ---");
        registro.mostrarMaxYMin();

        /** 6. Contamos días superiores a un umbral (ej. 30.0°)**/
        System.out.println("\n--- Días calurosos ---");
        registro.contarDiasSuperiores(30.0);
    }
}
