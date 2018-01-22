import java.io.*;
import java.util.ArrayList;

public class Goods {
    private Setting setting;
    private String simulationType;

    //商品の設定
    private int max;
    private int c_value;//部屋ごとの補正値
    private double average, variance, ratio, lambda;

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
            this.lambda = setting.lambda_poisson[roomType][goodsType];
        }

        itemBox = new ArrayList<>();
        /*for (int i = 0; i < max; i++) {
            Item item = new Item(setting.goods[goodsType][3]);
            itemBox.add(item);
        }*/
        while(itemBox.size() < max){
            itemBox.add(new Item(setting.goods[goodsType][3], 0));
        }
    }


    private ArrayList<Integer> sales_history = new ArrayList<>();//売り上げ個数の履歴
    private ArrayList<Integer> shortage_history = new ArrayList<>();//不足個数の履歴
    private ArrayList<Integer> demand_history = new ArrayList<>();//需要個数の履歴
    private ArrayList<Integer> stock_before_history = new ArrayList<>();//消費前の在庫数の履歴


    private ArrayList<Integer> sales_history_stock = new ArrayList<>();//ストックが十分にある場合の売上個数



    public int[] do_consume_goods(int demand_uniform){

        stock_before_history.add(itemBox.size());

        NormalDistribution nd = new NormalDistribution(average, variance, lambda);
        if(simulationType.equals(setting.simulatorType_dynamic)){
            nd = new NormalDistribution(average, variance, lambda*setting.increase_sales);
        }
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

            if(setting.use_same_demand){
                demand = demand_uniform;
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

        if(itemBox.size() > demand){
            shortage = 0;
            sales = demand;

            if(itemBox.get(0).getExpire() < 0){
                throw new RuntimeException("賞味期限切れの商品を消費しました");
            }
            for (int i = 0; i < demand; i++) {
                itemBox.remove(0);
            }

            sales_history_stock.add(sales);

        }else if(itemBox.size() > 0){
            shortage = demand - itemBox.size();
            sales = itemBox.size();

            if(itemBox.get(0).getExpire() < 0){
                throw new RuntimeException("賞味期限切れの商品を消費しました");
            }
            while(itemBox.size() > 0){
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
    public int[] do_replenishment_goods(int day){

        //賞味期限チェックに引っかかるものを破棄
        int expire_count = 0;
        int test = 0;
        while(itemBox.size() > 0 && itemBox.get(0).getExpire() < setting.goods[goodsType][3]/3){
            if(itemBox.get(0).getDay_displayed() == 0){
                test++;
            }
            itemBox.remove(0);
            expire_count++;
        }

        if(simulationType.equals(setting.simulatorType_dynamic)){
            //TODO:明らかに廃棄になりそうなら補充をしない
            boolean enough_flag = false;
            if(sales_history.size() > 20){
                int sales_in_20days = 0;
                for (int i = 0; i < 20; i++) {
                    sales_in_20days += sales_history.get(sales_history.size()-1-i);
                }
                if(sales_in_20days*5 < itemBox.size()){
                    enough_flag = true;
                    //System.out.println("sales_in:" + sales_in_20days + ", itembox:" + itemBox.size());
                }
            }
            if(enough_flag){
                return new int[]{expire_count, test};
            }
        }

        while(itemBox.size() < max){
            itemBox.add(new Item(setting.goods[goodsType][3], day));
        }

        //return expire_count;
        return new int[]{expire_count, test};
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

        //在庫切れのときは以前の売上履歴を利用して予測する
        if(itemBox.size() == 0){
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

            if(consume_til_next > itemBox.size()){
                int expect = consume_til_next - itemBox.size();
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

                if(consume_til_next > itemBox.size()){
                    int expect = consume_til_next - itemBox.size();
                    return expect;
                }

                return 0;
            }else{
                //売り上げデータがないとき
                return 0;
            }
        }
    }


    /*public void loss_check(int day, int roomId, int goodsId){

        //TODO:初日に設置したもので廃棄になった個数を比べる

        //初日に設置した個数の残っている数を数える
        int count = 0;
        for (Item anItemBox : itemBox) {
            if (anItemBox.getDay_displayed() == 0) {
                count++;
            }
        }
        String path = "./Data/loss_test.csv";
        if(simulationType.equals(setting.simulatorType_static)){
            path = "./Data/loss_test_st.csv";
        }else if(simulationType.equals(setting.simulatorType_dynamic)){
            path = "./Data/loss_test_dy.csv";
        }
        try{
            PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(new File(path), true)));
            //day,simulationType, roomId, goodsId, num
            p.write(day + "," + simulationType + "," + roomId + "," + goodsId + "," + count + "\n");
            p.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


    //getter,setter

    public int getStock() {
        return itemBox.size();
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
