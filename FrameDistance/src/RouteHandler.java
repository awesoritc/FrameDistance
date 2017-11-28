import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class RouteHandler {

    //ルートを作成する時に毎回呼ばれる

    private Setting setting;
    private String simulatorType;

    private int day;

    RouteHandler(int day, Setting setting, String simulatorType){
        this.day = day;
        this.setting = setting;
        this.simulatorType = simulatorType;
    }


    public ArrayList<Room> route_creator(Room[] rooms){

        int current_area = day%setting.area;
        ArrayList<Room> route = new ArrayList<>();

        if(simulatorType.equals(setting.simulatorType_static)){
            //固定のルート
            for (int i = 0; i < setting.room; i++) {
                if(rooms[i].getAreaNumber() == current_area){
                    route.add(rooms[i]);
                }
            }
            route = setBetterOrder(route);

        }else if(simulatorType.equals(setting.simulatorType_dynamic)){
            //変動のルート
            if(setting.routeType.equals(setting.routeType_value)){
                route = setBetterOrder(setIdOrder(basedOnValue(rooms, current_area)));
                //route = setBetterOrder(setIdOrder(basedOnProfit(rooms, current_area)));
            }else if(setting.routeType.equals(setting.routeType_greedy)){
                route = setBetterOrder(setIdOrder(basedOnSuf_rate(rooms, current_area)));
            }

        }

        return route;
    }


    //ルートの距離を返却
    public int calculate_route_distance(ArrayList<Room> route){

        if(route.size() != 0){

            int[] hub_point = setting.hub_point;
            int current_x = hub_point[0];
            int current_y = hub_point[1];

            /*int current_x = route.get(0).getX_pos();
            int current_y = route.get(0).getY_pos();*/

            int routetime = 0;
            for (int i = 0; i < route.size(); i++) {

                int next_x = route.get(i).getX_pos();
                int next_y = route.get(i).getY_pos();
                int next_time = 0;
                if(current_x > next_x){
                    next_time += current_x - next_x;
                }else{
                    next_time += next_x - current_x;
                }
                if(current_y > next_y){
                    next_time += current_y - next_y;
                }else{
                    next_time += next_y - current_y;
                }

                routetime += next_time;

                current_x = next_x;
                current_y = next_y;
            }

            //最後ハブに戻る
            int next_x = hub_point[0];
            int next_y = hub_point[1];
            int next_time = 0;
            if(current_x > next_x){
                next_time += current_x - next_x;
            }else{
                next_time += next_x - current_x;
            }
            if(current_y > next_y){
                next_time += current_y - next_y;
            }else{
                next_time += next_y - current_y;
            }

            routetime += next_time;


            //ルートを回ることによってかかる時間を計算
            double time = 0;
            time += route.size()*setting.service_time_per_room;
            time += routetime*setting.move_time_per_1;
            //System.out.println(time/setting.work_time);

            return routetime;
        }

        return 0;
    }







    //ルート作成用のメソッド

    //補充優先度
    private ArrayList<Room> basedOnValue(Room[] r, int current_area){

        ArrayList<Room> room = new ArrayList<>(Arrays.asList(r));

        ArrayList<Room> tmp_room = new ArrayList<>(Arrays.asList(r));


        ArrayList<Room> route = new ArrayList<>();//補充に回る部屋の集合


        //最大日数を超えて補充に回っていない部屋を追加
        //TODO:expire_flagがtrueの部屋を必ず回る(最大日数は削除)
        ArrayList<Integer> array = new ArrayList<>();
        for (int i = 0; i < room.size(); i++) {
            if(room.get(i).getAreaNumber() == current_area && room.get(i).isExpire_flag()){
                if(route.size() >= setting.limit){
                    return route;
                }
                route.add(room.get(i));
                array.add(room.get(i).getRoomId());
            }
        }

        /*//最大日数を超えたもの補充
        array = new ArrayList<>();
        for (int i = 0; i < room.size(); i++){
            if(room.get(i).getAreaNumber() == current_area && room.get(i).isOverLongest(day)){
                route.add(room.get(i));
                array.add(i);
            }
        }*/


        //value大きい順に並べる
        for (int i = 0; i < room.size(); i++) {
            for (int j = 0; j < room.size(); j++) {
                if(i < j){
                    if(room.get(i).rep_value(current_area) < room.get(j).rep_value(current_area)){
                        Room tmp = room.get(i);
                        room.set(i, room.get(j));
                        room.set(j, tmp);
                    }
                }
            }
        }

        //ルートに優先度の高い部屋を追加
        outside: for (int i = 0; i < setting.room; i++) {

            for (int j = 0; j < array.size(); j++) {
                if(room.get(i).getRoomId() == array.get(j)){
                    continue outside;
                }
            }

            if(room.get(i).rep_value(current_area) > 0){
                route.add(room.get(i));
            }

            if(route.size() >= setting.limit){
                break;
            }
        }

        //補充しなければいけない場所がなければ、エリア補充をする
        if(route.size() == 0){
            for (int i = 0; i < setting.room; i++) {
                if(r[i].getAreaNumber() == current_area){
                    route.add(r[i]);
                }
            }
        }

        //ここでそれぞれの部屋の状況を書き出し
        if(simulatorType.equals(setting.simulatorType_dynamic)){
            try{
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("./Data/rooms_condition_dy.csv"), true)));
                for (int i = 0; i < r.length; i++) {
                    boolean ifInRoute = false;
                    for (Room aRooms: route){
                        if(r[i].getRoomId() == aRooms.getRoomId()){
                            ifInRoute = true;
                            break;
                        }
                    }
                    //(day,roomId,ifInRoute,suf_rate,expect_shortage,dis_from_point)
                    pw.write(day + "," + r[i].getRoomId() + "," + ifInRoute + "," + r[i].suf_rate() + "," + r[i].expect_shortage(current_area) + "," + r[i].getDistance_to_gravity()[current_area] + "\n");
                }
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return route;
    }


    //商品が少なくなったところを回る
    public ArrayList<Room> basedOnSuf_rate(Room[] r, int current_area){

        ArrayList<Room> room = new ArrayList<>(Arrays.asList(r));
        ArrayList<Room> route = new ArrayList<>();

        ArrayList<Integer> array = new ArrayList<>();
        for (int i = 0; i < room.size(); i++) {
            if(room.get(i).getAreaNumber() == current_area && room.get(i).isExpire_flag()){
                route.add(room.get(i));
                array.add(room.get(i).getRoomId());
            }
        }

        //suf_rate小さい順に並べる
        for (int i = 0; i < room.size(); i++) {
            for (int j = 0; j < room.size(); j++) {
                if(i < j){
                    if(room.get(i).suf_rate() > room.get(j).suf_rate()){
                        Room tmp = room.get(i);
                        room.set(i, room.get(j));
                        room.set(j, tmp);
                    }
                }
            }
        }

        //ルート追加
        outside: for (int i = 0; i < room.size(); i++) {

            for (int j = 0; j < array.size(); j++) {
                if(room.get(i).getRoomId() == array.get(j)){
                    continue outside;
                }
            }

            if(room.get(i).suf_rate() < 1){
                route.add(room.get(i));
                if(route.size() >= setting.limit){
                    break;
                }
            }
        }

        //補充しなければいけない場所がなければ、エリア補充をする
        if(route.size() == 0){
            for (int i = 0; i < setting.rooms_area; i++) {
                route.add(r[i+(current_area*setting.rooms_area)]);
            }
        }

        return route;
    }



    //機会損失回避のメリットと、補充の人件費を考慮した金額ベースの補充優先度
    public ArrayList<Room> basedOnProfit(Room[] r, int current_area){

        ArrayList<Room> room = new ArrayList<>(Arrays.asList(r));
        ArrayList<Room> route = new ArrayList<>();

        ArrayList<Integer> array = new ArrayList<>();
        for (int i = 0; i < room.size(); i++) {
            if(room.get(i).getAreaNumber() == current_area && room.get(i).isExpire_flag()){
                route.add(room.get(i));
                array.add(room.get(i).getRoomId());
            }
        }

        //profit大きい順に並べる
        for (int i = 0; i < room.size(); i++) {
            for (int j = 0; j < room.size(); j++) {
                if(i < j){
                    if(room.get(i).profit(current_area) < room.get(j).profit(current_area)){
                        Room tmp = room.get(i);
                        room.set(i, room.get(j));
                        room.set(j, tmp);
                    }
                }
            }
        }

        //ルート追加
        outside: for (int i = 0; i < room.size(); i++) {

            for (int j = 0; j < array.size(); j++) {
                if(room.get(i).getRoomId() == array.get(j)){
                    continue outside;
                }
            }

            if(room.get(i).profit(current_area) > 0){
                route.add(room.get(i));
                if(route.size() >= setting.limit){
                    break;
                }
            }
        }

        //補充しなければいけない場所がなければ、エリア補充をする
        if(route.size() == 0){
            for (int i = 0; i < setting.rooms_area; i++) {
                route.add(r[i+(current_area*setting.rooms_area)]);
            }
        }

        return route;
    }



    //id順に並べ直す
    private ArrayList<Room> setIdOrder(ArrayList<Room> route){

        for (int i = 0; i < route.size(); i++) {
            for (int j = 0; j < route.size(); j++) {
                if(i < j){
                    if(route.get(i).getRoomId() > route.get(j).getRoomId()){
                        Room tmp = route.get(i);
                        route.set(i, route.get(j));
                        route.set(j, tmp);
                    }
                }
            }
        }
        return route;
    }


    //何かしら経路が短くなる順に並べなおす
    private ArrayList<Room> setBetterOrder(ArrayList<Room> route){

        //TODO:局所探索法に変更(要修正)

        //局所探索法
        //1. ランダムルートを作成
        //2. 2つの地点の組み合わせを取得しスワップ
        //3. 距離を計算し、短縮されていればルート更新
        //4. 2,3を繰り返し

        //これらを何回か繰り返し、一番良かったものを取得


        ArrayList<Room> best_route = new ArrayList<>(route);
        int best_distance = calculate_route_distance(best_route);

        for (int a = 0; a < 1000; a++) {
            //1.ランダムルートの作成
            ArrayList<Room> random_route = new ArrayList<>(route);
            double[] d = new double[route.size()];
            for (int i = 0; i < d.length; i++) {
                d[i] = Math.random();
            }
            for (int i = 0; i < random_route.size(); i++) {
                for (int j = 0; j < random_route.size(); j++) {
                    if(i < j){
                        if(d[i] > d[j]){
                            double tmp_d = d[i];
                            d[i] = d[j];
                            d[j] = tmp_d;

                            Room tmp_room = random_route.get(i);
                            random_route.set(i, random_route.get(j));
                            random_route.set(j, tmp_room);
                        }
                    }
                }
            }

            //ランダムルートの測定
            ArrayList<Room> tmp_best_route = new ArrayList<>(random_route);
            int tmp_best_distance = calculate_route_distance(tmp_best_route);
            if(calculate_route_distance(random_route) < tmp_best_distance){
                tmp_best_route = new ArrayList<>(random_route);
                tmp_best_distance = calculate_route_distance(tmp_best_route);
            }


            //2.2つの地点を順番に取得してスワップ
            ArrayList<Room> search_route = new ArrayList<>(random_route);

            int pre_best_distance;
            do{
                pre_best_distance = tmp_best_distance;
                for (int i = 0; i < search_route.size(); i++) {
                    for (int j = 0; j < search_route.size(); j++) {
                        ArrayList<Room> tmp_route = new ArrayList<>(search_route);
                        if(i < j){
                            Room tmp = tmp_route.get(i);
                            tmp_route.set(i, tmp_route.get(j));
                            tmp_route.set(j, tmp);

                            //tmp_routeの測定
                            if(calculate_route_distance(tmp_route) < tmp_best_distance){
                                tmp_best_route = new ArrayList<>(tmp_route);
                                tmp_best_distance = calculate_route_distance(tmp_best_route);
                            }
                        }
                    }
                }
                search_route = new ArrayList<>(tmp_best_route);
            }while(tmp_best_distance < pre_best_distance);

            if(tmp_best_distance < best_distance){
                best_route = new ArrayList<>(tmp_best_route);
                best_distance = calculate_route_distance(best_route);
            }
        }

        return best_route;



/*
        //独自アルゴリズム


        //100000回適当に入れ替えてみて一番いいやつを採用
        ArrayList<Room> route_tmp = new ArrayList<>(route);
        Random rand = new Random();

        int best = calculate_route_distance(route);

        for (int i = 0; i < 100000; i++) {
            int ran1 = rand.nextInt(route_tmp.size());
            int ran2 = rand.nextInt(route_tmp.size());
            Room tmp = route_tmp.get(ran1);
            route_tmp.set(ran1, route_tmp.get(ran2));
            route_tmp.set(ran2, tmp);
            if(calculate_route_distance(route_tmp) < best){
                best = calculate_route_distance(route_tmp);
                route = new ArrayList<>(route_tmp);
                route_tmp = new ArrayList<>(route);
            }
            if(rand.nextInt(100) < 50){
                route_tmp = new ArrayList<>(route);
            }
        }
        return route;

        */
    }








    //根本的なルート作成方法の変更
    //TODO:ルートの作成を ルート選択・最適度計算 を回して最適なルートを作成する
    public void route_create_agile(Room[] rooms){
        //何かしらの基準で部屋を取得

        //距離と回収できる不足個数を兼ねた評価基準での評価
        //TODO:評価値の作成

        //評価値が高いかどうかを
    }


    //近似解のルートを作成するのに時間がかかるので、ルートを作成せずに距離が長いか短いかを全域木を利用して大体計測する。
    public int calc_about_distance(ArrayList<Room> selected_rooms/*選択された部屋群*/, int current_area){

        //全ての部屋の (current_areaの重心との距離*2) を足し合わせたものを、最悪距離として返却
        int worst_distance = 0;

        ArrayList<Room> tmp = new ArrayList<>(selected_rooms);
        for (int i = 0; i < tmp.size(); i++) {
            worst_distance += tmp.get(i).getDistance_to_gravity()[current_area];
        }

        return worst_distance;
    }

}
