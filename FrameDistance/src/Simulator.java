import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Simulator {

    private Room[] rooms;
    private Setting setting;
    private String simulatiorType;

    //ファイル用のデータ
    ArrayList<Integer> routeDistance = new ArrayList<>();
    ArrayList<ArrayList<Room>> routeHistory = new ArrayList<>();
    ArrayList<Integer> salesHistory = new ArrayList<>();
    ArrayList<Integer> shortageHistory = new ArrayList<>();
    ArrayList<Integer> expire_countHistory = new ArrayList<>();//賞味期限切れの個数
    Integer[] room_expire;//部屋ごとの賞味期限切れの個数
    ArrayList<Double> availabilityHistory = new ArrayList<>();//稼働率の推移

    //部屋ごとの売上・不足を保持
    private int[] sales_rooms = new int[100];
    private int[] shortage_rooms = new int [100];

    Simulator(/*Room[] rooms, */ArrayList<Integer> goods_alloc, Setting setting, String simulatiorType){

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

            //Executorで作成した番号リストに従って商品を登録する
            for (int j = 0; j < setting.goodsNum_per_room; j++) {
                rooms[i].register_goods(goods_alloc.get((i*10)+j));
            }
        }

        this.rooms = rooms;


        //初期化
        room_expire = new Integer[setting.room];
        for (int i = 0; i < room_expire.length; i++) {
            room_expire[i] = 0;
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
        //if(simulatiorType.equals(setting.simulatorType_static)) System.out.println(handle.calculate_route_distance(route));//1日のルート距離を出力
        routeDistance.add(handle.calculate_route_distance(route));

        //稼働率を計算
        double availability = ((handle.calculate_route_distance(route)*setting.move_time_per_1) + (route.size()*setting.service_time_per_room)) / setting.work_time;
        //if(simulatiorType.equals(setting.simulatorType_static))System.out.println(availability);//stの稼働率を出力
        availabilityHistory.add(availability);

        //TODO:稼働率を書き出し・廃棄ロスと合わせて損失を計算・dyがよくなるような方法を考える
        //そもそも、エリアを優先にしてその周りを回れたら回る方式に変更する？

        rep_route = route;
    }


    public void do_consume_simulator(int day){

        int sales = 0;
        int shortage = 0;

        for (int i = 0; i < rooms.length; i++) {
            int[] tmp = rooms[i].do_consume_room(day);
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


    int total_expire_loss = 0;

    public void do_replenishment_simulator(int day){
        routeHistory.add(rep_route);

        int expire_count = 0;
        for (int i = 0; i < rep_route.size(); i++) {
            int room_exp = rep_route.get(i).do_replenishment_room(day);
            expire_count += room_exp;
            room_expire[rep_route.get(i).getRoomId()] += room_exp;
            total_expire_loss += room_exp;
        }

        expire_countHistory.add(expire_count);
    }



    //1日の終わりに行う
    public void finish_day(){
        for (int i = 0; i < rooms.length; i++) {
            rooms[i].finish_day_room();
        }
    }





    //使い捨て

    //商品ごとの累積の不足数を書き出し
    public void write_goods_shortage(){

        for (int i = 0; i < rooms.length; i++) {
            Room room = rooms[i];
            ArrayList<Goods> goodslist = room.getGoodsList();
            try{
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("./Data/ac_goods_ss_" + simulatiorType +".csv"), true)));
                for (int j = 0; j < goodslist.size(); j++) {
                    //(simulatorType,roomID,goodsNumber,roomType,goodsType,ac_shortage)
                    pw.write(simulatiorType + "," + room.getRoomId() + ","  + j + "," + room.getRoomType() + "," + goodslist.get(j).getGoodsType() + "," + goodslist.get(j).getAc_sales() + "," + goodslist.get(j).getAc_shortage() + "\n");
                }
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    //getter,setter

    public int getTotal_sales() {
        return total_sales;
    }

    public int getTotal_shortage() {
        return total_shortage;
    }

    public int getTotal_expire_loss() {
        return total_expire_loss;
    }

    public int getTotal_distance(){
        int distance = 0;
        for (int t: routeDistance) {
            distance += t;
        }
        return distance;
    }

    public ArrayList<Integer> getRouteDistance() {
        return routeDistance;
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

    public Integer[] getRoom_expire() {
        return room_expire;
    }

    public ArrayList<Double> getAvailabilityHistory() {
        return availabilityHistory;
    }

    public int[] getSales_rooms() {
        return sales_rooms;
    }

    public int[] getShortage_rooms() {
        return shortage_rooms;
    }
}
