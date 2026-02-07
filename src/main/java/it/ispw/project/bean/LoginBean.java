package it.ispw.project.bean;

public class LoginBean {

    private String username;
    private String password;

    // Costruttore vuoto (spesso richiesto dai framework o utile per istanziare e poi settare)
    public LoginBean() {}

    // Costruttore per comodità
    public LoginBean(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // --- Getters e Setters ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}