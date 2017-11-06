import java.util.ArrayList;

public class Room {
    Setting setting;
    String simulatiorType;

    private int roomType, roomId, areaNumber, x_pos, y_pos;
    private int roomMax;
    int[] distance_to_gravity;
    int last_replenishment = 0;//最後に補充された日

    ArrayList<Goods> goodsList = new ArrayList<>();



    Room(int roomId, int areaNumber, int x_pos, int y_pos, int roomType, int[][] gravity_points, Setting setting, String simulatorType){

        this.roomId = roomId;
        this.areaNumber = areaNumber;
        this.x_pos = x_pos;
        this.y_pos = y_pos;

        this.setting = setting;
        this.simulatiorType = simulatorType;

        this.roomType = roomType;
        this.roomMax = setting.room_max[roomType];
        this.distance_to_gravity = new int[setting.area];


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

    public int[] do_consume_room(){

        int sales = 0;
        int shortage = 0;
        for(Goods aGoods: goodsList){
            int tmp[] = aGoods.do_consume_goods();
            sales += tmp[0];
            shortage += tmp[1];
        }

        return new int[]{sales, shortage};
    }

    public void do_replenishment_room(int day){

        for(Goods aGoods: goodsList){
            aGoods.do_replenishment_goods();
        }

        last_replenishment = day;
    }




    //補充優先度を返す
    public double rep_value(int current_area){


        int interval = Util.get_interval(current_area, areaNumber);

        double expect = 0;
        for (Goods aGoods_list : goodsList) {
            expect += aGoods_list.expect_shortage_goods(interval);
        }

        return (expect / (double)distance_to_gravity[current_area]);
    }



    //部屋の商品充足率を返す
    public double suf_rate(){

        int max = 0;
        double amount = 0;
        for(Goods aGoods: goodsList){
            max += aGoods.getMax();
            amount += aGoods.getStock();
        }

        return amount / max;
    }


    public boolean isOverLongest(int day){
        return ((day - last_replenishment) > setting.longest_interval);
    }



    //使い捨てのもの

    //商品の登録
    public void register_goods(int goodsType){
        goodsList.add(new Goods(roomType, goodsType, setting, simulatiorType));
    }






    //getter,setter

    public int getRoomId() {
        return roomId;
    }

    public int getAreaNumber() {
        return areaNumber;
    }

    public int getX_pos() {
        return x_pos;
    }

    public int getY_pos() {
        return y_pos;
    }

    public ArrayList<Goods> getGoodsList() {
        return goodsList;
    }

    public int getRoomType() {
        return roomType;
    }
}
