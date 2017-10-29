import java.util.ArrayList;

public class Goods {
    Setting setting;
    String simulationType;

    //商品の設定
    int average, variance, max;
    double ratio;

    //動かすもの
    int stock;

    int goodsType;

    Goods(int roomType, int goodsType, Setting setting, String simulatorType){

        this.goodsType = goodsType;
        this.setting = setting;
        this.simulationType = simulatorType;

        this.average = setting.goods[goodsType][0];
        this.variance = setting.goods[goodsType][1];
        this.max = setting.goods[goodsType][2];

        this.ratio = setting.demand_mul[roomType];
    }


    ArrayList<Integer> sales_history = new ArrayList<>();
    ArrayList<Integer> shortage_history = new ArrayList<>();
    ArrayList<Integer> demand_history = new ArrayList<>();
    ArrayList<Integer> stock_before_history = new ArrayList<>();


    public int[] do_consume_goods(){

        stock_before_history.add(stock);

        NormalDistribution nd = new NormalDistribution(average, variance);
        int demand = (int) (Math.round(nd.random()) * ratio);
        if(demand < 0){
            demand = 0;
        }

        int sales;
        int shortage;

        if(stock > demand){
            shortage = 0;
            sales = demand;
            stock -= demand;
        }else if(stock > 0){
            shortage = demand - stock;
            sales = stock;
            stock = 0;
        }else{
            shortage = demand;
            sales = 0;
        }
        sales_history.add(sales);
        shortage_history.add(shortage);
        demand_history.add(demand);

        return new int[]{sales, shortage};
    }

    public void do_replenishment_goods(){
        stock = max;
    }





    //ルート作成用のメソッド
    public int expect_shortage_goods(int interval){

        double tmp = 0;
        int days = setting.interval_days;
        int weeks = setting.interval_weeks * setting.interval_days;
        if(sales_history.size() >= weeks){
            //25以上売り上げデータがある時
            for(int i = 0; i < weeks; i++){
                tmp += sales_history.get(sales_history.size()-(i+1));
            }

            int cons = Math.round((int)((double)Math.round(tmp / weeks) * interval));

            if(cons > stock){
                return cons - stock;
            }else{
                return 0;
            }

        }else if(sales_history.size() >= 5){
            //5以上売り上げデータがある時

            if(sales_history.size() > days){
                for(int i = 0; i < days; i++){
                    tmp += sales_history.get(sales_history.size()-(i+1));
                }
            }else{
                return 0;
            }

            int consume_til_next = (int)Math.round((tmp / days) * interval);

            if(consume_til_next > stock){
                return consume_til_next - stock;
            }

            return 0;

        }else{
            //売り上げデータがないとき
            return 0;
        }

    }


    //TODO:商品設置数の調整用メソッド
    public void adjust_goods(){}
}
