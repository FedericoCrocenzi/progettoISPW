package it.ispw.project.graphic_controller;


public interface ControllerGraficoBase {
    // Ogni controller grafico deve poter ricevere la sessione corrente
    void initData(String sessionId);
}