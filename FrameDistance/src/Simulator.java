import java.util.ArrayList;

public class Simulator {

    Room[] rooms;
    Setting setting;
    String simulatiorType;

    Simulator(Room[] rooms, Setting setting, String simulatiorType){

        this.rooms = rooms;
        this.setting = setting;
        this.simulatiorType = simulatiorType;

    }


    int total_sales = 0;
    int total_shortage = 0;

    //当日の補充ルート
    ArrayList<Room> rep_route = new ArrayList<>();
    public void create_route(){

        //TODO:ルート専用のファイルを作成
        //部屋を選択
        //id順に並び替え
        //巡回した時の距離を計算
        rep_route 
    }

    public void do_consume_simulator(){

        int sales = 0;
        int shortage = 0;

        for (int i = 0; i < rooms.length; i++) {
            int[] tmp = rooms[i].do_consume_room();
            sales += tmp[0];
            shortage += tmp[1];
        }

        total_sales += sales;
        total_shortage += shortage;
    }

    public void do_replenishment_simulator(){
        for(Room aRooms: rep_route){
            aRooms.do_replenishment_room();
        }
    }

}
