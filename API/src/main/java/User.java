package main.java;

class User {
    private String password;
    private Integer nbConnexions;

    public User(String password, Integer nbConnexions) {
        this.password = password;
        this.nbConnexions = nbConnexions;
    }

    public String getPassword() {
        return password;
    }

    public Integer getNbConnexions() {
        return nbConnexions;
    }

    public void setNbConnexions(Integer nbConnexions) {
        this.nbConnexions = nbConnexions;
    }
}
