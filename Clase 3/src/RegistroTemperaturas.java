import java.util.Scanner;

/**
 * Clase que gestiona el registro y análisis de temperaturas semanales.
 */
public class RegistroTemperaturas {
    // Atributo: arreglo para almacenar las temperaturas
    private double[] temperaturas;

    // Constructor: inicializa el arreglo con 7 posiciones
    public RegistroTemperaturas() {
        this.temperaturas = new double[7];
    }

    // 1. Cargar las 7 temperaturas
    public void cargarTemperaturas() {
        Scanner teclado = new Scanner(System.in);
        for (int i = 0; i < this.temperaturas.length; i++) {
            System.out.print("Ingrese la temperatura del día " + (i + 1) + ": ");
            this.temperaturas[i] = teclado.nextDouble();
        }
    }

    // 2. Calcular e imprimir el promedio
    public void calcularPromedio() {
        double suma = 0.0;
        for (int i = 0; i < this.temperaturas.length; i++) {
            suma += this.temperaturas[i];
        }
        double promedio = suma / this.temperaturas.length;
        System.out.println("El promedio de temperaturas es: " + promedio);
    }

    // 3. Encontrar la temperatura máxima y la mínima
    public void mostrarMaxYMin() {
        double maximo = this.temperaturas[0];
        double minimo = this.temperaturas[0];

        for (int i = 0; i < this.temperaturas.length; i++) {
            if (this.temperaturas[i] > maximo) {
                maximo = this.temperaturas[i];
            }
            if (this.temperaturas[i] < minimo) {
                minimo = this.temperaturas[i];
            }
        }
        System.out.println("Temperatura máxima: " + maximo);
        System.out.println("Temperatura mínima: " + minimo);
    }

    // 4. Contar días que superaron un umbral dado
    public void contarDiasSuperiores(double umbral) {
        int contador = 0;
        for (int i = 0; i < this.temperaturas.length; i++) {
            if (this.temperaturas[i] > umbral) {
                contador++;
            }
        }
        System.out.println("Días que superaron los " + umbral + "°: " + contador);
    }

    // 5. Imprimir cada día con su temperatura usando length
    public void mostrarTemperaturas() {
        for (int i = 0; i < this.temperaturas.length; i++) {
            System.out.println("Día " + (i + 1) + ": " + this.temperaturas[i] + "°");
        }
    }
}