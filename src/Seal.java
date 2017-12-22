import java.util.HashMap;
import java.util.Vector;

/**
 * Created by root on 19.12.17 with love.
 */
public class Seal {

    private int[] key;
    private final int K[] = {
            0x5A827999,
            0x6ED9EBA1,
            0x8F1BBCDC,
            0xCA62C1D6
    };
    private int n = 0;
    private int L = 64 * 1024 * 8;
    private int size = L / 1024;
    private int A;
    private int B;
    private int C;
    private int D;
    private int E;
    private int[] T = new int[512];
    private int[] S = new int[256];
    private int[] R = new int[256];
    private int n1, n2, n3, n4;
    public int[] z;

    public Seal(int[] key, int n) {
        this.n = n;
        this.key = key;
        A = key[0];
        B = key[1];
        C = key[2];
        D = key[3];
        E = key[4];

        for (int i = 0; i < 512; i++)
            T[i] = createTable(i);
        for (int j = 0; j < 256; j++)
            S[j] = createTable(0x1000 + j);
        for (int k = 0; k < 256; k++)
            R[k] = createTable(0x2000 + k);

        z = new int[size];
        z = functionSEAL();

        Tester tester = new Tester();
        tester.frequencyTest();
        tester.sequentialTest();
 //       tester.testSeries();
    }

    public int[] encrypt(int[] text) {
        int[] res = new int[size];
        try {
            for (int i = 0; i < text.length; i++) {
                res[i] = z[i] ^ text[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public int[] decrypt(int[] encryptText) {
        int[] res = new int[size];
        for (int i = 0; i < encryptText.length; i++) {
            res[i] = z[i] ^ encryptText[i];
        }
        return res;
    }

    private int[] G(int I) {
        int[] W = new int[80];
        W[0] = I;
        for (int i = 1; i < 16; i++) {
            W[i] = 0;
        }
        for (int i = 16; i < 80; i++) {
            W[i] = shift(1, (W[i - 3] ^ W[i - 8] ^ W[i - 14] ^ W[i - 16]));
        }
        int temp;
        int t;
        for (t = 0; t < 20; t++) {
            temp = shift(5, A) + ((B & C) | ((~B) & D)) + E + W[t] + K[0];
            temp &= 0xFFFFFFFF;
            E = D;
            D = C;
            C = shift(30, B);
            B = A;
            A = temp;
        }
        for (t = 20; t < 40; t++) {
            temp = shift(5, A) + (B ^ C ^ D) + E + W[t] + K[1];
            temp &= 0xFFFFFFFF;
            E = D;
            D = C;
            C = shift(30, B);
            B = A;
            A = temp;
        }
        for (t = 40; t < 60; t++) {
            temp = shift(5, A) + ((B & C) | (B & D) | (C & D)) + E + W[t] + K[2];
            temp &= 0xFFFFFFFF;
            E = D;
            D = C;
            C = shift(30, B);
            B = A;
            A = temp;
        }
        for (t = 60; t < 80; t++) {
            temp = shift(5, A) + (B ^ C ^ D) + E + W[t] + K[3];
            temp &= 0xFFFFFFFF;
            E = D;
            D = C;
            C = shift(30, B);
            B = A;
            A = temp;
        }
        int[] H = new int[5];
        H[0] = (key[0] + A);
        H[1] = (key[1] + B);
        H[2] = (key[2] + C);
        H[3] = (key[3] + D);
        H[4] = (key[4] + E);
        return H;
    }

    private int createTable(int i) {
        return G(i)[i % 5];
    }

    private int[] functionSEAL() {
        int l = 0;
        int[] y = new int[size];
        for (int i = 0; i < size; i++) {
            y[i] = 0;
        }
        int lengthY = 0;
        while (true) {
            procedureInitialize(l);
            int P;
            int Q;
            int[] h = new int[4];
            for (int i = 1; i < 64; i++) {
                P = A & 0x7FC;
                B = (B + T[P / 4]);
                A = shift(23, A);
                B = B ^ A;
                Q = B & 0x7FC;
                C = C ^ T[Q / 4];
                B = shift(23, B);
                C = (C + B);
                P = (P + C) & 0x7FC;
                D = (D + T[P / 4]);
                C = shift(23, C);
                D = D ^ C;
                Q = (Q + D) & 0x7FC;
                A = A ^ T[Q / 4];
                D = shift(23, D);
                A = (A + D);

                P = (P + A) & 0x7FC;
                B = B ^ T[P / 4];
                A = shift(23, A);
                Q = (Q + B) & 0x7FC;
                C = (C + T[Q / 4]);
                B = shift(23, B);
                P = (P + C) & 0x7FC;
                D = D ^ T[P / 4];
                C = shift(23, C);
                Q = (Q + D) & 0x7FC;
                A = (A + T[Q / 4]);
                D = shift(23, D);

                h[0] = (B + S[4 * i - 4]);
                h[1] = C ^ S[4 * i - 3];
                h[2] = (D + S[4 * i - 2]);
                h[3] = A ^ S[4 * i - 1];

                y[lengthY] = h[0];
                y[lengthY + 1] = h[1];
                y[lengthY + 2] = h[2];
                y[lengthY + 3] = h[3];


                lengthY += 4;


                if (lengthY >= L / 1024) {
                    return y;
                }
                if (i % 2 == 0) {
                    A = A + n1;
                    B = B + n2;
                    C = C ^ n1;
                    D = D ^ n2;
                } else {
                    A = A + n3;
                    B = B + n4;
                    C = C ^ n3;
                    D = D ^ n4;
                }
            }
        }
    }

    private void procedureInitialize(int l) {
        int P;
        A = n ^ R[4 * l];
        B = shift(24, n) ^ R[4 * l + 1];
        C = shift(16, n) ^ R[4 * l + 2];
        D = shift(8, n) ^ R[4 * l + 3];

        for (int j = 1; j <= 2; j++) {
            P = A & 0x7FC;
            B = (B + T[P / 4]);
            A = shift(23, A);
            P = B & 0x7FC;
            C = (C + T[P / 4]);
            B = shift(23, B);
            P = C & 0x7FC;
            D = (D + T[P / 4]);
            C = shift(23, C);
            P = D & 0x7FC;
            A = (A + T[P / 4]);
            D = shift(23, D);
        }
        n1 = A;
        n2 = B;
        n3 = C;
        n4 = D;
        P = A & 0x7FC;
        B = (B + T[P / 4]);
        A = shift(23, A);
        P = B & 0x7FC;
        C = (C + T[P / 4]);
        B = shift(23, B);
        P = C & 0x7FC;
        D = (D + T[P / 4]);
        C = shift(23, C);
        P = D & 0x7FC;
        A = (A + T[P / 4]);
        D = shift(23, D);
    }


    private int shift(int bits, int number) {
        return ((number << bits)) | ((number) >> (32 - bits));
    }

    private class Tester {

        int one = 0;
        int zero = 0;

        Tester() {

        }

        void frequencyTest() {
            long sum = 0;
            int[] tmpZ = z;
            for (int i = 0; i < tmpZ.length; i++) {
                while (tmpZ[i] > 0) {
                    if ((tmpZ[i] & 1) == 0) {
                        sum--;
                    } else {
                        sum++;
                    }
                    tmpZ[i] = tmpZ[i] >> 1;
                }
            }
            double res = sum / Math.sqrt(z.length * 32);
            System.out.print("Frequency test = " + res);
            if (res > 0.01) {
                System.out.println(" test Passed!");
            } else {
                System.out.println(" test Failed!");
            }
        }

        void serialTest() {

        }


    }


}
