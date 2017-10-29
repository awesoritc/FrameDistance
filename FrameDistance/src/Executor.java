public class Executor {


    public static void main(String[] args){

        Setting setting = new Setting();


        //ファイルから読み込んで、部屋を作成
        String filename = setting.filename;
        Room[] rooms =  dag

        Simulator simulator_static = new Simulator(rooms, setting, "static");
        Simulator simulator_dynamic = new Simulator(rooms, setting, "dynamic");


        for (int i = 0; i < setting.day; i++) {

            simulator_static.create_route();
            simulator_static.do_consume_room();
            simulator_static.do_replenishment();

            simulator_dynamic.create_route();
            simulator_dynamic.do_consume_room();
            simulator_dynamic.do_replenishment();

        }


        for (int i = 0; i < ; i++) {
            //ファイル出力

        }
        System.out.println();
    }
}
