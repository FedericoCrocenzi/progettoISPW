package it.ispw.project.graphic_controller_cli;

public final class CLIPrinter {

    private CLIPrinter() {

    }

    @SuppressWarnings("java:S106")
    public static void print(String message) {
        System.out.print(message);
    }

    @SuppressWarnings("java:S106")
    public static void println(String message) {
        System.out.println(message);
    }

    @SuppressWarnings("java:S106")
    public static void println() {
        System.out.println();
    }

    @SuppressWarnings("java:S106")
    public static void printf(String format, Object... args) {
        System.out.printf(format, args);
    }
}
