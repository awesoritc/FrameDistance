
public class Executor {


    public static void main(String[] args){

        Setting setting = new Setting();


        //ファイルから読み込んで、部屋を作成
        String filename = setting.filename;
        Room[] rooms_static =  new Room[setting.room];
        Room[] rooms_dynamic =  new Room[setting.room];
        int[][] room_element = Util.read_room_file(filename + ".csv", setting);
        int[][] gravity_points = Util.read_gravity_file(filename + "_gravity.csv", setting);

        for (int i = 0; i < rooms_static.length; i++) {
            rooms_static[i] = new Room(room_element[i][0], room_element[i][1], room_element[i][2], room_element[i][3], room_element[i][4],
                    gravity_points, setting, setting.simulatorType_static);
            rooms_dynamic[i] = new Room(room_element[i][0], room_element[i][1], room_element[i][2], room_element[i][3], room_element[i][4],
                    gravity_points, setting, setting.simulatorType_dynamic);
        }

        Simulator simulator_static = new Simulator(rooms_static, setting, setting.simulatorType_static);
        Simulator simulator_dynamic = new Simulator(rooms_dynamic, setting, setting.simulatorType_dynamic);


        for (int i = 0; i < setting.day; i++) {

            simulator_static.create_route();
            simulator_static.do_consume_simulator();
            simulator_static.do_replenishment_simulator();

            simulator_dynamic.create_route();
            simulator_dynamic.do_consume_simulator();
            simulator_dynamic.do_replenishment_simulator();

        }


        //結果ファイル出力
        System.out.println();
    }
}
