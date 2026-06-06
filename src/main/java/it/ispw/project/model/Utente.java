package it.ispw.project.model;


public class Utente {

    private int id;
    private String username;
    private String password;
    private String ruolo;
    private String email;
    private String indirizzo;


    // Il DAO userà questo per istanziare l'oggetto
    public Utente(int id, String username, String password, String ruolo, String email, String indirizzo) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.ruolo = ruolo;
        this.email = email;
        this.indirizzo = indirizzo;
    }



    public int ottieniId() { return id; }
    public String leggiUsername() { return username; }
    public String ottieniPassword() { return password; }
    public String scopriRuolo() { return ruolo; }
    public String leggiEmail() { return email; }
    public String leggiIndirizzo() { return indirizzo; }

}
