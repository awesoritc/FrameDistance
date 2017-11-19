import java.util.ArrayList;
import java.util.Random;

public class Simulator {

    private Room[] rooms;
    private Setting setting;
    private String simulatiorType;

    //ファイル用のデータ
    ArrayList<Integer> routeTime = new ArrayList<>();
    ArrayList<ArrayList<Room>> routeHistory = new ArrayList<>();
    ArrayList<Integer> salesHistory = new ArrayList<>();
    ArrayList<Integer> shortageHistory = new ArrayList<>();
    ArrayList<Integer> expire_countHistory = new ArrayList<>();//賞味期限切れの個数

    //部屋ごとの売上・不足を保持
    private int[] sales_rooms = new int[100];
    private int[] shortage_rooms = new int [100];

    Simulator(/*Room[] rooms, */Setting setting, String simulatiorType){

        //this.rooms = rooms;
        this.setting = setting;
        this.simulatiorType = simulatiorType;

        for(int i = 0; i < 100; i++){
            sales_rooms[i] = 0;
            shortage_rooms[i] = 0;
        }


        String filename = setting.filename;
        Room[] rooms =  new Room[setting.room];
        int[][] room_element = Util.read_room_file(filename + ".csv", setting);
        int[][] gravity_points = Util.read_gravity_file(filename + "_gravity.csv", setting);

        for (int i = 0; i < setting.room; i++) {
            //同じ部屋群をそれぞれに割り当て
            rooms[i] = new Room(room_element[i][0], room_element[i][1], room_element[i][2], room_element[i][3], room_element[i][4],
                    gravity_points, setting, simulatiorType);


            //それぞれの部屋にランダムで商品を登録
            for (int j = 0; j < setting.goodsNum_per_room; j++) {
                Random rand = new Random();
                int random = rand.nextInt(setting.goodsNum_per_room);
                int version;
                if(random < setting.goods_distribution[0]){
                    version = 0;
                }else if(random < setting.goods_distribution[0] + setting.goods_distribution[1]){
                    version = 1;
                }else{
                    version = 2;
                }
                rooms[i].register_goods(version);
            }
        }

        this.rooms = rooms;
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

        int expire_count = 0;
        for(Room aRooms: rep_route){
            expire_count += aRooms.do_replenishment_room(day);
        }

        expire_countHistory.add(expire_count);
    }



    //1日の終わりに行う
    public void finish_day(){
        for (int i = 0; i < rooms.length; i++) {
            rooms[i].finish_day_room();
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

    public ArrayList<Integer> getExpire_countHistory() {
        return expire_countHistory;
    }

    public int[] getSales_rooms() {
        return sales_rooms;
    }

    public int[] getShortage_rooms() {
        return shortage_rooms;
    }
}
