import java.util.ArrayList;

public class Room {
    Setting setting;
    String simulatiorType;

    int roomType, roomId, areaNumber, x_pos, y_pos;
    int[] distance_to_gravity;

    ArrayList<Goods> goodsList = new ArrayList<>();



    Room(int roomId, int areaNumber, int x_pos, int y_pos, int roomType, int[][] gravity_points, Setting setting, String simulatorType){

        this.roomId = roomId;
        this.areaNumber = areaNumber;
        this.x_pos = x_pos;
        this.y_pos = y_pos;

        this.setting = setting;
        this.simulatiorType = simulatorType;

        this.roomType = roomType;



        //重心との距離を設定
        for(int i = 0; i < setting.area; i++){
            int distance = 0;
            if(gravity_points[i][0] > x_pos){
                distance += gravity_points[i][0] - x_pos;
            }else{
                distance += x_pos - gravity_points[i][0];
            }

            if(gravity_points[i][1] > y_pos){
                distance += gravity_points[i][1] - y_pos;
            }else{
                distance += y_pos - gravity_points[i][1];
            }

            if(distance == 0){
                distance = 1;
            }
            this.distance_to_gravity[i] = distance;
        }


    }


    //メインで使うもの

    public void do_consume_room(){}

    public void do_replenishment_room(){}




    //補充優先度を返す
    public double rep_value(int current_area){


        int interval = Util.get_interval(current_area, areaNumber);

        int expect = 0;
        for (Goods aGoods_list : goodsList) {
            expect += aGoods_list.expect_shortage_goods(interval);
        }

        return (expect / (double)distance_to_gravity[current_area]);
    }




    //使い捨てのもの

    //商品の登録
    public void register_goods(int goodsType){
        goodsList.add(new Goods(roomType, goodsType, setting, simulatiorType));
    }

    
}
