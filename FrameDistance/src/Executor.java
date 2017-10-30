import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Executor {


    public static void main(String[] args){

        Setting setting = new Setting();


        //ファイルから読み込んで、部屋を作成
        String filename = setting.filename;
        Room[] rooms_static =  new Room[setting.room];
        Room[] rooms_dynamic =  new Room[setting.room];
        int[][] room_element = Util.read_room_file(filename + ".csv", setting);
        int[][] gravity_points = Util.read_gravity_file(filename + "_gravity.csv", setting);

        for (int i = 0; i < setting.room; i++) {
            rooms_static[i] = new Room(room_element[i][0], room_element[i][1], room_element[i][2], room_element[i][3], room_element[i][4],
                    gravity_points, setting, setting.simulatorType_static);
            rooms_dynamic[i] = new Room(room_element[i][0], room_element[i][1], room_element[i][2], room_element[i][3], room_element[i][4],
                    gravity_points, setting, setting.simulatorType_dynamic);


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
        }

        Simulator simulator_static = new Simulator(rooms_static, setting, setting.simulatorType_static);
        Simulator simulator_dynamic = new Simulator(rooms_dynamic, setting, setting.simulatorType_dynamic);


        for (int i = 0; i < setting.day; i++) {

            simulator_static.create_route(i);
            simulator_static.do_consume_simulator();
            simulator_static.do_replenishment_simulator();

            simulator_dynamic.create_route(i);
            simulator_dynamic.do_consume_simulator();
            simulator_dynamic.do_replenishment_simulator();

        }


        //結果出力
        System.out.println();
        System.out.println(simulator_static.getTotal_sales());
        System.out.println(simulator_static.getTotal_shortage());
        System.out.println(simulator_static.getTotal_time());
        System.out.println();
        System.out.println(simulator_dynamic.getTotal_sales());
        System.out.println(simulator_dynamic.getTotal_shortage());
        System.out.println(simulator_dynamic.getTotal_time());








        //結果の出力
        ArrayList<Integer> time_st = simulator_static.getRouteTime();
        ArrayList<Integer> time_dy = simulator_dynamic.getRouteTime();

        try{
            PrintWriter pw_st = new PrintWriter(new BufferedWriter(new FileWriter(new File("Time_static.csv"), true)));
            for (int i = 0; i < time_st.size(); i++) {
                //System.out.println(time_st.get(i));
                pw_st.write(time_st.get(i) + "\n");
            }
            pw_st.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            PrintWriter pw_dy = new PrintWriter(new BufferedWriter(new FileWriter(new File("Time_dynamic.csv"), true)));
            for (int i = 0; i < time_st.size(); i++) {
                pw_dy.write(time_dy.get(i) + "\n");
            }
            pw_dy.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
