package ag.com.dbo;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.api.Auth;

public class VaultTest {
    public static void main(String[] args) throws Exception {
        String vaultUrl = "http://localhost:8200";
        String username = "etl";
        String password = "etl";
        String secretPath="secret/bdo";

        // 1. Initialize config without an initial token
        VaultConfig config = new VaultConfig()
                .address(vaultUrl)
                .openTimeout(5)
                .readTimeout(30)
                .build();

        Vault vault = new Vault(config);

        // 2. Authenticate using username and password
        Auth auth = vault.auth();

        String clientToken = auth.loginByUserPass(username, password).getAuthClientToken();

        System.out.println("Successfully authenticated. Token: " + clientToken);

        // 3. Use the authenticated token for subsequent secret operations
        String mySecret = vault.logical()
                .read(secretPath)
                .getData()
                .get("password");

        String mySecret2 = vault.logical()
                .read(secretPath)
                .getData()
                .get("user");

        System.out.println("user: " + mySecret +" pass: "+mySecret2);
    }
}
