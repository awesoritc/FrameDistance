import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Executor {

    /*TODO:
    * 他のエリアに近い部屋が絶対に発生しないところまでbufferを広げる
    * 　エリア判別のJavaファイルを共有
    * Goods: 需要の発生をポアソン分布に変更
    * Data_room_condition: 当日エリアで回らなかったところで、次に回ってくるまでにどの程度品切れを起こしているかを確認
    * RouteHandler: 他エリアを回る時にペナルティを設定する（回る)エリアの数が増えれば増えるほど
    * RouteHandler: 回る部屋を選択する時に、全域木を利用して部屋選択・ルート距離計算を回して、効率的なルートを作成する
    *
    */



    public static void main(String[] args){

        Setting setting = new Setting();

        //出力ファイルの初期化
        try{
            FileWriter w;

            w = new FileWriter(new File("./Data/rooms_condition_dy.csv"));
            w.write("day,roomId,ifInRoute,suf_rate,expect_shortage,dis_from_point\n");
            w.close();
            new FileWriter(new File("./Data/Route_dynamic.csv")).write("");
            new FileWriter(new File("./Data/Route_static.csv")).write("");
            new FileWriter(new File("./Data/day_based.csv")).write("");
            new FileWriter(new File("./Data/room_based.csv")).write("");
            w = new FileWriter(new File("./Data/shortage_day_room.csv"));
            w.write("simulatorType,last_rep_day,day,roomId,roomType,shortage\n");
            w.close();

            w = new FileWriter(new File("./Data/ac_goods_ss_dynamic.csv"));
            w.write("simulatorType,roomID,goodsNumber,roomType,goodsType,ac_sales,ac_shortage\n");
            w.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        //それぞれの部屋に登録する商品の番号をセット(登録はSimulatorで行う)
        ArrayList<Integer> goods_alloc = new ArrayList<>();
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

        Simulator simulator_static = new Simulator(/*rooms_static, */goods_alloc, setting, setting.simulatorType_static);
        Simulator simulator_dynamic = new Simulator(/*rooms_dynamic, */goods_alloc, setting, setting.simulatorType_dynamic);


        for (int i = 0; i < setting.day; i++) {

            int day = i;

            System.out.println(i);

            simulator_static.create_route(day);
            simulator_static.do_consume_simulator(day);
            simulator_static.do_replenishment_simulator(day);
            simulator_static.finish_day();

            simulator_dynamic.create_route(day);
            simulator_dynamic.do_consume_simulator(day);
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
        System.out.println();
        System.out.println("dynamic");
        System.out.println(simulator_dynamic.getTotal_sales());
        System.out.println(simulator_dynamic.getTotal_shortage());
        System.out.println(simulator_dynamic.getTotal_distance());
        System.out.println(simulator_dynamic.getTotal_expire_loss());


        simulator_dynamic.write_goods_shortage();





        //結果の書き出し


        /*//日毎の移動距離
        try{
            ArrayList<Integer> time_st = simulator_static.getRouteTime();
            ArrayList<Integer> time_dy = simulator_dynamic.getRouteTime();

            new FileWriter(new File("time.csv")).write("");
            PrintWriter pw_time = new PrintWriter(new BufferedWriter(new FileWriter(new File("time.csv"), true)));
            pw_time.write("day,time_static,time_dynamic\n");
            for (int i = 0; i < time_st.size(); i++) {
                //System.out.println(time_st.get(i));
                pw_time.write(i + "," +  time_st.get(i) + "," + time_dy.get(i) + "\n");
            }
            pw_time.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //部屋ごとのルート(dy)
        try{
            ArrayList<ArrayList<Room>> time_route_dy = simulator_dynamic.getRouteHistory();

            PrintWriter pw_route_dy = new PrintWriter(new BufferedWriter(new FileWriter(new File("./Data/Route_dynamic.csv"), true)));
            pw_route_dy.write("day,roomId,areaNum,pos\n");
            for (int i = 0; i < time_route_dy.size(); i++) {
                ArrayList<Room> tmp = time_route_dy.get(i);
                //pw_route_dy.write("Day:" + i + "\n");
                for (int j = 0; j < tmp.size(); j++) {
                    pw_route_dy.write(i + "," + tmp.get(j).getRoomId() + "," + tmp.get(j).getAreaNumber() +  ",(" + tmp.get(j).getX_pos() + ":" + tmp.get(j).getY_pos() + ")" + "\n");
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

        /*//日毎のss
        try{
            ArrayList<Integer> sales_st = simulator_static.getSalesHistory();
            ArrayList<Integer> shortage_st = simulator_static.getShortageHistory();
            ArrayList<Integer> sales_dy = simulator_dynamic.getSalesHistory();
            ArrayList<Integer> shortage_dy = simulator_dynamic.getShortageHistory();
            new FileWriter(new File("ss.csv")).write("");
            PrintWriter pw_history_ss = new PrintWriter(new BufferedWriter(new FileWriter(new File("ss.csv"), true)));
            pw_history_ss.write("day,sales_static,shortage_static,sales_dynamic,shortage_dynamic" + "\n");
            for (int i = 0; i < sales_st.size(); i++) {
                pw_history_ss.write(i + "," + sales_st.get(i) + "," + shortage_st.get(i) + "," + sales_dy.get(i) + "," + shortage_dy.get(i) + "\n");
            }
            pw_history_ss.write("\n");
            pw_history_ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //部屋ごとの売上と不足個数を書き出し
        try{
            new FileWriter(new File("ss_rooms.csv")).write("");
            PrintWriter pw_room = new PrintWriter(new BufferedWriter(new FileWriter(new File(
                    "ss_rooms.csv"), true)));

            pw_room.write("roomId,sales_st,shortage_st,sales_dy,shortage_dy\n");

            int[] sales_st = simulator_static.getSales_rooms();
            int[] shortage_st = simulator_static.getShortage_rooms();
            int[] sales_dy = simulator_dynamic.getSales_rooms();
            int[] shortage_dy = simulator_dynamic.getShortage_rooms();
            for (int i = 0; i < sales_st.length; i++) {
                pw_room.write(i + "," +  + sales_st[i] + "," + shortage_st[i] + "," + sales_dy[i] + "," + shortage_dy[i] + "\n");
            }
            pw_room.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        //日毎の廃棄ロスを書き出し
        try{
            new FileWriter(new File("expire_loss.csv")).write("");

            ArrayList<Integer> loss_st = simulator_static.getExpire_countHistory();
            ArrayList<Integer> loss_dy = simulator_dynamic.getExpire_countHistory();
            PrintWriter pw_loss = new PrintWriter(new BufferedWriter(new FileWriter(new File(
                    "expire_loss.csv"), true)));

            pw_loss.write("day,loss_static,loss_dynamic\n");

            for (int i = 0; i < loss_st.size(); i++) {
                pw_loss.write(i + "," +  loss_st.get(i) + "," + loss_dy.get(i) + "\n");
            }
            pw_loss.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //部屋ごとの廃棄ロスを書き出し
        try{
            new FileWriter(new File("expire_loss_room.csv")).write("");
            ArrayList<Integer> loss_st = new ArrayList<>(Arrays.asList(simulator_static.getRoom_expire()));
            ArrayList<Integer> loss_dy = new ArrayList<>(Arrays.asList(simulator_dynamic.getRoom_expire()));
            PrintWriter pw_loss = new PrintWriter(new BufferedWriter(new FileWriter(new File(
                    "expire_loss_room.csv"), true)));

            pw_loss.write("roomId,loss_static,loss_dynamic\n");

            for (int i = 0; i < loss_st.size(); i++) {
                pw_loss.write(i + "," +  loss_st.get(i) + "," + loss_dy.get(i) + "\n");
            }
            pw_loss.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //日毎の稼働率書き出し
        try{
            new FileWriter(new File("availability.csv")).write("");
            ArrayList<Double> availability_st = simulator_static.getAvailabilityHistory();
            ArrayList<Double> availability_dy = simulator_dynamic.getAvailabilityHistory();
            PrintWriter pw_availability = new PrintWriter(new BufferedWriter(new FileWriter(new File(
                    "availability.csv"), true)));

            pw_availability.write("day,availability_static,availability_dynamic\n");

            for (int i = 0; i < availability_st.size(); i++) {
                pw_availability.write(i + "," +  availability_st.get(i) + "," + availability_dy.get(i) + "\n");
            }
            pw_availability.close();

        } catch (IOException e) {
            e.printStackTrace();
        }*/







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

            PrintWriter pw_time = new PrintWriter(new BufferedWriter(new FileWriter(new File("./Data/day_based.csv"), true)));
            pw_time.write("day,time_static,time_dynamic,sales_static,shortage_static,sales_dynamic,shortage_dynamic,loss_static,loss_dynamic,availability_static,availability_dynamic\n");
            for (int i = 0; i < time_st.size(); i++) {
                //System.out.println(time_st.get(i));
                pw_time.write(i + "," +  time_st.get(i) + "," + time_dy.get(i) + "," +
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
    }
}
