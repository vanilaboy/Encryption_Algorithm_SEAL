import java.io.*;
import java.util.Random;

/**
 * Created by root on 19.12.17 with love.
 */
public class Main {
    public static void main(String[] args) {
        String pathInputFile = "/root/IdeaProjects/crypto_lab3/src/food";
        String pathEncryptFile = "/root/IdeaProjects/crypto_lab3/src/foodEncrypt";
        String pathOutputFile = "/root/IdeaProjects/crypto_lab3/src/foodDecrypt";

        int blockSize = 512;
        int[] key = generateKey();
        Seal seal1 = new Seal(key, 234567);

        try {
            FileInputStream in = new FileInputStream(new File(pathInputFile));
            FileOutputStream encryptOut = new FileOutputStream(new File(pathEncryptFile));
            FileOutputStream decryptedOut = new FileOutputStream(new File(pathOutputFile));
            while (in.available() > 0) {
                int[] block = new int[blockSize];
                int counter = 0;
                for (int i = 0; i < blockSize; i++) {
                    int tmp = in.read();
                    if (tmp == -1) {
                    } else {
                        block[i] = tmp;
                        counter++;
                    }
                }
                int[] encrypted = seal1.encrypt(block);
                for (int i = 0; i < counter; i++) {
                    encryptOut.write(encrypted[i]);
                }
                int[] decrypted = seal1.decrypt(block);
                for (int i = 0; i < counter; i++) {
                    decryptedOut.write(decrypted[i]);
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

    }

    private static int[] generateKey() {
        int[] key = new int[20];
        Random random = new Random(System.currentTimeMillis());
        for(int i = 0; i < 20; i++) {
            key[i] = 234;
        }
        return key;
    }
}
