package fi.bizhop.emailerrest.model;

public enum Store {
    NBDG("nbdg", "NBDG"), PG("pg", "Powergrip");

    private final String storeName;
    private final String emailName;

    Store(String storeName, String emailName) {
        this.storeName = storeName;
        this.emailName = emailName;
    }

    public String getStoreName() { return storeName; }

    public String getEmailName() { return emailName; }

    public static Store fromStoreName(String storeName) {
        if(storeName == null) return null;
        for(var value : Store.values()) {
            if(storeName.equals(value.getStoreName())) {
                return value;
            }
        }
        return null;
    }
}
