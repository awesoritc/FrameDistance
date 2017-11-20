import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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
            }else if(setting.routeType.equals(setting.routeType_greedy)){
                route = setBetterOrder(setIdOrder(basedOnSuf_rate(rooms, current_area)));
            }

        }

        return route;
    }


    //ルートの距離を返却
    public int calculate_route_time(ArrayList<Room> route){

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
                array.add(i);
            }
        }
        for (int i = 0; i < array.size(); i++) {
            room.remove(array.get(i));
        }

        /*//最大日数を超えたもの補充
        array = new ArrayList<>();
        for (int i = 0; i < room.size(); i++){
            if(room.get(i).getAreaNumber() == current_area && room.get(i).isOverLongest(day)){
                route.add(room.get(i));
                array.add(i);
            }
        }
        for (int i = 0; i < array.size(); i++) {
            room.remove(array.get(i));
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
        for (int i = 0; i < setting.limit; i++) {
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
                array.add(i);
            }
        }
        for (int i = 0; i < array.size(); i++) {
            room.remove(array.get(i));
        }

        //最大日数を超えたもの補充
        /*array = new ArrayList<>();
        for (int i = 0; i < room.size(); i++){
            if(room.get(i).isOverLongest(day)){
                route.add(room.get(i));
                array.add(i);
            }
        }
        for (int i = 0; i < array.size(); i++) {
            room.remove(array.get(i));
        }*/

        //suf_rate小さい順に並べる
        for (int i = 0; i < room.size(); i++) {
            for (int j = 0; j < room.size(); j++) {
                if(i < j){
                    if(room.get(i).suf_rate() > r[j].suf_rate()){
                        Room tmp = room.get(i);
                        room.set(i, room.get(j));
                        room.set(j, tmp);
                    }
                }
            }
        }

        for (int i = 0; i < route.size(); i++) {
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



    public ArrayList<Room> staticBase(Room[] rooms, int current_area){

        //TODO:ルート固定をベースに行かないところ、追加するところをいれ、修正程度のルートを作成
        return null;
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

        //100000回適当に入れ替えてみて一番いいやつを採用
        ArrayList<Room> route_tmp = new ArrayList<>(route);
        Random rand = new Random();

        int best = calculate_route_time(route);

        for (int i = 0; i < 100000; i++) {
            int ran1 = rand.nextInt(route_tmp.size());
            int ran2 = rand.nextInt(route_tmp.size());
            Room tmp = route_tmp.get(ran1);
            route_tmp.set(ran1, route_tmp.get(ran2));
            route_tmp.set(ran2, tmp);
            if(calculate_route_time(route_tmp) < best){
                best = calculate_route_time(route_tmp);
                route = new ArrayList<>(route_tmp);
                route_tmp = new ArrayList<>(route);
            }
            if(rand.nextInt(100) < 50){
                route_tmp = new ArrayList<>(route);
            }
        }
        return route;
    }








    //TPSをとく
    private ArrayList<Room> routeOptimizer(ArrayList<Room> rawRoot){

        //1. 30個ルートを作製
        //2. ルートの距離を計算
        //3. 30個から距離が短いルート3つを選択
        //TODO: 交叉をどの様に行なうか考えなければいけない
        //4. その3つと自分を除いた29個を確立的に交叉を行いルートを30個作成する
        //5. 突然変異確率に応じて、突然変異を行う(何処か1箇所スワップさせる)
        //6. ランダム作成したルート10個追加
        //7. 2,3,4を繰り返す
        //1000世代でストップ


        //rawRoot:id順のルート

        ArrayList<ArrayList<Room>> applicants = new ArrayList<>();

        //30個ルートを作成
        int applicants_num = 30;
        for (int i = 0; i < applicants_num; i++) {

            //1-20のセットを作成
            int[] numset = new int[20];
            for (int j = 1; j < 21; j++) {
                numset[j] = j;
            }

            //1-20の順番ランダムな配列
            for (int j = 0; j < 100; j++) {
                Random rand = new Random();
                int num1 = rand.nextInt(20);
                int num2 = rand.nextInt(20);
                int tmp = numset[num1];
                numset[num1] = numset[num2];
                numset[num2] = tmp;
            }

            ArrayList<Room> routes = new ArrayList<>();
            for (int j = 0; j < numset.length; j++) {
                routes.add(rawRoot.get(numset[j]));
            }

            applicants.add(routes);
        }



        //ルートの距離順に並び替えて、短い順に3ルート抜き出す
        //ルートの距離を保持
        int[] route_time = new int[applicants_num];
        for (int i = 0; i < applicants_num; i++) {
            route_time[i] = calculate_route_time(applicants.get(i));
        }

        //ルートの距離短い順に並べる
        for (int i = 0; i < applicants_num; i++) {
            for (int j = 0; j < applicants_num; j++) {

                if(route_time[i] > route_time[j]){
                    int tmp_route_time = route_time[i];
                    route_time[i] = route_time[j];
                    route_time[j] = tmp_route_time;

                    ArrayList tmp_applicants = applicants.get(i);
                    applicants.set(i,applicants.get(j));
                    applicants.set(j,tmp_applicants);
                }
            }
        }













        ArrayList<Room> answer_route = new ArrayList<>();

        return answer_route;
    }

}
