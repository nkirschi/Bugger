package selenium;

import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.LogExtension;

@ExtendWith(LogExtension.class)
public class Parallelized {

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                LoginTest login = new LoginTest();
                LogoutTest logout = new LogoutTest();

                login.run();
                logout.run();

                HuettenkaeseTest huettenkaese = new HuettenkaeseTest();
                huettenkaese.run();
            }).start();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
