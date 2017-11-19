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

    //部屋ごとの売上・不足を保持
    private int[] sales_rooms = new int[100];
    private int[] shortage_rooms = new int [100];

    private Factory factory;

    Simulator(Setting setting, String simulatiorType){


        this.setting = setting;
        this.simulatiorType = simulatiorType;

        factory = new Factory(simulatiorType, setting);

        this.rooms = create_rooms();

        for(int i = 0; i < setting.room; i++) {
            sales_rooms[i] = 0;
            shortage_rooms[i] = 0;
        }
    }


    int total_sales = 0;
    int total_shortage = 0;

    //当日の補充ルート
    ArrayList<Room> rep_route = new ArrayList<>();
    public void create_route(int day){

        //TODO:1日進める
        factory.proceed_day_factory();

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
            //int[] tmp = rooms[i].do_consume_room();
            int[][] goodsNum_ss = rooms[i].do_consume_room();
            int[] tmp = {0, 0};
            for (int j = 0; j < goodsNum_ss.length; j++) {
                tmp[0] += goodsNum_ss[j][0];
                tmp[1] += goodsNum_ss[j][1];
            }

            sales += tmp[0];
            shortage += tmp[1];
            //部屋ごとの値を保持
            sales_rooms[i] += tmp[0];
            shortage_rooms[i] += tmp[1];

            //TODO:ここで、Itemの処理をつける(商品ごとにいくつ売れたかを返すようにしなければいけない)
            //部屋の消費数をその部屋のストックから削除する
            for (int j = 0; j < goodsNum_ss.length; j++) {
                factory.do_consume_factory(i, j, goodsNum_ss[j][0]);
            }
        }

        total_sales += sales;
        total_shortage += shortage;

        salesHistory.add(sales);
        shortageHistory.add(shortage);
    }


    public void do_replenishment_simulator(int day){
        routeHistory.add(rep_route);
        /*for(Room aRooms: rep_route){
            aRooms.do_replenishment_room(day);
        }*/
        for (int i = 0; i < rep_route.size(); i++) {
            int[] rep_goodsNum = rep_route.get(i).do_replenishment_room(day);
            ArrayList<Goods> goodss = rep_route.get(i).getGoodsList();

            //TODO:ここで、部屋ごとに補充した数商品を作成し、部屋に紐づける(商品ごとにいくつ補充したかを返すようにしなければならない)
            for (int j = 0; j < rep_goodsNum.length; j++) {
                //補充した分のitemを作成し、セット
                factory.do_replenishment_factory(rep_route.get(i).getRoomId(), rep_route.get(i).getGoodsList().get(j).getGoodsType(), j, rep_goodsNum[j]);

                //賞味期限のチェック
                factory.check_expire(rep_route.get(i).getRoomId(), goodss.get(j).getGoodsType(), j);
            }


        }
    }








    //使い捨てのメソッド

    //部屋を作成
    private Room[] create_rooms(){
        String filename = setting.filename;
        Room[] rooms =  new Room[setting.room];
        int[][] room_element = Util.read_room_file(filename + ".csv", setting);
        int[][] gravity_points = Util.read_gravity_file(filename + "_gravity.csv", setting);

        for (int i = 0; i < setting.room; i++) {
            //同じ部屋群をそれぞれに割り当て
            rooms[i] = new Room(room_element[i][0], room_element[i][1], room_element[i][2], room_element[i][3], room_element[i][4],
                    gravity_points, setting, setting.simulatorType_static);

            //それぞれの部屋にランダムで商品を登録
            for (int j = 0; j < setting.goods_num; j++) {
                Random rand = new Random();
                int random = rand.nextInt(setting.goods_num);
                int version;
                if(random < setting.goods_distribution[0]){
                    version = 0;
                }else if(random < setting.goods_distribution[0] + setting.goods_distribution[1]){
                    version = 1;
                }else{
                    version = 2;
                }
                rooms[i].register_goods(version);
                factory.init(i, version, j);
            }
        }
        return rooms;
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
