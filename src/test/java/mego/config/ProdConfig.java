package mego.config;

public class ProdConfig implements Config {

    static final ProdConfig instance = new ProdConfig();

    private ProdConfig() {

    }

    @Override
    public String apiUrl() {
        return "https://epg.megogo.net/";
    }

}
