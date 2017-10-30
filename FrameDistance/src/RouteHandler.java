import java.lang.reflect.Array;
import java.util.ArrayList;

public class RouteHandler {
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
            for (int i = 0; i < setting.rooms_area; i++) {
                route.add(rooms[i+(current_area*setting.rooms_area)]);
            }
        }else if(simulatorType.equals(setting.simulatorType_dynamic)){
            //変動のルート
            route = basedOnValue(rooms, current_area);
        }

        return route;
    }



    public int calculate_route_time(ArrayList<Room> route){

        if(route.size() != 0){

            int current_x = route.get(0).getX_pos();
            int current_y = route.get(0).getY_pos();

            int routetime = 0;
            for (int i = 1; i < route.size(); i++) {

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
            }

            return routetime;
        }

        return 0;
    }








    //ルート作成用のメソッド
    private ArrayList<Room> basedOnValue(Room[] r, int current_area){

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

        ArrayList<Room> route = new ArrayList<>();
        for (int i = 0; i < setting.limit; i++) {
            if(r[i].rep_value(current_area) > 0){
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
