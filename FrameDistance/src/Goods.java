import java.io.*;
import java.util.ArrayList;

public class Goods {
    private Setting setting;
    private String simulationType;

    //商品の設定
    private int max;
    private int c_value;//部屋ごとの補正値
    private double average, variance, ratio;

    //動かすもの
    private int stock;

    private int goodsType, roomType;

    private ArrayList<Item> itemBox;

    private int ac_sales = 0;//累積の売上個数
    private int ac_shortage = 0;//累積の不足個数

    Goods(int roomType, int goodsType, Setting setting, String simulatorType){

        this.setting = setting;

        this.simulationType = simulatorType;
        this.goodsType = goodsType;
        this.roomType = roomType;
        this.ratio = setting.demand_mul[roomType];

        this.average = setting.goods[goodsType][0];
        this.variance = setting.goods[goodsType][1];
        this.max = setting.goods[goodsType][2];

        /*//補正値による調整
        this.c_value = setting.c_value[roomType];
        this.average = this.average + this.c_value;*/


        if(setting.ad_average){
            //TODO:掛け算ではなく、平行移動させる
            this.average = (setting.goods[goodsType][0])*ratio;
            //this.average = (int)Math.round(setting.goods[goodsType][0]+setting.c_value[roomType]);
        }

        if(setting.ad_max){
            this.max = (int)Math.round(setting.goods[goodsType][2]*ratio);
        }

        if(setting.ad_c_value){
            this.average = setting.goods[goodsType][0] + (setting.c_value_average[goodsType]*ratio);
            this.variance = setting.goods[goodsType][1] + (setting.c_value_variance[goodsType]*ratio);
        }

        if(setting.use_poisson){
            this.average = setting.lambda_poisson[roomType][goodsType];
            this.max = setting.max_poisson[roomType];
        }

        this.stock = max;

        itemBox = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            Item item = new Item(setting.goods[goodsType][3]);
            itemBox.add(item);
        }
    }


    private ArrayList<Integer> sales_history = new ArrayList<>();//売り上げ個数の履歴
    private ArrayList<Integer> shortage_history = new ArrayList<>();//不足個数の履歴
    private ArrayList<Integer> demand_history = new ArrayList<>();//需要個数の履歴
    private ArrayList<Integer> stock_before_history = new ArrayList<>();//消費前の在庫数の履歴


    private ArrayList<Integer> sales_history_stock = new ArrayList<>();//ストックが十分にある場合の売上個数



    public int[] do_consume_goods(){

        stock_before_history.add(stock);

        NormalDistribution nd = new NormalDistribution(average, variance);
        int demand;

        if(setting.use_poisson){
            demand = nd.poisson();
            if(average == 0){
                try{
                    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("tmp.csv"), true)));
                    pw.write(demand + "\n");
                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            if(setting.ad_average){
                demand = (int) Math.round(nd.random());
            }else{
                demand = (int) (Math.round(nd.random()) * ratio);
            }

            if(demand < 0){
                demand = 0;
            }
        }


        int sales;
        int shortage;

        if(stock > demand){
            shortage = 0;
            sales = demand;
            stock -= demand;

            if(itemBox.get(0).getExpire() < 0){
                throw new RuntimeException("賞味期限切れの商品を消費しました");
            }
            for (int i = 0; i < demand; i++) {
                itemBox.remove(0);
            }

            sales_history_stock.add(sales);

        }else if(stock > 0){
            shortage = demand - stock;
            sales = stock;
            stock = 0;

            if(itemBox.get(0).getExpire() < 0){
                throw new RuntimeException("賞味期限切れの商品を消費しました");
            }
            for (int i = 0; i < itemBox.size()-1; i++) {
                itemBox.remove(0);
            }
        }else{
            shortage = demand;
            sales = 0;
        }
        sales_history.add(sales);
        shortage_history.add(shortage);
        demand_history.add(demand);

        ac_sales += sales;
        ac_shortage += shortage;

        return new int[]{sales, shortage};
    }




    //賞味期限切れになった個数を返す
    public int do_replenishment_goods(){
        stock = max;

        while(itemBox.size() < max){
            itemBox.add(new Item(setting.goods[goodsType][3]));
        }

        //賞味期限チェックに引っかかるものを破棄
        int expire_count = 0;
        for (int i = itemBox.size()-1; i > -1; i--) {
            if(itemBox.get(i).getExpire() < setting.goods[goodsType][3]/3/*賞味期限の1/3*/){
                itemBox.remove(i);
                itemBox.add(new Item(setting.goods[goodsType][3]));
                expire_count++;
            }
        }

        return expire_count;
    }


    //1日の終わりに行う
    public boolean finish_day_goods(){
        //1日進める
        for (int i = 0; i < itemBox.size(); i++) {
            itemBox.get(i).proceed_day();
            //System.out.println("test:" + itemBox.get(i).getExpire());
        }

        //期限が迫っていればflagを立てる
        for (int i = 0; i < itemBox.size(); i++) {
            if(itemBox.get(i).getExpire() < setting.goods[goodsType][3]/3/*賞味期限の1/3*/){
                return true;
            }
        }
        return false;
    }




    //ルート作成用のメソッド
    //商品の欠品予想数を計算
    private double prev_exp = 0;
    public int expect_shortage_goods(int interval){


        if(stock == 0){
            return (int)Math.round(prev_exp * interval);
        }

        double tmp = 0;
        int days = setting.interval_days;
        int weeks = setting.interval_weeks * setting.interval_days;

        /*if(sales_history_stock.size() >= weeks){
            //25以上売り上げデータがある時
            for(int i = 0; i < weeks; i++){
                tmp += sales_history_stock.get(sales_history_stock.size()-(i+1));
            }

            double exp_per_day = (double)Math.round(tmp / weeks);
            int consume_til_next = (int)Math.round(exp_per_day * interval);

            prev_exp = exp_per_day;

            if(consume_til_next > stock){
                int expect = consume_til_next - stock;
                return expect;
            }else{
                return 0;
            }

        }else*/ if(sales_history_stock.size() >= 5){
            //5以上売り上げデータがある時

            if(sales_history_stock.size() > days){
                for(int i = 0; i < days; i++){
                    tmp += sales_history_stock.get(sales_history_stock.size()-(i+1));
                }
            }else{
                return 0;
            }

            double exp_per_day = (tmp / days);
            int consume_til_next = (int)Math.round(exp_per_day * interval);

            prev_exp = exp_per_day;

            if(consume_til_next > stock){
                int expect = consume_til_next - stock;
                return expect;
            }

            return 0;

        }else{


            /*if(sales_history.size() >= weeks){
                //25以上売り上げデータがある時
                for(int i = 0; i < weeks; i++){
                    tmp += sales_history.get(sales_history.size()-(i+1));
                }

                double exp_per_day = (double)Math.round(tmp / weeks);
                int consume_til_next = (int)Math.round(exp_per_day * interval);

                prev_exp = exp_per_day;

                if(consume_til_next > stock){
                    int expect = consume_til_next - stock;
                    return expect;
                }else{
                    return 0;
                }

            }else*/ if(sales_history.size() >= 5){
                //5以上売り上げデータがある時

                if(sales_history.size() > days){
                    for(int i = 0; i < days; i++){
                        tmp += sales_history.get(sales_history.size()-(i+1));
                    }
                }else{
                    return 0;
                }

                double exp_per_day = (tmp / days);
                int consume_til_next = (int)Math.round(exp_per_day * interval);

                prev_exp = exp_per_day;

                if(consume_til_next > stock){
                    int expect = consume_til_next - stock;
                    return expect;
                }

                return 0;

            }else{
                //売り上げデータがないとき
                return 0;
            }


        }


    }


    //getter,setter

    public int getStock() {
        return stock;
    }

    public int getMax() {
        return max;
    }

    public int getGoodsType() {
        return goodsType;
    }

    public int getAc_sales() {
        return ac_sales;
    }

    public int getAc_shortage() {
        return ac_shortage;
    }

    public ArrayList<Integer> getSales_history() {
        return sales_history;
    }

    public ArrayList<Integer> getShortage_history() {
        return shortage_history;
    }
}
