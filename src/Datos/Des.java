/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author mramirez
 */

/*
 * Created on 2004-10-30
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/**
 * @author 李益民   我的第一个java程序
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Des {

    /**
     *
     */
    int subkey[][] = new int[16][48];//子密钥
    ///轮函数使用
    int E[] = {32, 1, 2, 3, 4, 5,
        4, 5, 6, 7, 8, 9,
        8, 9, 10, 11, 12, 13,
        12, 13, 14, 15, 16, 17,
        16, 17, 18, 19, 20, 21,
        20, 21, 22, 23, 24, 25,
        24, 25, 26, 27, 28, 29,
        28, 29, 30, 31, 32, 1};
    int P[] = {16, 7, 20, 21,
        29, 12, 28, 17,
        1, 15, 23, 26,
        5, 18, 31, 10,
        2, 8, 24, 14,
        32, 27, 3, 9,
        19, 13, 30, 6,
        22, 11, 4, 25};
    int IP[] = {58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6,
        64, 56, 48, 40, 32, 24, 16, 8,
        57, 49, 41, 33, 25, 17, 9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7};
    int IP_1[] = {40, 8, 48, 16, 56, 24, 64, 32,
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28,
        35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41, 9, 49, 17, 57, 25};
    int S[][][] = {
        {{14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7},
            {0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8},
            {4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0},
            {15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13}}, //s1

        {{15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10},
            {3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5},
            {0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15},
            {13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9}}, //s2

        {{10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8},
            {13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1},
            {13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7},
            {1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12}}, //s3

        {{7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15},
            {13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9},
            {10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4},
            {3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14}}, //s4

        {{2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9},
            {14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6},
            {4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14},
            {11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3}}, //s5

        {{12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11},
            {10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8},
            {9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6},
            {4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13}}, //s6

        {{4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1},
            {13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6},
            {1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2},
            {6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12}}, //s7

        {{13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7},
            {1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2},
            {7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8},
            {2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11}}};  //s8
    int result[] = new int[64];

    public Des() {
        super();
        // TODO Auto-generated constructor stub


    }

    public void LS(int aray[], int num, int shiftbit) //循环左移
    {
        for (int j = 0; j < shiftbit; j++) {
            int temp = aray[0];
            for (int i = 0; i < num - 1; i++) {
                aray[i] = aray[i + 1];
            }
            aray[num - 1] = temp;
        }
    }

    public int GenSubKey(int key[]) //产生16个子密钥
    {
//		子密钥产生使用
        int ip[] = {58, 50, 42, 34, 26, 18, 10, 2,
            60, 52, 44, 36, 28, 20, 12, 4,
            62, 54, 46, 38, 30, 22, 14, 6,
            64, 56, 48, 40, 32, 24, 16, 8,
            57, 49, 41, 33, 25, 17, 9, 1,
            59, 51, 43, 35, 27, 19, 11, 3,
            61, 53, 45, 37, 29, 21, 13, 5,
            63, 55, 47, 39, 31, 23, 15, 7};
        int PC_1[] = {57, 49, 41, 33, 25, 17, 9,
            1, 58, 50, 42, 34, 26, 18,
            10, 2, 59, 51, 43, 35, 27,
            19, 11, 3, 60, 52, 44, 36,
            63, 55, 47, 39, 31, 23, 15,
            7, 62, 54, 46, 38, 30, 22,
            14, 6, 61, 53, 45, 37, 29,
            21, 13, 5, 28, 20, 12, 4};
        int PC_2[] = {14, 17, 11, 24, 1, 5,
            3, 28, 15, 6, 21, 10,
            23, 19, 12, 4, 26, 8,
            16, 7, 27, 20, 13, 2,
            41, 52, 31, 37, 47, 55,
            30, 40, 51, 45, 33, 48,
            44, 49, 39, 56, 34, 53,
            46, 42, 50, 36, 29, 32};
        int shiftbit[] = {1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};  //每轮需要左移的次数
        int C[] = new int[28];
        int D[] = new int[28];
        for (int i = 0; i < 28; i++) {
            C[i] = key[PC_1[i] - 1];
        }
        for (int j = 0; j < 28; j++) {
            D[j] = key[PC_1[j + 28] - 1];
        }

        for (int r = 0; r < 16; r++) {
            LS(C, 28, shiftbit[r]);
            LS(D, 28, shiftbit[r]);
            for (int k = 0; k < 48; k++) {
                int index = PC_2[k] - 1;
                if (index < 28) {
                    subkey[r][k] = C[index];
                } else {
                    subkey[r][k] = D[index - 28];
                }
            }

        }

        return (1);
    }

    public void SetKey(int key[]) {
        GenSubKey(key);
    }

    public void XorAray(int aray1[], int aray2[], int arayout[], int length)///数组模2加
    {
        for (int k = 0; k < length; k++) {
            arayout[k] = (aray1[k] + aray2[k]) % 2;
        }
    }

    public void Fun_f(int data_in[], int data_out[], int r) ///f(r,k)函数
    {
        int data48[] = new int[48];
        int datatemp[] = new int[48];
        for (int i = 0; i < 48; i++) //E膨胀
        {
            data48[i] = data_in[E[i] - 1];
        }
        XorAray(data48, subkey[r], data48, 48);
        int row, col;
        //////////////////////////////////////////////////
        for (int si = 0; si < 8; si++) {
            row = data48[si * 6] * 2 + data48[si * 6 + 5];
            col = data48[si * 6 + 1] * 8 + data48[si * 6 + 2] * 4 + data48[si * 6 + 3] * 2 + data48[si * 6 + 4];
            int sdata = S[si][row][col];
            for (int k = 3; k >= 0; k--) {
                datatemp[si * 4 + k] = sdata & 0x1;
                sdata >>= 1;
            }
            for (int j = 0; j < 32; j++) //P置换
            {
                data_out[j] = datatemp[P[j] - 1];
            }
        }


    }

    public void encrypt(int plain[]) ///密文放在result[]中
    {
        int L[] = new int[32];
        int R[] = new int[32];
        int out[] = new int[32];

        for (int i = 0; i < 32; i++) {
            L[i] = plain[IP[i] - 1];
        }
        for (int i = 0; i < 32; i++) {
            R[i] = plain[IP[i + 32] - 1];
        }
        //System.out.println("encrypt:");
        for (int r = 0; r < 16; r++) {
            Fun_f(R, out, r);
            XorAray(L, out, out, 32);
            for (int j = 0; j < 32; j++) {
                L[j] = R[j];
            }
            for (int j = 0; j < 32; j++) {
                R[j] = out[j];
            }
            //////////////////////
            //System.out.println("");
            //System.out.println("L" + r);
            for (int i = 0; i < 32; i++) {
                if (i % 8 == 0) {
                    //System.out.print(" ");
                }
               // System.out.print(L[i]);
                ///
            }
//            System.out.print("\n");
//            System.out.println("R" + r);
            for (int i = 0; i < 32; i++) {
                if (i % 8 == 0) {
                    //System.out.print(" ");
                }
                //System.out.print("" + R[i]);
                //
            }
        }

        for (int i = 0; i < 64; i++) {
            int index = IP_1[i] - 1;
            if (index < 32) {
                result[i] = R[index];
            } else {
                result[i] = L[index - 32];
            }

        }

    }

    public void decrypt(int descode[]) ///明文放在result[]中
    {
        int L[] = new int[32];
        int R[] = new int[32];
        int out[] = new int[32];

        for (int i = 0; i < 32; i++) {
            L[i] = descode[IP[i] - 1]; //先通过IP盒
        }
        for (int i = 0; i < 32; i++) {
            R[i] = descode[IP[i + 32] - 1];
        }
        //System.out.println("decrypt:");
        for (int r = 0; r < 16; r++) {
            Fun_f(R, out, 15 - r);     ///子密钥使用与加密相反的顺序
            XorAray(L, out, out, 32);
            for (int j = 0; j < 32; j++) {
                L[j] = R[j];
            }
            for (int j = 0; j < 32; j++) {
                R[j] = out[j];
            }
            //////////////////////
//            System.out.println("");
//            System.out.println("L" + r);
            for (int i = 0; i < 32; i++) {
                if (i % 8 == 0) {
                   // System.out.print(" ");
                }
                //System.out.print(L[i]);
                ///
            }
            System.out.print("\n");
            System.out.println("R" + r);
            for (int i = 0; i < 32; i++) {
                if (i % 8 == 0) {
                    System.out.print(" ");
                }
                System.out.print("" + R[i]);
                //
            }
        }

        for (int i = 0; i < 64; i++) {
            int index = IP_1[i] - 1;
            if (index < 32) {
                result[i] = R[index];
            } else {
                result[i] = L[index - 32];
            }

        }

    }
}
