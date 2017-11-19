import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Executor {

    //TODO:2本以上のシミュレーターを同時に走らせれるようにする？

    public static void main(String[] args){

        Setting setting = new Setting();


        //ファイルから読み込んで、部屋を作成
        /*String filename = setting.filename;
        Room[] rooms_static =  new Room[setting.room];
        Room[] rooms_dynamic =  new Room[setting.room];
        int[][] room_element = Util.read_room_file(filename + ".csv", setting);
        int[][] gravity_points = Util.read_gravity_file(filename + "_gravity.csv", setting);

        for (int i = 0; i < setting.room; i++) {
            //同じ部屋群をそれぞれに割り当て
            rooms_static[i] = new Room(room_element[i][0], room_element[i][1], room_element[i][2], room_element[i][3], room_element[i][4],
                    gravity_points, setting, setting.simulatorType_static);
            rooms_dynamic[i] = new Room(room_element[i][0], room_element[i][1], room_element[i][2], room_element[i][3], room_element[i][4],
                    gravity_points, setting, setting.simulatorType_dynamic);


            //それぞれの部屋にランダムで商品を登録
            for (int j = 0; j < 10; j++) {
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
                rooms_static[i].register_goods(version);
                rooms_dynamic[i].register_goods(version);
            }
        }*/

        Simulator simulator_static = new Simulator(/*rooms_static, */setting, setting.simulatorType_static);
        Simulator simulator_dynamic = new Simulator(/*rooms_dynamic, */setting, setting.simulatorType_dynamic);


        for (int i = 0; i < setting.day; i++) {

            int day = i;

            simulator_static.create_route(day);
            simulator_static.do_consume_simulator();
            simulator_static.do_replenishment_simulator(day);
            simulator_static.finish_day();

            simulator_dynamic.create_route(day);
            simulator_dynamic.do_consume_simulator();
            simulator_dynamic.do_replenishment_simulator(day);
            simulator_dynamic.finish_day();
        }


        //結果出力
        System.out.println();
        System.out.println("static");
        System.out.println(simulator_static.getTotal_sales());
        System.out.println(simulator_static.getTotal_shortage());
        System.out.println(simulator_static.getTotal_time());
        System.out.println();
        System.out.println("dynamic");
        System.out.println(simulator_dynamic.getTotal_sales());
        System.out.println(simulator_dynamic.getTotal_shortage());
        System.out.println(simulator_dynamic.getTotal_time());








        //結果の書き出し
        ArrayList<Integer> time_st = simulator_static.getRouteTime();
        ArrayList<Integer> time_dy = simulator_dynamic.getRouteTime();

        ArrayList<ArrayList<Room>> time_route_dy = simulator_dynamic.getRouteHistory();
        ArrayList<ArrayList<Room>> time_route_st = simulator_static.getRouteHistory();

        try{
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
        }

        try{

            new FileWriter(new File("Route_dynamic.csv")).write("");
            PrintWriter pw_route_dy = new PrintWriter(new BufferedWriter(new FileWriter(new File("Route_dynamic.csv"), true)));
            pw_route_dy.write("roomId,pos,day\n");
            for (int i = 0; i < time_route_dy.size(); i++) {
                ArrayList<Room> tmp = time_route_dy.get(i);
                //pw_route_dy.write("Day:" + i + "\n");
                for (int j = 0; j < tmp.size(); j++) {
                    pw_route_dy.write(tmp.get(j).getRoomId() + ",(" + tmp.get(j).getX_pos() + ":" + tmp.get(j).getY_pos() + ")," + i + "\n");
                }
                //pw_route_dy.write("\n");
            }
            pw_route_dy.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try{

            new FileWriter(new File("Route_static.csv")).write("");
            PrintWriter pw_route_st = new PrintWriter(new BufferedWriter(new FileWriter(new File("Route_static.csv"), true)));
            pw_route_st.write("roomId,pos,day\n");
            for (int i = 0; i < time_route_st.size(); i++) {
                ArrayList<Room> tmp = time_route_st.get(i);
                //pw_route_st.write("Day:" + i + "\n");
                for (int j = 0; j < tmp.size(); j++) {
                    pw_route_st.write(tmp.get(j).getRoomId() + ",(" + tmp.get(j).getX_pos() + ":" + tmp.get(j).getY_pos() + ")," + i + "\n");
                }
                //pw_route_st.write("\n");
            }
            pw_route_st.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        try{
            ArrayList<Integer> sales_st = simulator_static.getSalesHistory();
            ArrayList<Integer> shortage_st = simulator_static.getShortageHistory();
            ArrayList<Integer> sales_dy = simulator_dynamic.getSalesHistory();
            ArrayList<Integer> shortage_dy = simulator_dynamic.getShortageHistory();
            new FileWriter(new File("ss.csv")).write("");
            PrintWriter pw_history_ss = new PrintWriter(new BufferedWriter(new FileWriter(new File("ss.csv"), true)));
            pw_history_ss.write("sales_static,shortage_static,sales_dynamic,shortage_dynamic" + "\n");
            for (int i = 0; i < sales_st.size(); i++) {
                pw_history_ss.write(sales_st.get(i) + "," + shortage_st.get(i) + "," + sales_dy.get(i) + "," + shortage_dy.get(i) + "\n");
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


        //廃棄ロスを書き出し
        try{
            new FileWriter(new File("loss.csv")).write("");

            ArrayList<Integer> loss_st = simulator_static.getExpire_countHistory();
            ArrayList<Integer> loss_dy = simulator_dynamic.getExpire_countHistory();
            PrintWriter pw_loss = new PrintWriter(new BufferedWriter(new FileWriter(new File(
                    "loss.csv"), true)));

            pw_loss.write("day,loss_static,loss_dynamic\n");

            for (int i = 0; i < loss_st.size(); i++) {
                pw_loss.write(i + "," +  loss_st.get(i) + "," + loss_dy.get(i) + "\n");
            }
            pw_loss.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
