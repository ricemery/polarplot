module com.chainstaysoftware.polarplot {

    // Java
    requires java.base;
    requires java.logging;

    // Java-FX
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.swing;

    exports com.chainstaysoftware.polarplot;
    exports com.chainstaysoftware.polarplot.series;
}