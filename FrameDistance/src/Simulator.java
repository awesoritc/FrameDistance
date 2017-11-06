import java.util.ArrayList;

public class Simulator {

    private Room[] rooms;
    private Setting setting;
    private String simulatiorType;

    //ファイル用のデータ
    ArrayList<Integer> routeTime = new ArrayList<>();
    ArrayList<ArrayList<Room>> routeHistory = new ArrayList<>();
    ArrayList<Integer> salesHistory = new ArrayList<>();
    ArrayList<Integer> shortageHistory = new ArrayList<>();

    //部屋ごとの売上・不足を保持
    private int[] sales_rooms = new int[100];
    private int[] shortage_rooms = new int [100];

    Simulator(Room[] rooms, Setting setting, String simulatiorType){

        this.rooms = rooms;
        this.setting = setting;
        this.simulatiorType = simulatiorType;

        for(int i = 0; i < 100; i++){
            sales_rooms[i] = 0;
            shortage_rooms[i] = 0;
        }
    }


    int total_sales = 0;
    int total_shortage = 0;

    //当日の補充ルート
    ArrayList<Room> rep_route = new ArrayList<>();
    public void create_route(int day){

        //部屋を選択
        //id順に並び替え
        //巡回した時の距離を計算
        RouteHandler handle = new RouteHandler(day, setting, simulatiorType);
        ArrayList<Room> route = handle.route_creator(rooms);
        routeTime.add(handle.calculate_route_time(route));

        rep_route = route;
    }


    public void do_consume_simulator(){

        int sales = 0;
        int shortage = 0;

        for (int i = 0; i < rooms.length; i++) {
            int[] tmp = rooms[i].do_consume_room();
            sales += tmp[0];
            shortage += tmp[1];
            //部屋ごとの値を保持
            sales_rooms[i] += tmp[0];
            shortage_rooms[i] += tmp[1];
        }

        total_sales += sales;
        total_shortage += shortage;

        salesHistory.add(sales);
        shortageHistory.add(shortage);
    }

    public void do_replenishment_simulator(int day){
        routeHistory.add(rep_route);
        for(Room aRooms: rep_route){
            aRooms.do_replenishment_room(day);
        }
    }






    //getter,setter

    public int getTotal_sales() {
        return total_sales;
    }

    public int getTotal_shortage() {
        return total_shortage;
    }

    public int getTotal_time(){
        int time = 0;
        for (int t: routeTime) {
            time += t;
        }
        return time;
    }

    public ArrayList<Integer> getRouteTime() {
        return routeTime;
    }

    public ArrayList<ArrayList<Room>> getRouteHistory() {
        return routeHistory;
    }

    public ArrayList<Integer> getSalesHistory() {
        return salesHistory;
    }

    public ArrayList<Integer> getShortageHistory() {
        return shortageHistory;
    }

    public int[] getSales_rooms() {
        return sales_rooms;
    }

    public int[] getShortage_rooms() {
        return shortage_rooms;
    }
}
