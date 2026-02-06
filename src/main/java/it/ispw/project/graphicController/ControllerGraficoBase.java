package it.ispw.project.graphicController;


public interface ControllerGraficoBase {
    // Ogni controller grafico deve poter ricevere la sessione corrente
    void initData(String sessionId);
}