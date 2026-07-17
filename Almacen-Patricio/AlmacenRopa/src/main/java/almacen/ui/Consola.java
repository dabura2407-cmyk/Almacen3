package almacen.ui;

import java.util.Scanner;

/** Utilidades de consola: colores ANSI, separadores y lectura de entrada. */
public class Consola {

    // ANSI colors
    public static final String RESET   = "\u001B[0m";
    public static final String BOLD    = "\u001B[1m";
    public static final String ROJO    = "\u001B[31m";
    public static final String VERDE   = "\u001B[32m";
    public static final String AMARILLO= "\u001B[33m";
    public static final String AZUL    = "\u001B[34m";
    public static final String CYAN    = "\u001B[36m";
    public static final String BLANCO  = "\u001B[37m";

    private static final Scanner sc = new Scanner(System.in);

    private Consola() {}

    public static void titulo(String texto) {
        System.out.println();
        System.out.println(BOLD + CYAN + "╔══════════════════════════════════════════════════╗" + RESET);
        System.out.printf( BOLD + CYAN + "║  %-47s ║%n" + RESET, texto);
        System.out.println(BOLD + CYAN + "╚══════════════════════════════════════════════════╝" + RESET);
    }

    public static void separador() {
        System.out.println(AZUL + "──────────────────────────────────────────────────" + RESET);
    }

    public static void ok(String msg)    { System.out.println(VERDE   + "✔ " + msg + RESET); }
    public static void error(String msg) { System.out.println(ROJO    + "✘ " + msg + RESET); }
    public static void info(String msg)  { System.out.println(AMARILLO + "ℹ " + msg + RESET); }

    public static String leerLinea(String prompt) {
        System.out.print(BLANCO + prompt + RESET);
        return sc.nextLine().trim();
    }

    public static int leerEntero(String prompt) {
        while (true) {
            String s = leerLinea(prompt);
            try { return Integer.parseInt(s); }
            catch (NumberFormatException e) { error("Ingresa un número válido."); }
        }
    }

    public static int leerOpcion(int min, int max) {
        while (true) {
            int op = leerEntero("  Opción [" + min + "-" + max + "]: ");
            if (op >= min && op <= max) return op;
            error("Opción fuera de rango.");
        }
    }

    public static void pausar() {
        leerLinea("\n  Presiona ENTER para continuar...");
    }

    /** Limpia la pantalla (compatible con terminales ANSI). */
    public static void limpiar() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
