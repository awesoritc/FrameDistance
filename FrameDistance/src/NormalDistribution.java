import java.util.ArrayList;
import java.util.Random;

public final class NormalDistribution {

    private final double mean;
    private final double variance;


    //正規分布
    public NormalDistribution(double mean, double variance){
        if(variance < 0.0){
            //分散は正の値しか認めない
            throw new  IllegalArgumentException("Variance must be non-negative. Given variance: " + variance);
        }
        this.mean = mean;
        this.variance = variance;
    }

    public double frequencyOf(double value){
        if(this.variance == 0.0){
            return this.mean == value ? 1.0 : 0.0;
        }
        return Math.exp(-0.5 * Math.pow(value - this.mean, 2.0) / this.variance)
                / Math.sqrt(2.0 * Math.PI * this.variance);
    }

    public double random(){
        double c = Math.sqrt(-2.0 * Math.log(Math.random()));
        if(Math.random() < 0.5){
            return c * Math.sin(2.0 * Math.PI * Math.random()) * this.variance + this.mean;
        }
        return c * Math.cos(2.0 * Math.PI * Math.random()) * this.variance + this.mean;
    }




    //ポアソン分布
    public int poisson(){

        //TODO:修正
        //平均が大きかった時にきちんとした値を返すことができない

        double lambda = mean;//平均
        ArrayList<Integer> array = new ArrayList<>();//ポアソン分布の確率分布

        //ポアソン分布の確率分布を作成
        for(int i = 0; i < 100; i++){
            double[] fact = factorial(i);
            double percent = (Math.pow(lambda, i) * Math.pow(Math.E, (lambda*(-1)))) / fact[0];
            for(int j = 0; j < fact[1] - 1; j++){
                percent = percent / 10;
            }
            //System.out.println(i + ":" + Math.floor(percent * 10000));
            array.add((int)Math.floor(percent * 10000));
            if(Math.floor(percent * 10000) < 1 ){
                break;
            }
        }

        //作成したポアソン分布に従って需要数を発生
        Random rand = new Random();
        int tmp = rand.nextInt(10000);
        int accum = 0;
        for(int i = 0; i < array.size(); i++){
            accum += array.get(i);
            if(tmp < accum){
                return i;
            }
        }
        return array.get(array.size()-1);
    }



    private double[] factorial(int target){
        if(target < 0){
            return new double[]{0, 0};
        }else if(target == 0){
            return new double[]{1, 1};
        }

        double[] ret = factorial(target-1);
        double fact = target * ret[0];
        double e = ret[1];
        while(fact > 10){
            e++;
            fact = fact / 10;
        }

        return new double[]{fact, e};
    }
}