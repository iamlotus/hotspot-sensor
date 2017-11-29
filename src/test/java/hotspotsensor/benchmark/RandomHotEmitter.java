package hotspotsensor.benchmark;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lotus.jzx
 */
public class RandomHotEmitter implements StringEmitter {

    private double[] thresholds;
    private String[] results;

    private String[] candidates;

    private Map<String, Double> hotProbability;


    public RandomHotEmitter(Map<String, Double> hotProbability) {

        if (hotProbability == null) {
            thresholds = new double[] {1d};
            results = new String[] {null};
        } else {
            thresholds = new double[hotProbability.size() + 1];
            results = new String[hotProbability.size() + 1];

            int i = 0;
            double sumProbability = 0d;
            for (Map.Entry<String, Double> entry : hotProbability.entrySet()) {
                String key = entry.getKey();
                double probability = entry.getValue();
                if (probability <= 0d) {
                    throw new IllegalArgumentException();
                }

                sumProbability += probability;

                if (sumProbability >= 1d) {
                    throw new IllegalArgumentException();
                }


                thresholds[i] = sumProbability;
                results[i] = key;

                i++;
            }

            thresholds[i] = 1d;
            results[i] = null;
        }

        int length = 4;

        int candidateLength = (int) Math.pow(26, length);

        char[] value = new char[length];
        candidates = new String[candidateLength];

        int candidatesIndex = 0;


        for (int v1 = 0; v1 < 26; v1++) {
            value[0] = (char) ('A' + v1);
            for (int v2 = 0; v2 < 26; v2++) {
                value[1] = (char) ('A' + v2);
                for (int v3 = 0; v3 < 26; v3++) {
                    value[2] = (char) ('A' + v3);
                    for (int v4 = 0; v4 < 26; v4++) {
                        value[3] = (char) ('A' + v4);
                        candidates[candidatesIndex++] = new String(value).intern();
                    }
                }
            }
        }

    }

    @Override
    public String emit() {

        Random r = ThreadLocalRandom.current();
        double d = r.nextDouble();
        for (int i = 0; i < thresholds.length; i++) {
            if (thresholds[i] >= d) {
                String result = results[i];
                if (result == null) {
                    result = candidates[r.nextInt(candidates.length)];
                }
                return result;
            }
        }

        // never reach here;
        throw new Error();
    }


}
