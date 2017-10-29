public final class NormalDistribution {

    private final double mean;
    private final double variance;

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
}