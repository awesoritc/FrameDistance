import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Room {
    Setting setting;
    String simulatiorType;

    private int roomType, roomId, areaNumber, x_pos, y_pos;
    private int roomMax;
    private int[] distance_to_gravity;
    private int last_replenishment = 0;//最後に補充された日
    private boolean expire_flag = false;

    private final ArrayList<Goods> goodsList = new ArrayList<>();



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

        //if(simulatorType.equals(setting.simulatorType_dynamic)) Util.write_gravity(roomId, areaNumber, distance_to_gravity);

    }


    //メインで使うもの

    public int[] do_consume_room(int day){

        int sales = 0;
        int shortage = 0;
        for(Goods aGoods: goodsList){
            int tmp[] = aGoods.do_consume_goods();
            sales += tmp[0];
            shortage += tmp[1];
        }

        //TODO:日にちごとに不足個数を書き出し
        try{
            FileWriter w = new FileWriter(new File("/Users/takuyamorimatsu/Documents/GitHub/FrameDistance/FrameDistance/Data/shortage_day_room.csv"), true);
            w.write( simulatiorType + "," + last_replenishment + "," + day + "," + roomId + "," + shortage + "\n");
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return new int[]{sales, shortage};
    }


    //部屋での賞味期限切れになった個数を返す
    public int do_replenishment_room(int day){

        int expire_count = 0;
        for(Goods aGoods: goodsList){
            expire_count += aGoods.do_replenishment_goods();
        }

        last_replenishment = day;
        expire_flag = false;

        return expire_count;
    }


    //1日の終わりに行う
    public void finish_day_room(){
        for (int i = 0; i < goodsList.size(); i++) {
            boolean tmp = goodsList.get(i).finish_day_goods();
            if(tmp){
                expire_flag = true;
            }
        }
    }



    //不足個数を返す
    public int expect_shortage(int current_area){
        int interval = Util.get_interval(current_area, areaNumber);

        int expect = 0;
        for (Goods aGoods_list : goodsList) {
            expect += aGoods_list.expect_shortage_goods(interval);
        }

        return expect;
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

    public int[] getDistance_to_gravity() {
        return distance_to_gravity;
    }

    public ArrayList<Goods> getGoodsList() {
        return goodsList;
    }

    public int getRoomType() {
        return roomType;
    }

    public boolean isExpire_flag() {
        return expire_flag;
    }
}
