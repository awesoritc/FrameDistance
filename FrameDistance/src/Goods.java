import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Goods {
    private Setting setting;
    private String simulationType;

    //商品の設定
    private int average, variance, max;
    private int c_value;//部屋ごとの補正値
    private double ratio;

    //動かすもの
    private int stock;

    private int goodsType, roomType;

    Goods(int roomType, int goodsType, Setting setting, String simulatorType){

        this.goodsType = goodsType;
        this.setting = setting;
        this.simulationType = simulatorType;

        this.average = setting.goods[goodsType][0];
        this.variance = setting.goods[goodsType][1];
        this.max = setting.goods[goodsType][2];

        this.roomType = roomType;
        this.ratio = setting.demand_mul[roomType];
        /*//補正値による調整
        this.c_value = setting.c_value[roomType];
        this.average = this.average + this.c_value;*/


        if(setting.ad_average){
            this.average = (int)Math.round(setting.goods[goodsType][0]*ratio);
        }

        if(setting.ad_max){
            this.max = (int)Math.round(setting.goods[goodsType][2]*ratio);
        }

        this.stock = max;
    }


    ArrayList<Integer> sales_history = new ArrayList<>();//売り上げ個数の履歴
    ArrayList<Integer> shortage_history = new ArrayList<>();//不足個数の履歴
    ArrayList<Integer> demand_history = new ArrayList<>();//需要個数の履歴
    ArrayList<Integer> stock_before_history = new ArrayList<>();//消費前の在庫数の履歴


    public int[] do_consume_goods(){

        stock_before_history.add(stock);

        NormalDistribution nd = new NormalDistribution(average, variance);
        int demand;
        if(setting.ad_average){
            demand = (int) Math.round(nd.random());
        }else{
            demand = (int) (Math.round(nd.random()) * ratio);
        }

        if(demand < 0){
            demand = 0;
        }


        /*
        //需要数の確認の為の書き出し
        try{
            FileWriter writer = new FileWriter(new File("nd_test.csv"), true);
            writer.write(String.valueOf(demand) + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */



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
    //商品の欠品予想数を計算
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






    //getter,setter

    public int getStock() {
        return stock;
    }

    public int getMax() {
        return max;
    }

    public ArrayList<Integer> getSales_history() {
        return sales_history;
    }

    public ArrayList<Integer> getShortage_history() {
        return shortage_history;
    }
}
