import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Executor {

    /*TODO:
    * エリアごとの部屋規模を固定する(10部屋ごとに割合で固定した値にする)
    * 部屋ごとに設置する商品の種類とその数は固定にする
    * それぞれの部屋タイプ・商品タイプで、どの需要にすれば売上と不足のバランスが取れるかを確認しで設定
    * 補充優先度のグラフを書いて分布確認(切るポイントを見つけるため)
    *
    */



    public static void main(String[] args){

        Setting setting = new Setting();

        //出力ファイルの初期化
        try{
            FileWriter w;

            w = new FileWriter(new File("./Data/rooms_condition_dy.csv"));
            w.write("day,roomId,ifInRoute,suf_rate,expect_shortage,dis_from_point,rep_value\n");
            w.close();
            new FileWriter(new File("./Data/Route_dynamic.csv")).write("");
            new FileWriter(new File("./Data/Route_static.csv")).write("");
            new FileWriter(new File("./Data/day_based.csv")).write("");
            new FileWriter(new File("./Data/room_based.csv")).write("");
            w = new FileWriter(new File("./Data/shortage_day_room.csv"));
            w.write("day,roomId,areaNum,simulatorType,last_rep_day,roomType,shortage\n");
            w.close();

            w = new FileWriter(new File("./Data/ac_goods_ss_dynamic.csv"));
            w.write("simulatorType,roomID,goodsNumber,roomType,goodsType,ac_sales,ac_shortage\n");
            w.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        //それぞれの部屋に登録する商品の番号をセット(登録はSimulatorで行う)
        ArrayList<Integer> goods_alloc = new ArrayList<>();
        if(setting.goods_fix){
            for (int i = 0; i < setting.room; i++) {
                for (int j = 0; j < setting.goodsNum_per_room; j++) {
                    //todo:登録する商品の種類を1種類に固定して検証
                    //int version = 0;

                    int version;
                    if(j < setting.goods_distribution[0]){
                        version = 0;
                    }else if(j < (setting.goods_distribution[0] + setting.goods_distribution[1])){
                        version = 1;
                    }else{
                        version = 2;
                    }
                    goods_alloc.add(version);
                }
            }
        }else{
            for (int i = 0; i < setting.room; i++) {
                for (int j = 0; j < setting.goodsNum_per_room; j++) {
                    Random rand = new Random();
                    int random = rand.nextInt(10);
                    int version;
                    if(random < setting.goods_distribution[0]){
                        version = 0;
                    }else if(random < setting.goods_distribution[0] + setting.goods_distribution[1]){
                        version = 1;
                    }else{
                        version = 2;
                    }
                    goods_alloc.add(version);
                }
            }
        }

        Simulator simulator_static = new Simulator(/*rooms_static, */goods_alloc, setting, setting.simulatorType_static);
        Simulator simulator_dynamic = new Simulator(/*rooms_dynamic, */goods_alloc, setting, setting.simulatorType_dynamic);

        for (int i = 0; i < setting.day; i++) {

            int day = i;

            System.out.println(i);

            //2つのシミュレーターに同じ値を渡す
            //1000個の需要をここで作成する(100部屋×10種類)
            double[][] lambda = setting.lambda_poisson;
            int[][] demand = new int[setting.room][setting.goodsNum_per_room];
            int[][] demand_dynamic = new int[setting.room][setting.goodsNum_per_room];
            for (int j = 0; j < setting.room; j++) {
                int roomType;
                if(j%10 < 5){
                    roomType = 0;
                }else if(j%10 < 8){
                    roomType = 1;
                }else{
                    roomType = 2;
                }
                for (int k = 0; k < setting.goodsNum_per_room; k++) {
                    int goodsType;
                    if(k < 7){
                        goodsType = 0;
                    }else if(k < 9){
                        goodsType = 1;
                    }else{
                        goodsType = 2;
                    }
                    NormalDistribution nd = new NormalDistribution(0, 0, lambda[roomType][goodsType]);
                    demand[j][k] = nd.poisson();
                }
            }


            simulator_static.create_route(day);
            simulator_static.do_consume_simulator(day, demand);
            simulator_static.do_replenishment_simulator(day);
            simulator_static.finish_day();

            simulator_dynamic.create_route(day);
            simulator_dynamic.do_consume_simulator(day, demand);
            simulator_dynamic.do_replenishment_simulator(day);
            simulator_dynamic.finish_day();
        }


        //結果出力
        System.out.println();
        System.out.println("static");
        System.out.println(simulator_static.getTotal_sales());
        System.out.println(simulator_static.getTotal_shortage());
        System.out.println(simulator_static.getTotal_distance());
        System.out.println(simulator_static.getTotal_expire_loss());

        int rooms_n_st = 0;
        ArrayList<ArrayList<Room>> a_st = simulator_static.getRouteHistory();
        for (int i = 0; i < a_st.size(); i++) {
            rooms_n_st += a_st.get(i).size();
        }
        System.out.println(((rooms_n_st * setting.service_time_per_room_static * setting.payment_per_min) + (simulator_static.getTotal_distance() * setting.move_time_per_1 * setting.payment_per_min)));


        System.out.println();


        System.out.println("dynamic");
        System.out.println(simulator_dynamic.getTotal_sales());
        System.out.println(simulator_dynamic.getTotal_shortage());
        System.out.println(simulator_dynamic.getTotal_distance());
        System.out.println(simulator_dynamic.getTotal_expire_loss());

        /*int rooms_n_dy = 0;
        ArrayList<ArrayList<Room>> a_dy = simulator_dynamic.getRouteHistory();
        for (int i = 0; i < a_dy.size(); i++) {
            rooms_n_dy += a_dy.get(i).size();
        }
        System.out.println(((rooms_n_dy * setting.service_time_per_room_dynamic * setting.payment_per_min) + (simulator_dynamic.getTotal_distance() * setting.move_time_per_1 * setting.payment_per_min)));
        */
        System.out.println("手数料:" + Math.ceil(simulator_dynamic.getTotal_sales()*100*setting.payment_service_fee));
        System.out.println("残業時間(分):" + simulator_dynamic.getOverworktime());

        simulator_dynamic.write_goods_shortage();





        //結果の書き出し

        //部屋ごとのルート(dy)
        try{
            ArrayList<ArrayList<Room>> time_route_dy = simulator_dynamic.getRouteHistory();

            PrintWriter pw_route_dy = new PrintWriter(new BufferedWriter(new FileWriter(new File("./Data/Route_dynamic.csv"), true)));
            pw_route_dy.write("day,area,roomId,areaNum,pos\n");
            for (int i = 0; i < time_route_dy.size(); i++) {
                ArrayList<Room> tmp = time_route_dy.get(i);
                //pw_route_dy.write("Day:" + i + "\n");
                for (int j = 0; j < tmp.size(); j++) {
                    pw_route_dy.write(i + "," + setting.order_rep[i%5] + "," + tmp.get(j).getRoomId() + "," + tmp.get(j).getAreaNumber() +  ",(" + tmp.get(j).getX_pos() + ":" + tmp.get(j).getY_pos() + ")" + "\n");
                }
                //pw_route_dy.write("\n");
            }
            pw_route_dy.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //部屋ごとのルート(st)
        try{
            ArrayList<ArrayList<Room>> time_route_st = simulator_static.getRouteHistory();

            new FileWriter(new File("./Data/Route_static.csv")).write("");
            PrintWriter pw_route_st = new PrintWriter(new BufferedWriter(new FileWriter(new File("./Data/Route_static.csv"), true)));
            pw_route_st.write("day,roomId,areaNum,pos\n");
            for (int i = 0; i < time_route_st.size(); i++) {
                ArrayList<Room> tmp = time_route_st.get(i);
                //pw_route_st.write("Day:" + i + "\n");
                for (int j = 0; j < tmp.size(); j++) {
                    pw_route_st.write( i + "," +tmp.get(j).getRoomId() + "," + tmp.get(j).getAreaNumber() + ",(" + tmp.get(j).getX_pos() + ":" + tmp.get(j).getY_pos() + ")" + "\n");
                }
                //pw_route_st.write("\n");
            }
            pw_route_st.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        //整理したもの

        //日
        try{

            ArrayList<Integer> time_st = simulator_static.getRouteDistance();
            ArrayList<Integer> time_dy = simulator_dynamic.getRouteDistance();
            ArrayList<Integer> sales_st = simulator_static.getSalesHistory();
            ArrayList<Integer> shortage_st = simulator_static.getShortageHistory();
            ArrayList<Integer> sales_dy = simulator_dynamic.getSalesHistory();
            ArrayList<Integer> shortage_dy = simulator_dynamic.getShortageHistory();
            ArrayList<Integer> loss_st = simulator_static.getExpire_countHistory();
            ArrayList<Integer> loss_dy = simulator_dynamic.getExpire_countHistory();
            ArrayList<Double> availability_st = simulator_static.getAvailabilityHistory();
            ArrayList<Double> availability_dy = simulator_dynamic.getAvailabilityHistory();
            ArrayList<Integer> work_time_st = simulator_static.getWork_timeHistory();
            ArrayList<Integer> work_time_dy = simulator_dynamic.getWork_timeHistory();

            PrintWriter pw_time = new PrintWriter(new BufferedWriter(new FileWriter(new File("./Data/day_based.csv"), true)));
            pw_time.write("day,distance_static,distance_dynamic,work_time_static,work_time_dynamic,sales_static,shortage_static,sales_dynamic,shortage_dynamic,loss_static,loss_dynamic,availability_static,availability_dynamic\n");
            for (int i = 0; i < time_st.size(); i++) {
                //System.out.println(time_st.get(i));
                pw_time.write(i + "," +  time_st.get(i) + "," + time_dy.get(i) + "," + work_time_st.get(i) + "," + work_time_dy.get(i) +
                        sales_st.get(i) + "," + shortage_st.get(i) + "," + sales_dy.get(i) + "," + shortage_dy.get(i) + "," +
                        loss_st.get(i) + "," + loss_dy.get(i) + "," +
                        availability_st.get(i) + "," + availability_dy.get(i) +  "\n");
            }
            pw_time.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //部屋
        try{

            int[] sales_st = simulator_static.getSales_rooms();
            int[] shortage_st = simulator_static.getShortage_rooms();
            int[] sales_dy = simulator_dynamic.getSales_rooms();
            int[] shortage_dy = simulator_dynamic.getShortage_rooms();
            ArrayList<Integer> loss_st = new ArrayList<>(Arrays.asList(simulator_static.getRoom_expire()));
            ArrayList<Integer> loss_dy = new ArrayList<>(Arrays.asList(simulator_dynamic.getRoom_expire()));


            PrintWriter pw_room = new PrintWriter(new BufferedWriter(new FileWriter(new File(
                    "./Data/room_based.csv"), true)));

            pw_room.write("roomId,sales_st,shortage_st,sales_dy,shortage_dy,loss_static,loss_dynamic\n");

            for (int i = 0; i < sales_st.length; i++) {
                pw_room.write(i + "," +  + sales_st[i] + "," + shortage_st[i] + "," + sales_dy[i] + "," + shortage_dy[i] + "," +  loss_st.get(i) + "," + loss_dy.get(i) + "\n");
            }
            pw_room.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        //ここで今動かしている変数を確認
        System.out.println();
        System.out.println("需要:" + setting.increase_sales);
        System.out.println("作業時間:" + setting.service_time_per_room_dynamic);
        System.out.println("決済手数料:" + setting.payment_service_fee);
    }
}
