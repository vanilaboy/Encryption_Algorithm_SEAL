import static java.lang.Math.ceil;
import static java.lang.Math.log10;
import static java.lang.Math.pow;

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
                        zero++;
                    } else {
                        sum++;
                        one++;
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
            serialTest();
        }

        void serialTest() {
            int[] tmpZ = z;
            int[] n = new int[4];
            for (int i = 0; i < z.length; i++) {
                for (int j = 0; j < 32; j++) {
                    int tmp = tmpZ[i] & 3;
                    n[tmp]++;
                    tmpZ[i] = tmpZ[i] >> 1;
                }
            }
            double sum = 0;
            for (int i = 0; i < 4; i++) {
                sum += pow(n[i], 2);
            }
            double a = ((double) 4 / (zero + one - 1));
            double b = ((double) 2 / (zero + one));
            double answer = a * sum - b * (pow(zero, 2) + pow(one, 2)) + (double) 1;

            System.out.println("Serial test = " + answer);
            testSeriy();
        }

        void testSeriy() {
            int[] e = new int[10];
            int k = 0;
            for (int i = 0; i < 20; i++) {
                e[i] = (int) ((size * 32 - i + 2) / pow(2, i + 3));
                if (e[i] < 5) {
                    k = i;
                    break;
                }
            }

            int[] B = new int[k];
            int[] G = new int[k];
            for (int i = 0; i < k; i++)
                B[i] = G[i] = 0;
            int prev = 0;
            int len = 0;

            int[] tmpZ = z;
            for (int i = 0; i < z.length; i++) {
                for (int j = 31; j >= 0; j--) {
                    if (len == 0) {
                        prev = (tmpZ[i] >> j) % 2;
                        len = 1;
                        continue;
                    }
                    if ((tmpZ[i] >> j) % 2 == prev)
                        len++;
                    else {
                        if (len > k) {
                            len = k;
                        }
                        if (prev >= 1) {
                            B[len - 1]++;
                        } else G[len - 1]++;
                        len = 1;
                        prev = (prev + 1) % 2;
                    }
                }
            }
            double sum1 = 0, sum2 = 0;
            for (int i = 0; i < k; i++) {
                sum1 += pow(B[i] - e[i], 2) / e[i];
                sum2 += pow(G[i] - e[i], 2) / e[i];
            }
            double answer = sum1 + sum2;
            double[][] table = {
                    {1, 3.8415, 7.8794},
                    {2, 5.9915, 10.5966},
                    {4, 9.4877, 14.8603},
                    {10, 18.307, 25.1882},
                    {20, 31.4104, 39.9968},
                    {22, 33.9244, 42.7957}
            };
            int v = 0;
            for (int i = 5; i >= 0; i--) {
                if (2 * k - 2 >= table[i][0]) {
                    v = i;
                    break;
                }
            }

            if (answer < table[v][1]) {
                System.out.println("Test passed. Answer  = " + answer);
            } else if (answer < table[v][2]) {
                System.out.printf("Test passed. Answer = " + answer);
            } else {
                System.out.println("Test Failed. Answer = " + answer);
            }
            autocorTest();
        }

        void autocorTest() {
            int shift = 5;
            int A = 0;
            int[] tmpZ = z;
            for (int i = 0; i < z.length; i++) {
                for (int j = 0; j < 32; j++) {
                    int a = (tmpZ[i] >> shift) % 2;
                    int b = tmpZ[i] % 2;
                    if ((a ^ b) > 0) {
                        A++;
                    }
                    tmpZ[i] = tmpZ[i] >> 1;
                }
            }
            double up = 2 * (A - ((double) ((one + zero) - shift)) / 2);
            double answer = up / pow((one + zero) - shift, 2);
            if (answer < 1.65 && answer > -1.65) {
                System.out.println("Test Passed. Answer = " + answer);
            } else if (answer < 2.575 && answer > -2.575) {
                System.out.println("Test Passed. Answer = " + answer);
            } else {
                System.out.println("Test failed");
            }
            universalTest();
        }

        void universalTest() {
            int L = 13;
            int number = 1;
            for (int i = 0; i < L - 1; i++) {
                number = number << 1;
                number++;
            }
            int Q = (int) (10 * pow(2, L));
            int K = (int) (1000 * pow(2, L));


            int[] T = new int[Q + K];
            for (int j = 0; j < Q + K; j++)
                T[j] = 0;

            int q = 0;
            double sum = 0;
            for (int i = 0; i < size; i++) {
                int t = z[i];
                for (int j = 0; j < ceil(((double) 32) / L); j++) {
                    if (q < Q) {
                        T[t & number] = q;
                    }
                    if (q >= Q && q < Q + K) {
                        sum += log10(q - T[t & number]);
                        T[t & number] = q;
                    }
                    t = t >> L;
                    q++;
                }
                if (q >= Q + K) break;
            }
            double answer = sum / K;
            if (answer < 1.6449 && answer> -1.6449)
                System.out.println("universTest passed");
            else if (answer < 2.5758 && answer > -2.5758) {
                System.out.println("universTest passed");
            }
            else {
                System.out.println("universTest failed");
            }
        }
    }
}



