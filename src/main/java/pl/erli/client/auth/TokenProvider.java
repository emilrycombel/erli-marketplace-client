package pl.erli.client.auth;

@FunctionalInterface
public interface TokenProvider {

    String getToken();
}
