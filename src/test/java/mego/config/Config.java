package mego.config;

public interface Config {

    static Config getInstance() {
//        String env = System.getProperty("env");
        return ProdConfig.instance;
    }

    String apiUrl();
}
