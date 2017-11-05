import java.util.ArrayList;

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
        }else if(simulatorType.equals(setting.simulatorType_dynamic)){
            //変動のルート
            if(setting.routeType.equals(setting.routeType_value)){
                route = setIdOrder(basedOnValue(rooms, current_area));
            }else if(setting.routeType.equals(setting.routeType_greedy)){
                route = setIdOrder(basedOnSuf_rate(rooms, current_area));
            }

        }

        return route;
    }



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

            return routetime;
        }

        return 0;
    }








    //ルート作成用のメソッド

    //補充優先度
    private ArrayList<Room> basedOnValue(Room[] r, int current_area){

        //value大きい順に並べる
        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < r.length; j++) {
                if(i < j){
                    if(r[i].rep_value(current_area) < r[j].rep_value(current_area)){
                        Room tmp = r[i];
                        r[i] = r[j];
                        r[j] = tmp;
                    }
                }
            }
        }

        ArrayList<Room> route = new ArrayList<>();//補充に回る部屋の集合

        //最大日数を超えて補充に回っていない部屋を追加
        for (Room aRooms: r){
            if(aRooms.isOverLongest(day)){
                route.add(aRooms);
            }
        }


        //ルートに優先度の高い部屋を追加
        for (int i = 0; i < setting.limit; i++) {
            if(r[i].rep_value(current_area) > 0){
                route.add(r[i]);
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

        //suf_rate小さい順に並べる
        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < r.length; j++) {
                if(i < j){
                    if(r[i].suf_rate() > r[j].suf_rate()){
                        Room tmp = r[i];
                        r[i] = r[j];
                        r[j] = tmp;
                    }
                }
            }
        }

        ArrayList<Room> route = new ArrayList<>();
        for (int i = 0; i < setting.limit; i++) {
            if(r[i].suf_rate() < 1){
                route.add(r[i]);
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
}
