import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;

public class CatalogFinalCore {

    // Decode value in given base
    public static BigInteger decode(String val, int base) {
        return new BigInteger(val, base);
    }

    // Solve polynomial using Vandermonde matrix
    public static BigInteger[] solvePoly(int[] xs, BigInteger[] ys, int deg) {
        int n = deg + 1;
        BigInteger[][] m = new BigInteger[n][n + 1];

        for (int i = 0; i < n; i++) {
            BigInteger p = BigInteger.ONE;
            for (int j = 0; j <= deg; j++) {
                m[i][j] = p;
                p = p.multiply(BigInteger.valueOf(xs[i]));
            }
            m[i][n] = ys[i];
        }

        // Gaussian elimination
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                BigInteger f = m[j][i].divide(m[i][i]);
                for (int k = i; k <= n; k++) {
                    m[j][k] = m[j][k].subtract(f.multiply(m[i][k]));
                }
            }
        }

        BigInteger[] c = new BigInteger[n];
        for (int i = n - 1; i >= 0; i--) {
            BigInteger s = m[i][n];
            for (int j = i + 1; j < n; j++) {
                s = s.subtract(m[i][j].multiply(c[j]));
            }
            c[i] = s.divide(m[i][i]);
        }
        return c;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Ask for JSON filename
        System.out.print("Enter JSON filename (with .json extension): ");
        String fileName = sc.nextLine();

        String json = "";
        try {
            Scanner fileScanner = new Scanner(new File(fileName));
            while (fileScanner.hasNextLine()) {
                json += fileScanner.nextLine();
            }
            fileScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            return;
        }

        // Parse keys (n and k)
        Pattern keysPattern = Pattern.compile("\"keys\"\\s*:\\s*\\{[^}]*\\}");
        Matcher keysMatcher = keysPattern.matcher(json);
        int k = 0;
        if (keysMatcher.find()) {
            String keysBlock = keysMatcher.group();
            Pattern kPattern = Pattern.compile("\"k\"\\s*:\\s*(\\d+)");
            Matcher kMatcher = kPattern.matcher(keysBlock);
            if (kMatcher.find()) k = Integer.parseInt(kMatcher.group(1));
        }

        // Parse roots
        Pattern rootsPattern = Pattern.compile("\"(\\d+)\"\\s*:\\s*\\{\\s*\"base\"\\s*:\\s*\"(\\d+)\",\\s*\"value\"\\s*:\\s*\"([0-9a-zA-Z]+)\"\\s*\\}");
        Matcher rootsMatcher = rootsPattern.matcher(json);

        int[] xs = new int[k];
        BigInteger[] ys = new BigInteger[k];
        int idx = 0;

        while (rootsMatcher.find() && idx < k) {
            int x = Integer.parseInt(rootsMatcher.group(1));
            int base = Integer.parseInt(rootsMatcher.group(2));
            String value = rootsMatcher.group(3);
            BigInteger y = decode(value, base);

            xs[idx] = x;
            ys[idx] = y;
            idx++;
        }

        // Solve polynomial
        BigInteger[] coeff = solvePoly(xs, ys, k - 1);

        System.out.println("\nPolynomial Coefficients (constant to highest):");
        for (BigInteger c : coeff) System.out.println(c);

        System.out.println("\nSecret C: " + coeff[0]);
    }
}
